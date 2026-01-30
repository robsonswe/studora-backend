package com.studora.controller;

import com.studora.dto.InstituicaoDto;
import com.studora.service.InstituicaoService;
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

import java.util.List;

@RestController
@RequestMapping("/api/instituicoes")
@Tag(name = "Instituicoes", description = "Endpoints para gerenciamento de instituições")
public class InstituicaoController {

    @Autowired
    private InstituicaoService instituicaoService;

    @Operation(
        summary = "Obter todas as instituições",
        description = "Retorna uma lista com todas as instituições cadastradas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de instituições retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = InstituicaoDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Polícia Federal\"}]"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/instituicoes\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<List<InstituicaoDto>> getAllInstituicoes() {
        List<InstituicaoDto> instituicoes = instituicaoService.findAll();
        return ResponseEntity.ok(instituicoes);
    }

    @Operation(
        summary = "Obter instituição por ID",
        description = "Retorna uma instituição específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Instituição encontrada e retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = InstituicaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Polícia Federal\"}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Instituição com ID: '123'\",\"instance\":\"/api/instituicoes/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/instituicoes/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<InstituicaoDto> getInstituicaoById(
            @Parameter(description = "ID da instituição a ser buscada", required = true) @PathVariable Long id) {
        InstituicaoDto instituicao = instituicaoService.findById(id);
        return ResponseEntity.ok(instituicao);
    }

    @Operation(
        summary = "Criar nova instituição",
        description = "Cria uma nova instituição com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova instituição a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InstituicaoDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova instituição criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = InstituicaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 2, \"nome\": \"Receita Federal\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/instituicoes\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma instituição com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma instituição com o nome 'Receita Federal'\",\"instance\":\"/api/instituicoes\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/instituicoes\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<InstituicaoDto> createInstituicao(
            @RequestBody InstituicaoDto instituicaoDto) {
        InstituicaoDto createdInstituicao = instituicaoService.save(instituicaoDto);
        return new ResponseEntity<>(createdInstituicao, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar instituição",
        description = "Atualiza os dados de uma instituição existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da instituição",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InstituicaoDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Instituição atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = InstituicaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Polícia Federal Atualizada\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/instituicoes/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Instituição com ID: '1'\",\"instance\":\"/api/instituicoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe uma instituição com este nome",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe uma instituição com o nome 'Polícia Federal Atualizada'\",\"instance\":\"/api/instituicoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/instituicoes/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<InstituicaoDto> updateInstituicao(
            @Parameter(description = "ID da instituição a ser atualizada", required = true) @PathVariable Long id,
            @RequestBody InstituicaoDto instituicaoDto) {
        InstituicaoDto existingInstituicao = instituicaoService.findById(id);
        existingInstituicao.setNome(instituicaoDto.getNome());
        InstituicaoDto updatedInstituicao = instituicaoService.save(existingInstituicao);
        return ResponseEntity.ok(updatedInstituicao);
    }

    @Operation(
        summary = "Excluir instituição",
        description = "Remove uma instituição existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Instituição excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Instituição não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Instituição com ID: '1'\",\"instance\":\"/api/instituicoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/instituicoes/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstituicao(
            @Parameter(description = "ID da instituição a ser excluída", required = true) @PathVariable Long id) {
        instituicaoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
