package com.studora.controller;

import com.studora.dto.ErrorResponse;
import com.studora.dto.InstituicaoDto;
import com.studora.service.InstituicaoService;
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
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Polícia Federal\"}]"
                    )
                ))
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
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Polícia Federal\"}"
                    )
                ))
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
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 2, \"nome\": \"Receita Federal\"}"
                    )
                ))
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
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Polícia Federal Atualizada\"}"
                    )
                ))
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
            @ApiResponse(responseCode = "204", description = "Instituição excluída com sucesso")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstituicao(
            @Parameter(description = "ID da instituição a ser excluída", required = true) @PathVariable Long id) {
        instituicaoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
