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
 * 구글 사용자 정보 조회.
 * userinfo 엔드포인트는 토큰이 유효하지 않으면 401을 주므로, 호출 성공 자체가 검증을 겸한다.
 */
@Component
public class GoogleOAuthClient implements OAuthClient {

    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final RestClient restClient;

    public GoogleOAuthClient(RestClient oauthRestClient) {
        this.restClient = oauthRestClient;
    }

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.GOOGLE;
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

        if (body == null || body.path("sub").isMissingNode()) {
            throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
        }

        return new OAuthUserInfo(
                OAuthProvider.GOOGLE,
                body.path("sub").asText(),
                body.path("email").asText(null),
                body.path("name").asText(null));
    }
}
