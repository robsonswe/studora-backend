package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import com.studora.common.constants.AppConstants;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request DTO para atualização de uma instituição")
@Data
public class InstituicaoUpdateRequest {

    @NotBlank(message = "Nome da instituição é obrigatório")
    @Size(max = AppConstants.MAX_NAME_LENGTH, message = "Nome da instituição deve ter no máximo " + AppConstants.MAX_NAME_LENGTH + " caracteres")
    @Schema(description = "Nome da instituição", example = "Universidade Federal do Rio de Janeiro", required = true)
    private String nome;

    @NotBlank(message = "Área da instituição é obrigatória")
    @Schema(description = "Área da instituição", example = "Educação", required = true)
    private String area;

    // Constructors
    public InstituicaoUpdateRequest() {}

    public InstituicaoUpdateRequest(String nome, String area) {
        this.nome = nome;
        this.area = area;
    }
}
