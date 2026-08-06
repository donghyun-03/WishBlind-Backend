package com.example.wishBlind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 개발 단계 임시 보안 설정.
 * 아직 인증 기능이 없어 모든 요청을 허용한다.
 * 나중에 JWT/로그인 등 실제 인증을 붙일 때 이 클래스를 교체하면 된다.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
