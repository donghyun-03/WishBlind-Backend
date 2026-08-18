package com.example.wishBlind.auth.oauth;

import com.example.wishBlind.auth.config.OAuthProperties;
import com.example.wishBlind.global.exception.BusinessException;
import com.example.wishBlind.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** 다른 앱에서 발급된 토큰으로는 로그인되지 않아야 한다. */
class OAuthTokenAudienceTest {

    private static final String OUR_GOOGLE_CLIENT_ID = "our-app.apps.googleusercontent.com";
    private static final String OUR_KAKAO_APP_ID = "111111";

    private static final String GOOGLE_TOKENINFO =
            "https://oauth2.googleapis.com/tokeninfo?access_token=tok";
    private static final String GOOGLE_USERINFO =
            "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String KAKAO_TOKENINFO =
            "https://kapi.kakao.com/v1/user/access_token_info";
    private static final String KAKAO_USERINFO =
            "https://kapi.kakao.com/v2/user/me";

    private final OAuthProperties properties =
            new OAuthProperties(OUR_GOOGLE_CLIENT_ID, OUR_KAKAO_APP_ID);

    @Test
    @DisplayName("구글: 다른 앱에 발급된 토큰(aud 불일치)은 거부한다")
    void rejectsGoogleTokenIssuedToAnotherApp() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(GOOGLE_TOKENINFO))
                .andRespond(withSuccess("""
                        {"aud":"attacker-app.apps.googleusercontent.com","sub":"g-999"}
                        """, MediaType.APPLICATION_JSON));

        GoogleOAuthClient client = new GoogleOAuthClient(builder.build(), properties);

        assertThatThrownBy(() -> client.fetch("tok"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.OAUTH_VERIFICATION_FAILED);
    }

    @Test
    @DisplayName("구글: 우리 앱에 발급된 토큰(aud 일치)은 통과한다")
    void acceptsGoogleTokenIssuedToUs() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(GOOGLE_TOKENINFO))
                .andRespond(withSuccess("""
                        {"aud":"our-app.apps.googleusercontent.com","sub":"g-123"}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(GOOGLE_USERINFO))
                .andRespond(withSuccess("""
                        {"sub":"g-123","email":"a@b.com","name":"홍길동"}
                        """, MediaType.APPLICATION_JSON));

        GoogleOAuthClient client = new GoogleOAuthClient(builder.build(), properties);
        OAuthUserInfo info = client.fetch("tok");

        assertThat(info.providerUserId()).isEqualTo("g-123");
        assertThat(info.email()).isEqualTo("a@b.com");
    }

    @Test
    @DisplayName("카카오: 다른 앱에 발급된 토큰(app_id 불일치)은 거부한다")
    void rejectsKakaoTokenIssuedToAnotherApp() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(KAKAO_TOKENINFO))
                .andRespond(withSuccess("""
                        {"id":999,"app_id":222222}
                        """, MediaType.APPLICATION_JSON));

        KakaoOAuthClient client = new KakaoOAuthClient(builder.build(), properties);

        assertThatThrownBy(() -> client.fetch("tok"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.OAUTH_VERIFICATION_FAILED);
    }

    @Test
    @DisplayName("카카오: 우리 앱에 발급된 토큰(app_id 일치)은 통과한다")
    void acceptsKakaoTokenIssuedToUs() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(KAKAO_TOKENINFO))
                .andRespond(withSuccess("""
                        {"id":123,"app_id":111111}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(KAKAO_USERINFO))
                .andRespond(withSuccess("""
                        {"id":123,"kakao_account":{"email":"a@b.com","profile":{"nickname":"길동"}}}
                        """, MediaType.APPLICATION_JSON));

        KakaoOAuthClient client = new KakaoOAuthClient(builder.build(), properties);
        OAuthUserInfo info = client.fetch("tok");

        assertThat(info.providerUserId()).isEqualTo("123");
        assertThat(info.nickname()).isEqualTo("길동");
    }

    @Test
    @DisplayName("식별자가 설정돼 있지 않으면 통과시키지 않고 거부한다(fail closed)")
    void rejectsWhenAudienceNotConfigured() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer.bindTo(builder).build();
        OAuthProperties unconfigured = new OAuthProperties("", "");

        GoogleOAuthClient google = new GoogleOAuthClient(builder.build(), unconfigured);

        assertThatThrownBy(() -> google.fetch("tok"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.OAUTH_VERIFICATION_FAILED);
    }
}
