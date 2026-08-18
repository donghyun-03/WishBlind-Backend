package com.example.wishBlind.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정. secret은 HS256 요구사항상 최소 32바이트여야 한다.
 *
 * @param secret                 서명 키 (환경변수 JWT_SECRET)
 * @param accessTokenValidity    access token 유효기간(초)
 * @param refreshTokenValidity   refresh token 유효기간(초)
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenValidity,
        long refreshTokenValidity
) {
}
