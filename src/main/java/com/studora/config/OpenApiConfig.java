package com.studora.config;

import com.studora.dto.ErrorResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer globalResponsesCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }

            // 1. Resolve and add only ErrorResponse schema to components
            ResolvedSchema errorResponseResolvedSchema = ModelConverters.getInstance()
                    .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.class));
            if (errorResponseResolvedSchema.schema != null) {
                openApi.getComponents().addSchemas("ErrorResponse", errorResponseResolvedSchema.schema);
            }

            // 2. Add reusable global error responses
            openApi.getComponents().addResponses("BadRequest", new ApiResponse()
                    .description("Dados inválidos fornecidos")
                    .content(new Content().addMediaType("application/json", new MediaType()
                            .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                            .addExamples("DadosInválidos", new Example()
                                    .value(Map.of(
                                        "statusCode", 400,
                                        "message", "Erro de validação: campo 'nome' não deve estar em branco",
                                        "timestamp", "2026-01-30T10:30:00"
                                    ))))));

            openApi.getComponents().addResponses("NotFound", new ApiResponse()
                    .description("Recurso não encontrado")
                    .content(new Content().addMediaType("application/json", new MediaType()
                            .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                            .addExamples("RecursoNãoEncontrado", new Example()
                                    .value(Map.of(
                                        "statusCode", 404,
                                        "message", "Recurso não encontrado com o ID fornecido",
                                        "timestamp", "2026-01-30T10:30:00"
                                    ))))));

            openApi.getComponents().addResponses("InternalServerError", new ApiResponse()
                    .description("Erro interno do servidor")
                    .content(new Content().addMediaType("application/json", new MediaType()
                            .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                            .addExamples("ErroServidor", new Example()
                                    .value(Map.of(
                                        "statusCode", 500,
                                        "message", "Ocorreu um erro interno no servidor",
                                        "timestamp", "2026-01-30T10:30:00"
                                    ))))));

            // 3. Apply error responses to all operations
            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                var responses = operation.getResponses();
                if (!responses.containsKey("400")) {
                    responses.addApiResponse("400", new ApiResponse().$ref("#/components/responses/BadRequest"));
                }
                if (!responses.containsKey("404")) {
                    responses.addApiResponse("404", new ApiResponse().$ref("#/components/responses/NotFound"));
                }
                if (!responses.containsKey("500")) {
                    responses.addApiResponse("500", new ApiResponse().$ref("#/components/responses/InternalServerError"));
                }
            }));
        };
    }
}
