package com.example.wishBlind.ai.gemini;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Gemini 호출용 RestClient.
 *
 * 타임아웃을 반드시 건다. 기본값은 무한 대기라, Gemini가 응답하지 않으면
 * 추천 요청 스레드가 그대로 묶인다. 후보 3개마다 호출되므로 영향이 3배가 된다.
 * API 키는 이 빈이 아니라 호출 시점에 헤더로 붙인다 — 키가 없어도 앱은 떠야 한다.
 */
@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiConfig {

    @Bean
    public RestClient geminiRestClient(GeminiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // connect는 짧게(2초), read는 설정값(기본 10초). 느리면 빨리 실패시켜 규칙 기반으로 폴백한다.
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(properties.timeoutSeconds()));

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
