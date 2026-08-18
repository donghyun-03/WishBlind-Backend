package com.example.wishBlind.ai.gemini;

import com.example.wishBlind.ai.RuleBasedAiClient;
import com.example.wishBlind.ai.domain.LlmCallLog;
import com.example.wishBlind.ai.dto.AiRecommendationCommand;
import com.example.wishBlind.ai.dto.AiRecommendationResult;
import com.example.wishBlind.ai.repository.LlmCallLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * ★ 이 테스트가 지키는 것: AI가 실패해도 추천 자체는 성립한다.
 *
 * 코멘트는 추천을 읽기 좋게 만드는 값이지 추천의 성립 조건이 아니다.
 * 여기가 깨지면 Gemini 장애나 키 만료가 곧 서비스 장애가 된다.
 */
class GeminiAiClientTest {

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent";

    private static final AiRecommendationCommand COMMAND = new AiRecommendationCommand(
            "생일", "고마운 마음", List.of("실용적"), "가죽 카드지갑",
            87, List.of("선호 색상 반영"), List.of("사이즈가 작은 편"));

    private final RuleBasedAiClient fallback = new RuleBasedAiClient();
    private final LlmCallLogRepository callLogRepository = mock(LlmCallLogRepository.class);

    @Test
    @DisplayName("API 키가 없으면 호출하지 않고 규칙 기반 코멘트를 쓴다")
    void fallsBackWhenApiKeyMissing() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        AiRecommendationResult result = newClient(builder, "").generateComment(COMMAND);

        assertThat(result.comment()).isEqualTo(fallback.generateComment(COMMAND).comment());
        server.verify(); // 아무 요청도 나가지 않아야 한다
        verify(callLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("정상 응답이면 Gemini가 쓴 코멘트를 그대로 쓰고 성공 로그를 남긴다")
    void usesGeneratedComment() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(ENDPOINT))
                // 키는 URL이 아니라 헤더로 보낸다 — URL은 로그에 남기 쉽다
                .andExpect(header("x-goog-api-key", "test-key"))
                .andRespond(withSuccess("""
                        {
                          "candidates": [
                            {"content": {"parts": [{"text": "정성이 담긴 선물입니다."}]}, "finishReason": "STOP"}
                          ],
                          "usageMetadata": {"promptTokenCount": 96, "candidatesTokenCount": 47}
                        }
                        """, MediaType.APPLICATION_JSON));

        AiRecommendationResult result = newClient(builder, "test-key").generateComment(COMMAND);

        assertThat(result.comment()).isEqualTo("정성이 담긴 선물입니다.");

        ArgumentCaptor<LlmCallLog> captor = ArgumentCaptor.forClass(LlmCallLog.class);
        verify(callLogRepository).save(captor.capture());
        LlmCallLog saved = captor.getValue();
        assertThat(saved.isSuccess()).isTrue();
        assertThat(saved.getInputTokens()).isEqualTo(96L);
        assertThat(saved.getOutputTokens()).isEqualTo(47L);
        // 프롬프트 원문이 아니라 해시만 남아야 한다 (취향 정보가 들어 있음)
        assertThat(saved.getPromptHash()).hasSize(64).doesNotContain("고마운 마음");
    }

    @Test
    @DisplayName("호출이 실패해도 예외를 던지지 않고 규칙 기반으로 내려간다")
    void fallsBackWhenCallFails() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(ENDPOINT)).andRespond(withServerError());

        AiRecommendationResult result = newClient(builder, "test-key").generateComment(COMMAND);

        assertThat(result.comment()).isEqualTo(fallback.generateComment(COMMAND).comment());

        ArgumentCaptor<LlmCallLog> captor = ArgumentCaptor.forClass(LlmCallLog.class);
        verify(callLogRepository).save(captor.capture());
        assertThat(captor.getValue().isSuccess()).isFalse();
        assertThat(captor.getValue().getFailureCode()).isEqualTo("L006");
    }

    @Test
    @DisplayName("응답에 텍스트가 없으면(차단·잘림 등) 규칙 기반으로 내려간다")
    void fallsBackWhenNoText() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("""
                        {"candidates": [{"finishReason": "SAFETY"}]}
                        """, MediaType.APPLICATION_JSON));

        AiRecommendationResult result = newClient(builder, "test-key").generateComment(COMMAND);

        assertThat(result.comment()).isEqualTo(fallback.generateComment(COMMAND).comment());

        ArgumentCaptor<LlmCallLog> captor = ArgumentCaptor.forClass(LlmCallLog.class);
        verify(callLogRepository).save(captor.capture());
        assertThat(captor.getValue().getFailureCode()).isEqualTo("L002");
    }

    private GeminiAiClient newClient(RestClient.Builder builder, String apiKey) {
        return new GeminiAiClient(
                builder.build(),
                new GeminiProperties(apiKey, "gemini-3.5-flash-lite", 600, 30),
                new PromptBuilder(),
                callLogRepository,
                fallback);
    }
}
