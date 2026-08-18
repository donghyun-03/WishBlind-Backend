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
 * 규칙은 선언 순서대로 매칭된다. /api/invite/** 는 비회원 수령자가 쓰는 경로라 열어둔다.
 * 규칙을 바꾸면 SecurityWhitelistTest도 함께 확인할 것.
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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handler ->
                        handler.authenticationEntryPoint(unauthorizedEntryPoint()))
                .authorizeHttpRequests(auth -> auth
                        // 빼지 않으면 permitAll 경로의 404·500이 /error 포워드에서 401로 바뀐다
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()

                        // /api/auth/** permitAll보다 먼저 선언해야 인증이 걸린다
                        .requestMatchers(HttpMethod.POST, "/api/auth/social/link").authenticated()

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/invite/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 인증 실패 응답도 ApiResponse 형태를 유지한다. */
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
