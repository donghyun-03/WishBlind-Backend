package com.example.wishBlind.global.exception;

import com.example.wishBlind.auth.api.AuthController;
import com.example.wishBlind.auth.application.AuthService;
import com.example.wishBlind.auth.application.TokenService;
import com.example.wishBlind.auth.jwt.JwtProperties;
import com.example.wishBlind.auth.jwt.JwtTokenProvider;
import com.example.wishBlind.global.config.SecurityConfig;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 없는 경로와 잘못된 메서드가 catch-all에 걸려 500으로 나가지 않는지 확인한다.
 * 보호 경로는 인가가 먼저 걸려 401이 되므로 열린 경로로 확인한다.
 */
@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-hs256-at-least-32-bytes",
        "jwt.access-token-validity=1800",
        "jwt.refresh-token-validity=1209600"
})
class NotFoundResponseTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("열린 경로 아래 없는 URL은 500이 아니라 404 C003으로 응답한다")
    void unknownPathReturns404() throws Exception {
        mockMvc.perform(get("/api/auth/this-path-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("허용되지 않은 메서드는 500이 아니라 405 C004로 응답한다")
    void wrongMethodReturns405() throws Exception {
        mockMvc.perform(patch("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.METHOD_NOT_ALLOWED.getCode()));
    }
}
