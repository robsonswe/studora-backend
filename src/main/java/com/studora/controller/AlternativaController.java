package com.studora.controller;

import com.studora.dto.AlternativaDto;
import com.studora.dto.ErrorResponse;
import com.studora.service.AlternativaService;
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
@RequestMapping("/api/alternativas")
@CrossOrigin(origins = "*")
@Tag(name = "Alternativas", description = "Endpoints para gerenciamento de alternativas de questões")
public class AlternativaController {
    
    @Autowired
    private AlternativaService alternativaService;

    @Operation(
        summary = "Obter todas as alternativas",
        description = "Retorna uma lista com todas as alternativas cadastradas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de alternativas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = AlternativaDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"texto\": \"Alternativa A\", \"correta\": true, \"questaoId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping
    public ResponseEntity<List<AlternativaDto>> getAllAlternativas() {
        List<AlternativaDto> alternativas = alternativaService.getAllAlternativas();
        return ResponseEntity.ok(alternativas);
    }

    @Operation(
        summary = "Obter alternativa por ID",
        description = "Retorna uma alternativa específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Alternativa encontrada e retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = AlternativaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"texto\": \"Alternativa A\", \"correta\": true, \"questaoId\": 1}"
                    )
                ))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AlternativaDto> getAlternativaById(
            @Parameter(description = "ID da alternativa a ser buscada", required = true) @PathVariable Long id) {
        AlternativaDto alternativa = alternativaService.getAlternativaById(id);
        return ResponseEntity.ok(alternativa);
    }

    @Operation(
        summary = "Obter alternativas por ID da questão",
        description = "Retorna uma lista de alternativas associadas a uma questão específica",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de alternativas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = AlternativaDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"texto\": \"Alternativa A\", \"correta\": true, \"questaoId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping("/questao/{questaoId}")
    public ResponseEntity<List<AlternativaDto>> getAlternativasByQuestaoId(
            @Parameter(description = "ID da questão para filtrar alternativas", required = true) @PathVariable Long questaoId) {
        List<AlternativaDto> alternativas = alternativaService.getAlternativasByQuestaoId(questaoId);
        return ResponseEntity.ok(alternativas);
    }

    @Operation(
        summary = "Obter alternativas corretas por ID da questão",
        description = "Retorna uma lista de alternativas corretas associadas a uma questão específica",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de alternativas corretas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = AlternativaDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"texto\": \"Alternativa A\", \"correta\": true, \"questaoId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping("/questao/{questaoId}/corretas")
    public ResponseEntity<List<AlternativaDto>> getAlternativasCorretasByQuestaoId(
            @Parameter(description = "ID da questão para filtrar alternativas corretas", required = true) @PathVariable Long questaoId) {
        List<AlternativaDto> alternativas = alternativaService.getAlternativasCorretasByQuestaoId(questaoId);
        return ResponseEntity.ok(alternativas);
    }

    @Operation(
        summary = "Criar nova alternativa",
        description = "Cria uma nova alternativa com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova alternativa a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AlternativaDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova alternativa criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = AlternativaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 2, \"texto\": \"Alternativa B\", \"correta\": false, \"questaoId\": 1}"
                    )
                ))
        }
    )
    @PostMapping
    public ResponseEntity<AlternativaDto> createAlternativa(
            @Valid @RequestBody AlternativaDto alternativaDto) {
        AlternativaDto createdAlternativa = alternativaService.createAlternativa(alternativaDto);
        return new ResponseEntity<>(createdAlternativa, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar alternativa",
        description = "Atualiza os dados de uma alternativa existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da alternativa",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AlternativaDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Alternativa atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = AlternativaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"texto\": \"Alternativa A Atualizada\", \"correta\": true, \"questaoId\": 1}"
                    )
                ))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<AlternativaDto> updateAlternativa(
            @Parameter(description = "ID da alternativa a ser atualizada", required = true) @PathVariable Long id,
            @Valid @RequestBody AlternativaDto alternativaDto) {
        AlternativaDto updatedAlternativa = alternativaService.updateAlternativa(id, alternativaDto);
        return ResponseEntity.ok(updatedAlternativa);
    }

    @Operation(
        summary = "Excluir alternativa",
        description = "Remove uma alternativa existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Alternativa excluída com sucesso")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlternativa(
            @Parameter(description = "ID da alternativa a ser excluída", required = true) @PathVariable Long id) {
        alternativaService.deleteAlternativa(id);
        return ResponseEntity.noContent().build();
    }
}