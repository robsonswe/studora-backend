package com.studora.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .servers(List.of(new Server().url("/api/v1").description("Servidor Local - v1")))
                .info(new Info()
                        .title("Studora API")
                        .version("1.0.0")
                        .description("API para plataforma de estudos focada em bancos de questões de concursos públicos. " +
                                     "Organizada em taxonomia hierárquica (Disciplina -> Tema -> Subtema).")
                        .contact(new Contact()
                                .name("Suporte Studora")
                                .email("suporte@studora.com.br"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .tags(List.of(
                        new Tag().name("Simulados").description("Geração e gerenciamento de simulados personalizados"),
                        new Tag().name("Questões").description("Gerenciamento de banco de questões"),
                        new Tag().name("Taxonomia").description("Gestão da hierarquia de Disciplinas, Temas e Subtemas"),
                        new Tag().name("Estrutura").description("Gestão de Bancas, Instituições, Concursos e Cargos"),
                        new Tag().name("Operacional").description("Endpoints de saúde e monitoramento")
                ));
    }
}
