package com.example.wishBlind.ai.anthropic;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Anthropic API 설정.
 *
 * @param apiKey         ANTHROPIC_API_KEY
 * @param model          모델 ID (기본 claude-opus-5)
 * @param maxTokens      응답 최대 토큰
 * @param timeoutSeconds 호출 타임아웃(초)
 */
@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(
        String apiKey,
        String model,
        long maxTokens,
        long timeoutSeconds
) {
}
