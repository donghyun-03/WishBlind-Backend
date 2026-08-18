package com.example.wishBlind.auth.config;

import com.example.wishBlind.auth.jwt.AuthUserArgumentResolver;
import com.example.wishBlind.auth.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/** {@code @AuthUser} 리졸버 등록. global/config를 건드리지 않으려고 auth 쪽에 둔다. */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, OAuthProperties.class})
public class AuthWebMvcConfig implements WebMvcConfigurer {

    private final AuthUserArgumentResolver authUserArgumentResolver;

    public AuthWebMvcConfig(AuthUserArgumentResolver authUserArgumentResolver) {
        this.authUserArgumentResolver = authUserArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserArgumentResolver);
    }
}
