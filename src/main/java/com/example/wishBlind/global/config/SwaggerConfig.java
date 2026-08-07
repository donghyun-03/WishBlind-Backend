package com.example.wishBlind.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 문서 설정.
 * 실행 후 http://localhost:8080/swagger-ui/index.html 에서 API 목록·테스트 가능.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI wishBlindOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WishBlind API")
                        .description("위시블라인드 백엔드 API 문서")
                        .version("v0.0.1"));
    }
}
