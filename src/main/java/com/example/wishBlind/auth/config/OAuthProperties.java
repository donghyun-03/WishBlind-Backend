package com.example.wishBlind.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 소셜 토큰의 발급 대상 검증용 식별자. 시크릿은 아니지만 없으면 소셜 로그인을 거부한다.
 *
 * @param googleClientId 구글 tokeninfo의 aud와 대조
 * @param kakaoAppId     카카오 access_token_info의 app_id와 대조
 */
@ConfigurationProperties(prefix = "oauth")
public record OAuthProperties(String googleClientId, String kakaoAppId) {
}
