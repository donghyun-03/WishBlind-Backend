package com.example.wishBlind.ai.anthropic;

import com.anthropic.client.AnthropicClient;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.example.wishBlind.ai.AiRecommendCommand;
import com.example.wishBlind.ai.AiRecommendation;
import com.example.wishBlind.ai.AiRecommender;
import com.example.wishBlind.ai.domain.LlmCallLog;
import com.example.wishBlind.ai.repository.LlmCallLogRepository;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Anthropic Claude로 최종 추천 3개를 고른다.
 *
 * 자동 재시도를 하지 않는 이유: 구성도 6번의 "다시 시도"는 사용자가 누르는 명시적
 * 액션이고, 그 루프는 5번 추천 job이 소유한다. 여기서 또 재시도하면 두 겹이 된다.
 */
@Service
public class ClaudeAiRecommender implements AiRecommender {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAiRecommender.class);

    private final AnthropicClient client;
    private final AnthropicProperties properties;
    private final PromptBuilder promptBuilder;
    private final LlmCallLogRepository callLogRepository;

    public ClaudeAiRecommender(AnthropicClient client,
                               AnthropicProperties properties,
                               PromptBuilder promptBuilder,
                               LlmCallLogRepository callLogRepository) {
        this.client = client;
        this.properties = properties;
        this.promptBuilder = promptBuilder;
        this.callLogRepository = callLogRepository;
    }

    @Override
    public AiRecommendation recommend(AiRecommendCommand command) {
        if (command.candidates() == null || command.candidates().isEmpty()) {
            throw new BusinessException(ErrorCode.LLM_NO_CANDIDATE);
        }

        String userPrompt = promptBuilder.user(command);
        String promptHash = sha256(userPrompt);
        int candidateCount = command.candidates().size();
        long startedAt = System.currentTimeMillis();

        try {
            StructuredMessageCreateParams<AiRecommendationPayload> params = MessageCreateParams.builder()
                    .model(properties.model())
                    .maxTokens(properties.maxTokens())
                    .system(promptBuilder.system())
                    .addUserMessage(userPrompt)
                    .outputConfig(AiRecommendationPayload.class)
                    .build();

            StructuredMessage<AiRecommendationPayload> message = client.messages().create(params);
            long latencyMs = System.currentTimeMillis() - startedAt;

            // 안전 분류기가 요청을 거절하면 HTTP 200으로 온다. content를 읽기 전에 먼저 확인한다.
            if (isRefusal(message)) {
                recordFailure(promptHash, candidateCount, latencyMs, ErrorCode.LLM_REFUSAL);
                throw new BusinessException(ErrorCode.LLM_REFUSAL);
            }

            AiRecommendationPayload payload = extractPayload(message);
            if (payload == null || payload.picks() == null || payload.picks().isEmpty()) {
                recordFailure(promptHash, candidateCount, latencyMs, ErrorCode.LLM_PARSE_ERROR);
                throw new BusinessException(ErrorCode.LLM_PARSE_ERROR);
            }

            List<AiRecommendation.Pick> picks = PickValidator.validate(toPicks(payload), command);

            callLogRepository.save(LlmCallLog.success(
                    properties.model(), promptHash, candidateCount, latencyMs,
                    message.usage().inputTokens(), message.usage().outputTokens()));

            return new AiRecommendation(picks);

        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.LLM_INVALID_PICK) {
                recordFailure(promptHash, candidateCount,
                        System.currentTimeMillis() - startedAt, ErrorCode.LLM_INVALID_PICK);
            }
            throw e;
        } catch (AnthropicIoException e) {
            log.warn("Anthropic 호출 타임아웃/네트워크 오류", e);
            recordFailure(promptHash, candidateCount,
                    System.currentTimeMillis() - startedAt, ErrorCode.LLM_TIMEOUT);
            throw new BusinessException(ErrorCode.LLM_TIMEOUT);
        } catch (AnthropicServiceException e) {
            log.warn("Anthropic 호출 실패", e);
            recordFailure(promptHash, candidateCount,
                    System.currentTimeMillis() - startedAt, ErrorCode.LLM_CALL_FAILED);
            throw new BusinessException(ErrorCode.LLM_CALL_FAILED);
        }
    }

    private boolean isRefusal(StructuredMessage<AiRecommendationPayload> message) {
        return message.stopReason()
                .map(reason -> "refusal".equalsIgnoreCase(reason.toString()))
                .orElse(false);
    }

    private AiRecommendationPayload extractPayload(StructuredMessage<AiRecommendationPayload> message) {
        return message.content().stream()
                .flatMap(block -> block.text().stream())
                .map(text -> text.text())
                .findFirst()
                .orElse(null);
    }

    private List<AiRecommendation.Pick> toPicks(AiRecommendationPayload payload) {
        return payload.picks().stream()
                .map(p -> new AiRecommendation.Pick(
                        p.productId(),
                        p.rank(),
                        p.matchScore(),
                        p.reasonSummary(),
                        p.reasons() == null ? List.of() : p.reasons(),
                        Map.of(
                                "color", p.colorScore(),
                                "style", p.styleScore(),
                                "practicality", p.practicalityScore()),
                        p.aiComment(),
                        p.considerations() == null ? List.of() : p.considerations()))
                .toList();
    }

    private void recordFailure(String promptHash, int candidateCount, long latencyMs, ErrorCode errorCode) {
        callLogRepository.save(LlmCallLog.failure(
                properties.model(), promptHash, candidateCount, latencyMs, errorCode.getCode()));
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
