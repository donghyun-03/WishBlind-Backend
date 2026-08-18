package com.example.wishBlind.ai.anthropic;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.TextBlock;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Claude로 추천 코멘트를 생성한다. {@link RuleBasedAiClient}를 @Primary로 대체한다.
 *
 * ★ 실패해도 추천 자체를 깨뜨리지 않는다 ★
 * 코멘트는 추천 결과를 읽기 좋게 만드는 값이지, 추천의 성립 조건이 아니다.
 * API 키가 없거나 호출이 실패하면 규칙 기반 코멘트로 조용히 내려간다 —
 * 코멘트 하나 때문에 사용자의 선물 추천 전체가 실패하는 편이 훨씬 나쁘다.
 *
 * 재시도는 하지 않는다. 폴백이 항상 성공하므로 재시도로 얻을 게 없고,
 * 후보 3개마다 호출되므로 재시도가 곧 3배 지연이 된다.
 */
@Component
@Primary
public class ClaudeAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAiClient.class);

    private final AnthropicClient client;
    private final AnthropicProperties properties;
    private final PromptBuilder promptBuilder;
    private final LlmCallLogRepository callLogRepository;
    private final RuleBasedAiClient fallback;

    public ClaudeAiClient(AnthropicClient client,
                          AnthropicProperties properties,
                          PromptBuilder promptBuilder,
                          LlmCallLogRepository callLogRepository,
                          RuleBasedAiClient fallback) {
        this.client = client;
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
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(properties.model())
                    .maxTokens(properties.maxTokens())
                    // 코멘트 한 문단이라 깊게 생각할 이유가 없다. effort를 낮춰 지연과 비용을 줄인다.
                    .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.LOW).build())
                    .system(promptBuilder.system())
                    .addUserMessage(userPrompt)
                    .build();

            Message message = client.messages().create(params);
            long latencyMs = System.currentTimeMillis() - startedAt;

            // 안전 분류기가 거절하면 예외가 아니라 HTTP 200으로 온다. content보다 먼저 본다.
            if (isRefusal(message)) {
                return degrade(command, promptHash, latencyMs, ErrorCode.LLM_REFUSAL, null);
            }

            String comment = extractText(message);
            if (comment == null || comment.isBlank()) {
                return degrade(command, promptHash, latencyMs, ErrorCode.LLM_PARSE_ERROR, null);
            }

            callLogRepository.save(LlmCallLog.success(
                    properties.model(), promptHash, latencyMs,
                    message.usage().inputTokens(), message.usage().outputTokens()));

            return new AiRecommendationResult(comment.trim());

        } catch (RuntimeException e) {
            return degrade(command, promptHash,
                    System.currentTimeMillis() - startedAt, ErrorCode.LLM_CALL_FAILED, e);
        }
    }

    /** 실패를 기록하고 규칙 기반 코멘트로 내려간다. */
    private AiRecommendationResult degrade(AiRecommendationCommand command, String promptHash,
                                           long latencyMs, ErrorCode errorCode, Exception cause) {
        if (cause != null) {
            log.warn("Claude 코멘트 생성 실패({}), 규칙 기반으로 대체", errorCode.getCode(), cause);
        } else {
            log.warn("Claude 코멘트 생성 실패({}), 규칙 기반으로 대체", errorCode.getCode());
        }
        callLogRepository.save(LlmCallLog.failure(
                properties.model(), promptHash, latencyMs, errorCode.getCode()));
        return fallback.generateComment(command);
    }

    private boolean isRefusal(Message message) {
        return message.stopReason()
                .map(reason -> reason.toString().toLowerCase().contains("refusal"))
                .orElse(false);
    }

    private String extractText(Message message) {
        return message.content().stream()
                .flatMap(block -> block.text().stream())
                .map(TextBlock::text)
                .findFirst()
                .orElse(null);
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
}
