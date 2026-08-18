package com.example.wishBlind.auth.oauth;

import com.example.wishBlind.auth.domain.OAuthProvider;

/**
 * 앱 SDK가 발급받은 access token을 제공자에게 검증하고 사용자 정보를 받아온다.
 * OAuth2 redirect 왕복은 쓰지 않는다. 인터페이스로 둔 것은 테스트에서 대체하기 위해서다.
 */
public interface OAuthClient {

    OAuthProvider provider();

    /**
     * @param accessToken 클라이언트가 제공자 SDK로 발급받은 토큰
     * @return 사용자 정보
     * @throws com.example.wishBlind.global.exception.BusinessException 검증 실패 시 OAUTH_VERIFICATION_FAILED
     */
    OAuthUserInfo fetch(String accessToken);
}
