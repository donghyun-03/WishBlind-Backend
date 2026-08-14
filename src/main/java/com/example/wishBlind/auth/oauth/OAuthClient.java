package com.example.wishBlind.auth.oauth;

import com.example.wishBlind.auth.domain.OAuthProvider;

/**
 * 소셜 제공자에게 access token을 검증하고 사용자 정보를 받아온다.
 *
 * 클라이언트(앱)가 카카오/구글 SDK로 받은 access token을 서버에 넘기면,
 * 서버가 이 포트를 통해 제공자에게 직접 물어본다. OAuth2 redirect 왕복은 쓰지 않는다.
 *
 * 인터페이스로 둔 이유는 하나 — 테스트에서 실제 카카오/구글을 때리지 않기 위해서다.
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
