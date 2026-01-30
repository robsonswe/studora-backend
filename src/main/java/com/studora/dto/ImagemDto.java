package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para representar uma imagem")
@Data
public class ImagemDto {

    @Schema(description = "ID único da imagem", example = "1")
    private Long id;

    @NotBlank(message = "URL da imagem é obrigatória")
    @Schema(description = "URL da imagem", example = "https://exemplo.com/imagem.jpg", required = true)
    private String url;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Schema(description = "Descrição da imagem", example = "Gráfico mostrando crescimento populacional")
    private String descricao;

    // Constructors
    public ImagemDto() {}

    public ImagemDto(String url, String descricao) {
        this.url = url;
        this.descricao = descricao;
    }
}