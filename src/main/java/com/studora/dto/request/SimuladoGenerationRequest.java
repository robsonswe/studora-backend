package com.studora.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Schema(description = "Request DTO para geração de um simulado com quantidades específicas por item")
@Data
public class SimuladoGenerationRequest {

    @NotBlank(message = "O nome do simulado é obrigatório")
    @Schema(description = "Nome do simulado", example = "Simulado Geral 2024")
    private String nome;

    @Schema(description = "Ano mínimo (inclusivo). Questões de concursos deste ano até o ano atual. Se maior que o ano atual, o filtro é ignorado.", example = "2023")
    private Integer ano;

    @Schema(description = "ID da banca de preferência", example = "1")
    private Long bancaId;

    @Schema(description = "ID do cargo de preferência", example = "10")
    private Long cargoId;

    @Schema(description = "Lista de áreas de preferência (Cargo ou Instituição)", example = "[\"Jurídica\", \"Administrativa\"]")
    private List<String> areas;

    @Schema(description = "Nível do cargo (Superior, Médio, Fundamental). Define o teto da hierarquia.", example = "SUPERIOR")
    private com.studora.entity.NivelCargo nivel;

    @Schema(description = "Se verdadeiro, ignora questões que o usuário já respondeu. Padrão: false (inclui todas).", defaultValue = "false")
    private Boolean ignorarRespondidas = false;

    @Valid
    @Schema(description = "Seleção de questões por Disciplina")
    private List<ItemSelection> disciplinas;

    @Valid
    @Schema(description = "Seleção de questões por Tema")
    private List<ItemSelection> temas;

    @Valid
    @Schema(description = "Seleção de questões por Subtema")
    private List<ItemSelection> subtemas;

    @Data
    public static class ItemSelection {
        @NotNull(message = "O ID é obrigatório")
        @Schema(description = "ID do item (Disciplina, Tema ou Subtema)", example = "1")
        private Long id;

        @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
        @Schema(description = "Quantidade de questões desejadas para este ID", example = "10")
        private int quantidade;
    }
}
