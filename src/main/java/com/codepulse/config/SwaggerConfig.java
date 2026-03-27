package com.codepulse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CodePulse API")
                        .version("1.0.0")
                        .description("Real-time Code Quality Dashboard API")
                        .contact(new Contact()
                                .name("CodePulse")
                                .email("codepulse@example.com")));
    }
}
