package com.example.wishBlind.ai.gemini;

import com.example.wishBlind.ai.AiClient;
import com.example.wishBlind.ai.RuleBasedAiClient;
import com.example.wishBlind.ai.domain.LlmCallLog;
import com.example.wishBlind.ai.dto.AiRecommendationCommand;
import com.example.wishBlind.ai.dto.AiRecommendationResult;
import com.example.wishBlind.ai.repository.LlmCallLogRepository;
import com.example.wishBlind.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Gemini로 추천 코멘트를 생성한다. {@link RuleBasedAiClient}를 @Primary로 대체한다.
 *
 * ★ 실패해도 추천 자체를 깨뜨리지 않는다 ★
 * 코멘트는 추천 결과를 읽기 좋게 만드는 값이지, 추천의 성립 조건이 아니다.
 * 키가 없거나 호출이 실패하면 규칙 기반 코멘트로 조용히 내려간다 —
 * 코멘트 하나 때문에 사용자의 선물 추천 전체가 실패하는 편이 훨씬 나쁘다.
 *
 * 재시도는 하지 않는다. 폴백이 항상 성공하므로 재시도로 얻을 게 없고,
 * 후보 3개마다 호출되므로 재시도가 곧 3배 지연이 된다.
 */
@Component
@Primary
public class GeminiAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiClient.class);

    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    private final RestClient restClient;
    private final GeminiProperties properties;
    private final PromptBuilder promptBuilder;
    private final LlmCallLogRepository callLogRepository;
    private final RuleBasedAiClient fallback;

    public GeminiAiClient(RestClient geminiRestClient,
                          GeminiProperties properties,
                          PromptBuilder promptBuilder,
                          LlmCallLogRepository callLogRepository,
                          RuleBasedAiClient fallback) {
        this.restClient = geminiRestClient;
        this.properties = properties;
        this.promptBuilder = promptBuilder;
        this.callLogRepository = callLogRepository;
        this.fallback = fallback;
    }

    @Override
    public AiRecommendationResult generateComment(AiRecommendationCommand command) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            return fallback.generateComment(command);
        }

        String userPrompt = promptBuilder.user(command);
        String promptHash = sha256(userPrompt);
        long startedAt = System.currentTimeMillis();

        try {
            GeminiResponse response = restClient.post()
                    .uri(ENDPOINT_TEMPLATE, properties.model())
                    .header("x-goog-api-key", properties.apiKey())
                    .body(requestBody(userPrompt))
                    .retrieve()
                    .body(GeminiResponse.class);

            long latencyMs = System.currentTimeMillis() - startedAt;

            String comment = extractText(response);
            if (comment == null || comment.isBlank()) {
                return degrade(command, promptHash, latencyMs, ErrorCode.LLM_PARSE_ERROR, null);
            }

            callLogRepository.save(LlmCallLog.success(
                    properties.model(), promptHash, latencyMs,
                    inputTokens(response), outputTokens(response)));

            return new AiRecommendationResult(comment.trim());

        } catch (RuntimeException e) {
            return degrade(command, promptHash,
                    System.currentTimeMillis() - startedAt, ErrorCode.LLM_CALL_FAILED, e);
        }
    }

    private Map<String, Object> requestBody(String userPrompt) {
        return Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", promptBuilder.system()))),
                "contents", List.of(Map.of("parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of("maxOutputTokens", properties.maxOutputTokens()));
    }

    /** 실패를 기록하고 규칙 기반 코멘트로 내려간다. */
    private AiRecommendationResult degrade(AiRecommendationCommand command, String promptHash,
                                           long latencyMs, ErrorCode errorCode, Exception cause) {
        if (cause != null) {
            log.warn("Gemini 코멘트 생성 실패({}), 규칙 기반으로 대체", errorCode.getCode(), cause);
        } else {
            log.warn("Gemini 코멘트 생성 실패({}), 규칙 기반으로 대체", errorCode.getCode());
        }
        callLogRepository.save(LlmCallLog.failure(
                properties.model(), promptHash, latencyMs, errorCode.getCode()));
        return fallback.generateComment(command);
    }

    /**
     * 응답에서 첫 텍스트를 꺼낸다.
     *
     * 안전 필터에 걸리거나(finishReason=SAFETY) 토큰이 잘리면 candidates는 있는데
     * content가 통째로 비어서 온다. 예외가 아니라 200이므로 여기서 걸러야 한다.
     */
    private String extractText(GeminiResponse response) {
        if (response == null || response.candidates() == null) {
            return null;
        }
        return response.candidates().stream()
                .filter(c -> c.content() != null && c.content().parts() != null)
                .flatMap(c -> c.content().parts().stream())
                .map(GeminiResponse.Part::text)
                .filter(t -> t != null && !t.isBlank())
                .findFirst()
                .orElse(null);
    }

    private Long inputTokens(GeminiResponse response) {
        return (response == null || response.usageMetadata() == null)
                ? null : response.usageMetadata().promptTokenCount();
    }

    private Long outputTokens(GeminiResponse response) {
        return (response == null || response.usageMetadata() == null)
                ? null : response.usageMetadata().candidatesTokenCount();
    }

    /** 프롬프트 원문 대신 해시만 남긴다 — 원문에는 회원 취향 정보가 들어 있다. */
    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * generateContent 응답 중 우리가 쓰는 부분만 받는다.
     * 모르는 필드는 무시되므로 Gemini가 필드를 추가해도 깨지지 않는다.
     */
    record GeminiResponse(List<Candidate> candidates, UsageMetadata usageMetadata) {

        record Candidate(Content content, String finishReason) {
        }

        record Content(List<Part> parts) {
        }

        record Part(String text) {
        }

        record UsageMetadata(Long promptTokenCount, Long candidatesTokenCount) {
        }
    }
}
