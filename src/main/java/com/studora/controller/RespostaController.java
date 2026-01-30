package com.studora.controller;

import com.studora.dto.ErrorResponse;
import com.studora.dto.RespostaDto;
import com.studora.service.RespostaService;
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
@RequestMapping("/api/respostas")
@CrossOrigin(origins = "*")
@Tag(name = "Respostas", description = "Endpoints para gerenciamento de respostas")
public class RespostaController {

    @Autowired
    private RespostaService respostaService;

    @Operation(
        summary = "Obter todas as respostas",
        description = "Retorna uma lista com todas as respostas cadastradas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de respostas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = RespostaDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"usuarioId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping
    public ResponseEntity<List<RespostaDto>> getAllRespostas() {
        List<RespostaDto> respostas = respostaService.getAllRespostas();
        return ResponseEntity.ok(respostas);
    }

    @Operation(
        summary = "Obter resposta por ID",
        description = "Retorna uma resposta específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Resposta encontrada e retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = RespostaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"usuarioId\": 1}"
                    )
                ))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<RespostaDto> getRespostaById(
            @Parameter(description = "ID da resposta a ser buscada", required = true) @PathVariable Long id) {
        RespostaDto resposta = respostaService.getRespostaById(id);
        return ResponseEntity.ok(resposta);
    }

    @Operation(
        summary = "Obter respostas por ID da questão",
        description = "Retorna uma lista de respostas associadas a uma questão específica",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de respostas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = RespostaDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"usuarioId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping("/questao/{questaoId}")
    public ResponseEntity<List<RespostaDto>> getRespostasByQuestaoId(
            @Parameter(description = "ID da questão para filtrar respostas", required = true) @PathVariable Long questaoId) {
        List<RespostaDto> respostas = respostaService.getRespostasByQuestaoId(questaoId);
        return ResponseEntity.ok(respostas);
    }

    @Operation(
        summary = "Obter respostas por ID da alternativa",
        description = "Retorna uma lista de respostas associadas a uma alternativa específica",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de respostas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = RespostaDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"usuarioId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping("/alternativa/{alternativaId}")
    public ResponseEntity<List<RespostaDto>> getRespostasByAlternativaId(
            @Parameter(description = "ID da alternativa para filtrar respostas", required = true) @PathVariable Long alternativaId) {
        List<RespostaDto> respostas = respostaService.getRespostasByAlternativaId(alternativaId);
        return ResponseEntity.ok(respostas);
    }

    @Operation(
        summary = "Criar nova resposta",
        description = "Cria uma nova resposta com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova resposta a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RespostaDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova resposta criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = RespostaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 2, \"questaoId\": 1, \"alternativaId\": 2, \"usuarioId\": 1}"
                    )
                ))
        }
    )
    @PostMapping
    public ResponseEntity<RespostaDto> createResposta(
            @Valid @RequestBody RespostaDto respostaDto) {
        RespostaDto createdResposta = respostaService.createResposta(respostaDto);
        return new ResponseEntity<>(createdResposta, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar resposta",
        description = "Atualiza os dados de uma resposta existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da resposta",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RespostaDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Resposta atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = RespostaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"usuarioId\": 1}"
                    )
                ))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<RespostaDto> updateResposta(
            @Parameter(description = "ID da resposta a ser atualizada", required = true) @PathVariable Long id,
            @Valid @RequestBody RespostaDto respostaDto) {
        RespostaDto updatedResposta = respostaService.updateResposta(id, respostaDto);
        return ResponseEntity.ok(updatedResposta);
    }

    @Operation(
        summary = "Excluir resposta",
        description = "Remove uma resposta existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Resposta excluída com sucesso")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResposta(
            @Parameter(description = "ID da resposta a ser excluída", required = true) @PathVariable Long id) {
        respostaService.deleteResposta(id);
        return ResponseEntity.noContent().build();
    }
}