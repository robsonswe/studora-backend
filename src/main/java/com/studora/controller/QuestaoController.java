package com.studora.controller;

import com.studora.dto.QuestaoCargoDto;
import com.studora.dto.QuestaoDto;
import com.studora.dto.ErrorResponse;
import com.studora.service.QuestaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/questoes")
@CrossOrigin(origins = "*")
@Tag(name = "Questoes", description = "Endpoints para gerenciamento de questões")
public class QuestaoController {

    @Autowired
    private QuestaoService questaoService;

    @Operation(
        summary = "Obter todas as questões",
        description = "Retorna uma lista com todas as questões cadastradas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de questões retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"enunciado\": \"Qual a capital do Brasil?\", \"concursoId\": 1, \"subtemaId\": 1, \"anulada\": false}]"
                    )
                ))
        }
    )
    @GetMapping
    public ResponseEntity<List<QuestaoDto>> getAllQuestoes() {
        List<QuestaoDto> questoes = questaoService.getAllQuestoes();
        return ResponseEntity.ok(questoes);
    }

    @Operation(
        summary = "Obter questão por ID",
        description = "Retorna uma questão específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Questão encontrada e retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"enunciado\": \"Qual a capital do Brasil?\", \"concursoId\": 1, \"subtemaId\": 1, \"anulada\": false}"
                    )
                ))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<QuestaoDto> getQuestaoById(
            @Parameter(description = "ID da questão a ser buscada", required = true) @PathVariable Long id) {
        QuestaoDto questao = questaoService.getQuestaoById(id);
        return ResponseEntity.ok(questao);
    }

    @Operation(
        summary = "Obter questões por ID do concurso",
        description = "Retorna uma lista de questões associadas a um concurso específico",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de questões retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"enunciado\": \"Qual a capital do Brasil?\", \"concursoId\": 1, \"subtemaId\": 1, \"anulada\": false}]"
                    )
                ))
        }
    )
    @GetMapping("/concurso/{concursoId}")
    public ResponseEntity<List<QuestaoDto>> getQuestoesByConcursoId(
            @Parameter(description = "ID do concurso para filtrar questões", required = true) @PathVariable Long concursoId) {
        List<QuestaoDto> questoes = questaoService.getQuestoesByConcursoId(concursoId);
        return ResponseEntity.ok(questoes);
    }

    @Operation(
        summary = "Obter questões por ID do subtema",
        description = "Retorna uma lista de questões associadas a um subtema específico",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de questões retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"enunciado\": \"Qual a capital do Brasil?\", \"concursoId\": 1, \"subtemaId\": 1, \"anulada\": false}]"
                    )
                ))
        }
    )
    @GetMapping("/subtema/{subtemaId}")
    public ResponseEntity<List<QuestaoDto>> getQuestoesBySubtemaId(
            @Parameter(description = "ID do subtema para filtrar questões", required = true) @PathVariable Long subtemaId) {
        List<QuestaoDto> questoes = questaoService.getQuestoesBySubtemaId(subtemaId);
        return ResponseEntity.ok(questoes);
    }

    @Operation(
        summary = "Obter questões não anuladas",
        description = "Retorna uma lista de questões que não foram anuladas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de questões não anuladas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"enunciado\": \"Qual a capital do Brasil?\", \"concursoId\": 1, \"subtemaId\": 1, \"anulada\": false}]"
                    )
                ))
        }
    )
    @GetMapping("/nao-anuladas")
    public ResponseEntity<List<QuestaoDto>> getQuestoesNaoAnuladas() {
        List<QuestaoDto> questoes = questaoService.getQuestoesNaoAnuladas();
        return ResponseEntity.ok(questoes);
    }

    @Operation(
        summary = "Criar nova questão",
        description = "Cria uma nova questão com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova questão a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestaoDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova questão criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 2, \"enunciado\": \"Qual a capital de Portugal?\", \"concursoId\": 1, \"subtemaId\": 1, \"anulada\": false}"
                    )
                ))
        }
    )
    @PostMapping
    public ResponseEntity<QuestaoDto> createQuestao(
            @Valid @RequestBody QuestaoDto questaoDto) {
        QuestaoDto createdQuestao = questaoService.createQuestao(questaoDto);
        return new ResponseEntity<>(createdQuestao, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar questão",
        description = "Atualiza os dados de uma questão existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da questão",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestaoDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Questão atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"enunciado\": \"Qual a capital do Brasil? (Atualizada)\", \"concursoId\": 1, \"subtemaId\": 1, \"anulada\": false}"
                    )
                ))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<QuestaoDto> updateQuestao(
            @Parameter(description = "ID da questão a ser atualizada", required = true) @PathVariable Long id,
            @Valid @RequestBody QuestaoDto questaoDto) {
        QuestaoDto updatedQuestao = questaoService.updateQuestao(id, questaoDto);
        return ResponseEntity.ok(updatedQuestao);
    }

    @Operation(
        summary = "Excluir questão",
        description = "Remove uma questão existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Questão excluída com sucesso")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestao(
            @Parameter(description = "ID da questão a ser excluída", required = true) @PathVariable Long id) {
        questaoService.deleteQuestao(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints for managing cargo associations
    @Operation(
        summary = "Obter cargos por questão",
        description = "Retorna uma lista de cargos associados a uma questão específica",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de cargos retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoCargoDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"questaoId\": 1, \"concursoCargoId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping("/{id}/cargos")
    public ResponseEntity<List<QuestaoCargoDto>> getCargosByQuestao(
            @Parameter(description = "ID da questão para filtrar cargos", required = true) @PathVariable Long id) {
        List<QuestaoCargoDto> cargos = questaoService.getCargosByQuestaoId(id);
        return ResponseEntity.ok(cargos);
    }

    @Operation(
        summary = "Adicionar cargo à questão",
        description = "Associa um cargo existente a uma questão específica",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do cargo a ser adicionado à questão",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestaoCargoDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Cargo adicionado à questão com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoCargoDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 2, \"questaoId\": 1, \"concursoCargoId\": 2}"
                    )
                ))
        }
    )
    @PostMapping("/{id}/cargos")
    public ResponseEntity<QuestaoCargoDto> addCargoToQuestao(
            @Parameter(description = "ID da questão à qual o cargo será adicionado", required = true) @PathVariable Long id,
            @RequestBody QuestaoCargoDto questaoCargoDto) {
        questaoCargoDto.setQuestaoId(id); // Ensure the questao ID matches the path variable
        QuestaoCargoDto createdQuestaoCargo = questaoService.addCargoToQuestao(questaoCargoDto);
        return new ResponseEntity<>(createdQuestaoCargo, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Remover cargo da questão",
        description = "Desassocia um cargo de uma questão específica",
        responses = {
            @ApiResponse(responseCode = "204", description = "Cargo removido da questão com sucesso")
        }
    )
    @DeleteMapping("/{questaoId}/cargos/{concursoCargoId}")
    public ResponseEntity<Void> removeCargoFromQuestao(
            @Parameter(description = "ID da questão da qual o cargo será removido", required = true) @PathVariable Long questaoId,
            @Parameter(description = "ID do cargo a ser removido da questão", required = true) @PathVariable Long concursoCargoId) {
        questaoService.removeCargoFromQuestao(questaoId, concursoCargoId);
        return ResponseEntity.noContent().build();
    }
}