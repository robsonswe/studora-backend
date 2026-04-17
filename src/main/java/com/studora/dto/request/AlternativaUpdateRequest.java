package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Schema(description = "Request DTO para atualização de uma alternativa")
@Data
public class AlternativaUpdateRequest implements AlternativaBaseRequest {

    @Schema(description = "ID da alternativa", example = "1")
    private Long id;

    @Schema(description = "Ordem da alternativa na lista", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Ordem é obrigatória")
    @Positive(message = "Ordem deve ser um número positivo")
    private Integer ordem;

    @Schema(description = "Texto da alternativa", example = "A resposta correta é a opção A", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Texto da alternativa é obrigatório")
    private String texto;

    @Schema(description = "Indica se a alternativa é a correta", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Indicação de correta é obrigatória")
    private Boolean correta;

    @Schema(description = "Justificativa da alternativa", example = "Esta é a alternativa correta porque...")
    private String justificativa;
}
