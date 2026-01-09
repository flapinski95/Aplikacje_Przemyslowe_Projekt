package com.booklovers.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookLoversOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Book Lovers API")
                        .description("API do zarządzania domową biblioteczką")
                        .version("1.0"));
    }
}