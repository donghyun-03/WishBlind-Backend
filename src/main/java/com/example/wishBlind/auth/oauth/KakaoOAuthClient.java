package com.example.wishBlind.auth.oauth;

import com.example.wishBlind.auth.config.OAuthProperties;
import com.example.wishBlind.auth.domain.OAuthProvider;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;

/**
 * 카카오 access token 검증 + 사용자 정보 조회.
 *
 * ★ access_token_info를 먼저 부르는 이유 ★
 * /v2/user/me는 유효한 카카오 토큰이면 어느 앱에서 발급됐든 응답한다. app_id를 대조하지
 * 않으면 공격자가 자기 앱 토큰으로 남의 계정에 로그인할 수 있다(토큰 치환).
 *
 * email/nickname은 비어 있을 수 있다 — 카카오 콘솔에서 동의항목을 안 켰거나 사용자가
 * 동의하지 않은 경우다. 그래도 로그인 자체는 성립해야 하므로 실패시키지 않는다.
 */
@Component
public class KakaoOAuthClient implements OAuthClient {

    private static final Logger log = LoggerFactory.getLogger(KakaoOAuthClient.class);

    private static final String TOKEN_INFO_URI = "https://kapi.kakao.com/v1/user/access_token_info";
    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient;
    private final OAuthProperties properties;

    public KakaoOAuthClient(RestClient oauthRestClient, OAuthProperties properties) {
        this.restClient = oauthRestClient;
        this.properties = properties;
    }

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public OAuthUserInfo fetch(String accessToken) {
        String expectedAppId = properties.kakaoAppId();
        if (expectedAppId == null || expectedAppId.isBlank()) {
            // 설정이 없으면 통과시키지 않는다. 검증 없이 여는 편보다 소셜 로그인이 막히는 편이 낫다.
            log.error("oauth.kakao-app-id 미설정 — 카카오 로그인을 거부한다");
            throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
        }

        TokenInfo tokenInfo = get(URI.create(TOKEN_INFO_URI), accessToken, TokenInfo.class);
        if (tokenInfo.appId() == null || !expectedAppId.equals(String.valueOf(tokenInfo.appId()))) {
            log.warn("카카오 토큰의 app_id가 우리 앱이 아님 — 로그인 거부");
            throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
        }

        UserInfo userInfo = get(URI.create(USER_INFO_URI), accessToken, UserInfo.class);
        if (userInfo.id() == null) {
            throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
        }

        Account account = userInfo.kakaoAccount();
        String email = (account == null) ? null : account.email();
        String nickname = (account == null || account.profile() == null)
                ? null : account.profile().nickname();

        return new OAuthUserInfo(
                OAuthProvider.KAKAO, String.valueOf(userInfo.id()), email, nickname);
    }

    private <T> T get(URI uri, String accessToken, Class<T> type) {
        try {
            T body = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(type);
            if (body == null) {
                throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
            }
            return body;
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
        }
    }

    /** https://kapi.kakao.com/v1/user/access_token_info */
    record TokenInfo(Long id, @JsonProperty("app_id") Long appId) {
    }

    /** https://kapi.kakao.com/v2/user/me 응답 중 필요한 것만. */
    record UserInfo(Long id, @JsonProperty("kakao_account") Account kakaoAccount) {
    }

    record Account(String email, Profile profile) {
    }

    record Profile(String nickname) {
    }
}
