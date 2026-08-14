package com.example.wishBlind.auth.oauth;

import com.example.wishBlind.auth.domain.OAuthProvider;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 카카오 사용자 정보 조회.
 *
 * 응답에서 email/nickname이 비어 있을 수 있다 — 카카오 개발자 콘솔에서 해당 동의항목을
 * 켜지 않았거나 사용자가 동의하지 않은 경우다. 그래도 로그인은 성립해야 하므로 실패시키지 않는다.
 */
@Component
public class KakaoOAuthClient implements OAuthClient {

    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient;

    public KakaoOAuthClient(RestClient oauthRestClient) {
        this.restClient = oauthRestClient;
    }

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.KAKAO;
    }

    @Override
    public OAuthUserInfo fetch(String accessToken) {
        JsonNode body;
        try {
            body = restClient.get()
                    .uri(USER_INFO_URI)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
        }

        if (body == null || body.path("id").isMissingNode()) {
            throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
        }

        JsonNode account = body.path("kakao_account");
        String email = account.path("email").asText(null);
        String nickname = account.path("profile").path("nickname").asText(null);

        return new OAuthUserInfo(OAuthProvider.KAKAO, body.path("id").asText(), email, nickname);
    }
}
