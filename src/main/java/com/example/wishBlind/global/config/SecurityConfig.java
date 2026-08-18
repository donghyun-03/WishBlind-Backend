package com.example.wishBlind.global.config;

import com.example.wishBlind.auth.jwt.JwtAuthenticationFilter;
import com.example.wishBlind.global.common.ApiResponse;
import com.example.wishBlind.global.exception.ErrorCode;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * JWT 기반 인증 설정.
 *
 * ★ 화이트리스트를 건드릴 때 주의할 것 ★
 * /api/invite/** 는 반드시 열려 있어야 한다. 수령자는 비회원 상태로 초대 링크를
 * 열고 취향 테스트를 제출하기 때문에, 이 경로가 막히면 서비스 핵심 플로우가 통째로 죽는다.
 * SecurityWhitelistTest가 이 사실을 못박고 있으니 규칙을 바꾸면 그 테스트도 함께 볼 것.
 *
 * 규칙은 위에서부터 순서대로 매칭된다. /api/auth/social/link 는 로그인 상태에서만
 * 호출할 수 있어야 하므로 /api/auth/** permitAll 보다 먼저 선언한다.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // CorsConfig의 CorsConfigurationSource 사용
                .csrf(csrf -> csrf.disable())    // 토큰 인증이라 세션 CSRF가 필요 없다
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handler ->
                        handler.authenticationEntryPoint(unauthorizedEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        // 에러/포워드 디스패치는 인가 대상에서 뺀다.
                        // 빼지 않으면 permitAll 경로에서 404·500이 났을 때 /error 포워드가
                        // 다시 인가를 타면서 401로 둔갑한다 (원래 상태코드가 사라진다).
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()

                        // 인증이 필요한 예외 경로를 먼저 선언 (아래 permitAll보다 우선)
                        .requestMatchers(HttpMethod.POST, "/api/auth/social/link").authenticated()

                        .requestMatchers("/api/auth/**").permitAll()

                        // 수령자(비회원) 경로 — 절대 막지 말 것.
                        //   GET  /api/invite/{token}              초대 확인
                        //   POST /api/invite/verify               초대 코드 검증
                        //   GET  /api/invite/{token}/taste-form   취향 테스트 문항
                        //   POST /api/invite/{token}/preferences  취향 제출
                        //   GET  /api/invite/{token}/reveal       수령 후 공개
                        .requestMatchers("/api/invite/**").permitAll()

                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 인증 없이 보호된 경로에 접근했을 때도 ApiResponse 형태를 유지한다. */
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
            response.setStatus(errorCode.getStatus().value());
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            objectMapper.writeValue(
                    response.getWriter(),
                    ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
        };
    }
}
