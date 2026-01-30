package com.studora.controller;

import com.studora.dto.DisciplinaDto;
import com.studora.service.DisciplinaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/disciplinas")
@CrossOrigin(origins = "*")
@Tag(name = "Disciplinas", description = "Endpoints para gerenciamento de disciplinas")
public class DisciplinaController {

    @Autowired
    private DisciplinaService disciplinaService;

    @Operation(
        summary = "Obter todas as disciplinas",
        description = "Retorna uma lista com todas as disciplinas cadastradas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de disciplinas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = DisciplinaDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Direito Constitucional\"}, {\"id\": 2, \"nome\": \"Língua Portuguesa\"}]"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/disciplinas\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<List<DisciplinaDto>> getAllDisciplinas() {
        List<DisciplinaDto> disciplinas = disciplinaService.getAllDisciplinas();
        return ResponseEntity.ok(disciplinas);
    }

    @Operation(
        summary = "Obter disciplina por ID",
        description = "Retorna uma disciplina específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Disciplina encontrada e retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = DisciplinaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Direito Constitucional\"}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Disciplina não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Disciplina com ID: '123'\",\"instance\":\"/api/disciplinas/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/disciplinas/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<DisciplinaDto> getDisciplinaById(
            @Parameter(description = "ID da disciplina a ser buscada", required = true) @PathVariable Long id) {
        DisciplinaDto disciplina = disciplinaService.getDisciplinaById(id);
        return ResponseEntity.ok(disciplina);
    }

    @Operation(
        summary = "Criar nova disciplina",
        description = "Cria uma nova disciplina com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova disciplina a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DisciplinaDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova disciplina criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = DisciplinaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 3, \"nome\": \"Direito Administrativo\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/disciplinas\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/disciplinas\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<DisciplinaDto> createDisciplina(
            @Valid @RequestBody DisciplinaDto disciplinaDto) {
        DisciplinaDto createdDisciplina = disciplinaService.createDisciplina(disciplinaDto);
        return new ResponseEntity<>(createdDisciplina, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar disciplina",
        description = "Atualiza os dados de uma disciplina existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da disciplina",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DisciplinaDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Disciplina atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = DisciplinaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Direito Constitucional Aplicado\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/disciplinas/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Disciplina não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Disciplina com ID: '1'\",\"instance\":\"/api/disciplinas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/disciplinas/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<DisciplinaDto> updateDisciplina(
            @Parameter(description = "ID da disciplina a ser atualizada", required = true) @PathVariable Long id,
            @Valid @RequestBody DisciplinaDto disciplinaDto) {
        DisciplinaDto updatedDisciplina = disciplinaService.updateDisciplina(id, disciplinaDto);
        return ResponseEntity.ok(updatedDisciplina);
    }

    @Operation(
        summary = "Excluir disciplina",
        description = "Remove uma disciplina existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Disciplina excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Disciplina não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Disciplina com ID: '1'\",\"instance\":\"/api/disciplinas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/disciplinas/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisciplina(
            @Parameter(description = "ID da disciplina a ser excluída", required = true) @PathVariable Long id) {
        disciplinaService.deleteDisciplina(id);
        return ResponseEntity.noContent().build();
    }
}
