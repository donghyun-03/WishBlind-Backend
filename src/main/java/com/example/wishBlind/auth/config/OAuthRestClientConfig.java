package com.example.wishBlind.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * 소셜 제공자 호출용 RestClient.
 * 타임아웃을 명시하는 이유: 기본값은 무제한이라 제공자가 응답을 안 주면
 * 로그인 요청 스레드가 그대로 물린다.
 */
@Configuration
public class OAuthRestClientConfig {

    @Bean
    public RestClient oauthRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().requestFactory(factory).build();
    }
}
