package com.example.wishBlind.ai.anthropic;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/** Anthropic 클라이언트 빈. */
@Configuration
@EnableConfigurationProperties(AnthropicProperties.class)
public class AnthropicConfig {

    @Bean
    public AnthropicClient anthropicClient(AnthropicProperties properties) {
        return AnthropicOkHttpClient.builder()
                .apiKey(properties.apiKey())
                .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                .build();
    }
}
