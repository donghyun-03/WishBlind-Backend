package com.example.wishBlind.global.config;

import com.example.wishBlind.auth.api.AuthController;
import com.example.wishBlind.auth.api.MeController;
import com.example.wishBlind.auth.application.AuthService;
import com.example.wishBlind.auth.application.TokenService;
import com.example.wishBlind.auth.jwt.JwtProperties;
import com.example.wishBlind.auth.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 수령자(비회원) 경로가 인증에 막히지 않는지 확인한다.
 * 슬라이스를 인증 컨트롤러로 좁혀 다른 컨트롤러가 늘어나도 영향받지 않게 한다.
 */
@WebMvcTest(controllers = {AuthController.class, MeController.class})
// @WebMvcTest 슬라이스는 @Configuration을 스캔하지 않는다. 명시적으로 넣지 않으면
// Boot 기본 시큐리티가 응답해서 "내 규칙"이 아니라 기본값을 테스트하게 된다.
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-hs256-at-least-32-bytes",
        "jwt.access-token-validity=1800",
        "jwt.refresh-token-validity=1209600"
})
class SecurityWhitelistTest {

    @Autowired
    private MockMvc mockMvc;

    // 컨트롤러가 의존하는 서비스는 이 테스트의 관심사가 아니다 (DB를 태우지 않기 위해 대체)
    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenService tokenService;

    @TestConfiguration
    static class JwtTestConfig {
        @Bean
        JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
            return new JwtTokenProvider(properties);
        }

    }

    @Test
    @DisplayName("수령자 초대 확인 경로는 토큰 없이도 인증에 막히지 않는다")
    void invitePathIsOpenToAnonymous() throws Exception {
        mockMvc.perform(get("/api/invite/some-token"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("수령자 취향 제출 경로는 토큰 없이도 인증에 막히지 않는다")
    void preferenceSubmitIsOpenToAnonymous() throws Exception {
        mockMvc.perform(post("/api/invite/some-token/preferences"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("수령자 공개(reveal) 경로는 토큰 없이도 인증에 막히지 않는다")
    void revealIsOpenToAnonymous() throws Exception {
        mockMvc.perform(get("/api/invite/some-token/reveal"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("로그인/회원가입 경로는 토큰 없이 열려 있다")
    void authPathsAreOpenToAnonymous() throws Exception {
        mockMvc.perform(post("/api/auth/login"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("보호된 경로는 토큰 없이 접근하면 401이다")
    void protectedPathRequiresToken() throws Exception {
        mockMvc.perform(get("/api/gift-sessions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("소셜 계정 연결은 화이트리스트(/api/auth/**)보다 우선해 인증을 요구한다")
    void socialLinkRequiresToken() throws Exception {
        mockMvc.perform(post("/api/auth/social/link"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("잘못된 토큰을 들고 오면 401이다")
    void invalidTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/me").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    /** MockMvc의 status().is(matcher)에 넣을 "이 코드가 아님" 매처. */
    private static org.hamcrest.Matcher<Integer> not(int statusCode) {
        return org.hamcrest.Matchers.not(org.hamcrest.Matchers.equalTo(statusCode));
    }
}
