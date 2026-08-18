package com.example.wishBlind.ai.anthropic;

import com.anthropic.client.AnthropicClient;
import com.example.wishBlind.ai.RuleBasedAiClient;
import com.example.wishBlind.ai.domain.LlmCallLog;
import com.example.wishBlind.ai.dto.AiRecommendationCommand;
import com.example.wishBlind.ai.dto.AiRecommendationResult;
import com.example.wishBlind.ai.repository.LlmCallLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ★ 이 테스트가 지키는 것: AI가 실패해도 추천 자체는 성립한다.
 *
 * 코멘트는 추천 결과를 읽기 좋게 만드는 값이지 추천의 성립 조건이 아니다.
 * 여기가 깨지면 Anthropic 장애나 키 만료가 곧 서비스 장애가 된다.
 */
class ClaudeAiClientTest {

    private static final AiRecommendationCommand COMMAND = new AiRecommendationCommand(
            "생일", "고마운 마음", List.of("실용적"), "가죽 카드지갑",
            87, List.of("선호 색상 반영"), List.of("사이즈가 작은 편"));

    private final RuleBasedAiClient fallback = new RuleBasedAiClient();
    private final LlmCallLogRepository callLogRepository = mock(LlmCallLogRepository.class);
    private final AnthropicClient anthropicClient = mock(AnthropicClient.class);

    @Test
    @DisplayName("API 키가 없으면 호출하지 않고 규칙 기반 코멘트를 쓴다")
    void fallsBackWhenApiKeyMissing() {
        ClaudeAiClient client = newClient("");

        AiRecommendationResult result = client.generateComment(COMMAND);

        assertThat(result.comment()).isEqualTo(fallback.generateComment(COMMAND).comment());
        verify(anthropicClient, never()).messages();
        verify(callLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Anthropic 호출이 실패해도 예외를 던지지 않고 규칙 기반으로 내려간다")
    void fallsBackWhenCallFails() {
        when(anthropicClient.messages()).thenThrow(new RuntimeException("boom"));
        ClaudeAiClient client = newClient("test-key");

        AiRecommendationResult result = client.generateComment(COMMAND);

        assertThat(result.comment()).isEqualTo(fallback.generateComment(COMMAND).comment());
    }

    @Test
    @DisplayName("실패는 실패 코드와 함께 llm_call_logs에 남는다")
    void recordsFailureLog() {
        when(anthropicClient.messages()).thenThrow(new RuntimeException("boom"));
        ClaudeAiClient client = newClient("test-key");

        client.generateComment(COMMAND);

        ArgumentCaptor<LlmCallLog> captor = ArgumentCaptor.forClass(LlmCallLog.class);
        verify(callLogRepository).save(captor.capture());
        LlmCallLog saved = captor.getValue();
        assertThat(saved.isSuccess()).isFalse();
        assertThat(saved.getFailureCode()).isEqualTo("L006");
        // 프롬프트 원문이 아니라 해시만 남아야 한다 (취향 정보가 들어 있음)
        assertThat(saved.getPromptHash()).hasSize(64).doesNotContain("고마운 마음");
    }

    private ClaudeAiClient newClient(String apiKey) {
        return new ClaudeAiClient(
                anthropicClient,
                new AnthropicProperties(apiKey, "claude-opus-5", 16000, 120),
                new PromptBuilder(),
                callLogRepository,
                fallback);
    }
}
