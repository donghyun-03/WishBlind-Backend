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

/**
 * ★ 이 테스트가 막는 것: 토큰 치환(token substitution) 공격.
 *
 * 카카오/구글의 access token은 "누가 발급했는지"만으로는 우리 앱 것인지 알 수 없다.
 * 공격자가 자기 앱에서 받은 토큰을 우리 서버에 넘기면, 검증이 없을 경우
 * 그 토큰 주인의 계정으로 로그인이 된다.
 * 그래서 토큰의 발급 대상(구글 aud / 카카오 app_id)이 우리 앱인지 반드시 확인해야 한다.
 */
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
