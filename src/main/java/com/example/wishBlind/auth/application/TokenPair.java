package com.example.wishBlind.auth.application;

/**
 * 발급된 토큰 한 쌍.
 *
 * @param accessToken            JWT
 * @param refreshToken           불투명 랜덤 문자열 (이 값이 클라이언트에 나가는 유일한 시점)
 * @param accessTokenExpiresIn   access token 만료까지 남은 초
 */
public record TokenPair(String accessToken, String refreshToken, long accessTokenExpiresIn) {
}
