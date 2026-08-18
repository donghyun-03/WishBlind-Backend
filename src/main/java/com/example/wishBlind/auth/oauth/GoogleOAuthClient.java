package com.example.wishBlind.auth.oauth;

import com.example.wishBlind.auth.config.OAuthProperties;
import com.example.wishBlind.auth.domain.OAuthProvider;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * 구글 access token 검증 + 사용자 정보 조회.
 *
 * ★ tokeninfo를 먼저 부르는 이유 ★
 * userinfo는 "유효한 구글 토큰"이면 어떤 앱에서 발급됐든 응답한다. 그래서 userinfo만
 * 믿으면, 공격자가 자기 앱에서 받은 토큰으로 남의 계정에 로그인할 수 있다(토큰 치환).
 * tokeninfo의 aud가 우리 클라이언트 ID인지 대조해야 "우리 앱에 발급된 토큰"임이 보장된다.
 *
 * 응답을 JsonNode가 아니라 record로 받는 이유: JsonNode는 Jackson 버전에 묶인 타입이라
 * (Boot 4는 Jackson 3) 버전이 바뀌면 조용히 깨진다. record는 어느 변환기든 채워준다.
 */
@Component
public class GoogleOAuthClient implements OAuthClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthClient.class);

    private static final String TOKEN_INFO_URI = "https://oauth2.googleapis.com/tokeninfo";
    private static final String USER_INFO_URI = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final RestClient restClient;
    private final OAuthProperties properties;

    public GoogleOAuthClient(RestClient oauthRestClient, OAuthProperties properties) {
        this.restClient = oauthRestClient;
        this.properties = properties;
    }

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public OAuthUserInfo fetch(String accessToken) {
        String expectedAudience = properties.googleClientId();
        if (expectedAudience == null || expectedAudience.isBlank()) {
            // 설정이 없으면 통과시키지 않는다. 검증 없이 여는 편보다 소셜 로그인이 막히는 편이 낫다.
            log.error("oauth.google-client-id 미설정 — 구글 로그인을 거부한다");
            throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
        }

        URI tokenInfoUri = URI.create(UriComponentsBuilder.fromUriString(TOKEN_INFO_URI)
                .queryParam("access_token", accessToken)
                .toUriString());
        TokenInfo tokenInfo = get(tokenInfoUri, null, TokenInfo.class);

        if (!expectedAudience.equals(tokenInfo.aud())) {
            log.warn("구글 토큰의 aud가 우리 앱이 아님 — 로그인 거부");
            throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
        }

        UserInfo userInfo = get(URI.create(USER_INFO_URI), accessToken, UserInfo.class);
        if (userInfo.sub() == null) {
            throw new BusinessException(ErrorCode.OAUTH_VERIFICATION_FAILED);
        }

        return new OAuthUserInfo(OAuthProvider.GOOGLE, userInfo.sub(), userInfo.email(), userInfo.name());
    }

    private <T> T get(URI uri, String bearerToken, Class<T> type) {
        try {
            T body = restClient.get()
                    .uri(uri)
                    .headers(headers -> {
                        if (bearerToken != null) {
                            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
                        }
                    })
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

    /** https://oauth2.googleapis.com/tokeninfo 응답 중 필요한 것만. */
    record TokenInfo(String aud, String sub) {
    }

    /** https://www.googleapis.com/oauth2/v3/userinfo 응답 중 필요한 것만. */
    record UserInfo(String sub, String email, String name) {
    }
}
