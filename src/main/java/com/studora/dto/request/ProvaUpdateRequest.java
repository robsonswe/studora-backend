package com.studora.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ProvaUpdateRequest {
    private Long id; // ID is optional for new items
    
    @NotBlank(message = "Nome é obrigatório")
    private String nome;
    private Long cargoId;
    private List<ProvaSecaoUpdateRequest> secoes;
}