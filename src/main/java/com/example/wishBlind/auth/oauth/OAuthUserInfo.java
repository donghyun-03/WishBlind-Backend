package com.example.wishBlind.auth.oauth;

import com.example.wishBlind.auth.domain.OAuthProvider;

/**
 * 소셜 제공자가 알려준 사용자 정보.
 *
 * @param provider       제공자
 * @param providerUserId 제공자 내부 고유 ID (카카오 id, 구글 sub)
 * @param email          제공자가 안 줄 수도 있어 null 허용
 * @param nickname       제공자가 안 줄 수도 있어 null 허용
 */
public record OAuthUserInfo(
        OAuthProvider provider,
        String providerUserId,
        String email,
        String nickname
) {
}
