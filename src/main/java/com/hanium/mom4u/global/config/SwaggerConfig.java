package com.hanium.mom4u.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${openapi.base-url}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme jwtScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("JWT", jwtScheme))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .info(new Info()
                        .title("Dear Belly")
                        .version("v1.0")
                        .description("Dear Belly 백엔드 API 문서입니다."))
                .addServersItem(new Server().url(baseUrl));
    }
}
