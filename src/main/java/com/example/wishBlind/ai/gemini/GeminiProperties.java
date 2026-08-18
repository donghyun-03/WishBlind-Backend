package com.example.wishBlind.ai.gemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gemini API 설정.
 *
 * @param apiKey           GEMINI_API_KEY (Google AI Studio 발급)
 * @param model            모델 ID
 * @param maxOutputTokens  응답 최대 토큰
 * @param timeoutSeconds   호출 타임아웃(초)
 */
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        int maxOutputTokens,
        long timeoutSeconds
) {
}
