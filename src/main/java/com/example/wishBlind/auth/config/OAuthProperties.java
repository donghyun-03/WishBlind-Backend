package com.example.wishBlind.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 소셜 토큰의 발급 대상 검증에 쓰는 식별자.
 *
 * 시크릿이 아니라 공개 식별자다. 그래도 반드시 필요하다 —
 * 이 값이 없으면 "우리 앱에 발급된 토큰"과 "남의 앱에 발급된 토큰"을 구분할 수 없다.
 *
 * @param googleClientId 구글 OAuth 클라이언트 ID (tokeninfo의 aud와 대조)
 * @param kakaoAppId     카카오 앱 ID (access_token_info의 app_id와 대조)
 */
@ConfigurationProperties(prefix = "oauth")
public record OAuthProperties(String googleClientId, String kakaoAppId) {
}
