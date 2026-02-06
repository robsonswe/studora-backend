package com.studora.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("Functional API (v1)")
                .packagesToScan("com.studora.controller.v1")
                .build();
    }

    @Bean
    public GroupedOpenApi operationalApi() {
        return GroupedOpenApi.builder()
                .group("Operational")
                .packagesToScan("com.studora.controller.operational")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Studora API")
                        .version("1.0.0")
                        .description("API para plataforma de estudos para concursos públicos"));
    }
}
