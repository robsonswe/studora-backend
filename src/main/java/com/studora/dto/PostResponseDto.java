package com.studora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard response for POST operations")
public class PostResponseDto {

    @Schema(description = "ID of the created resource", example = "1")
    private Long id;

    @Schema(description = "Success message", example = "Recurso criado com sucesso")
    private String message;
}
