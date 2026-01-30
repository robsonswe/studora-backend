package com.studora.controller;

import com.studora.dto.ErrorResponse;
import com.studora.dto.SubtemaDto;
import com.studora.service.SubtemaService;
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
@RequestMapping("/api/subtemas")
@CrossOrigin(origins = "*")
@Tag(name = "Subtemas", description = "Endpoints para gerenciamento de subtemas")
public class SubtemaController {

    @Autowired
    private SubtemaService subtemaService;

    @Operation(
        summary = "Obter todos os subtemas",
        description = "Retorna uma lista com todos os subtemas cadastrados",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de subtemas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = SubtemaDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Direitos Individuais\", \"temaId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping
    public ResponseEntity<List<SubtemaDto>> getAllSubtemas() {
        List<SubtemaDto> subtemas = subtemaService.getAllSubtemas();
        return ResponseEntity.ok(subtemas);
    }

    @Operation(
        summary = "Obter subtema por ID",
        description = "Retorna um subtema específico com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Subtema encontrado e retornado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = SubtemaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Direitos Individuais\", \"temaId\": 1}"
                    )
                ))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<SubtemaDto> getSubtemaById(
            @Parameter(description = "ID do subtema a ser buscado", required = true) @PathVariable Long id) {
        SubtemaDto subtema = subtemaService.getSubtemaById(id);
        return ResponseEntity.ok(subtema);
    }

    @Operation(
        summary = "Obter subtemas por ID do tema",
        description = "Retorna uma lista de subtemas associados a um tema específico",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de subtemas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = SubtemaDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Direitos Individuais\", \"temaId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping("/tema/{temaId}")
    public ResponseEntity<List<SubtemaDto>> getSubtemasByTemaId(
            @Parameter(description = "ID do tema para filtrar subtemas", required = true) @PathVariable Long temaId) {
        List<SubtemaDto> subtemas = subtemaService.getSubtemasByTemaId(temaId);
        return ResponseEntity.ok(subtemas);
    }

    @Operation(
        summary = "Criar novo subtema",
        description = "Cria um novo subtema com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo subtema a ser criado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubtemaDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Novo subtema criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = SubtemaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 2, \"nome\": \"Direitos Sociais\", \"temaId\": 1}"
                    )
                ))
        }
    )
    @PostMapping
    public ResponseEntity<SubtemaDto> createSubtema(
            @Valid @RequestBody SubtemaDto subtemaDto) {
        SubtemaDto createdSubtema = subtemaService.createSubtema(subtemaDto);
        return new ResponseEntity<>(createdSubtema, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar subtema",
        description = "Atualiza os dados de um subtema existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados do subtema",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubtemaDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Subtema atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = SubtemaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Direitos Individuais Atualizado\", \"temaId\": 1}"
                    )
                ))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<SubtemaDto> updateSubtema(
            @Parameter(description = "ID do subtema a ser atualizado", required = true) @PathVariable Long id,
            @Valid @RequestBody SubtemaDto subtemaDto) {
        SubtemaDto updatedSubtema = subtemaService.updateSubtema(id, subtemaDto);
        return ResponseEntity.ok(updatedSubtema);
    }

    @Operation(
        summary = "Excluir subtema",
        description = "Remove um subtema existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Subtema excluído com sucesso")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubtema(
            @Parameter(description = "ID do subtema a ser excluído", required = true) @PathVariable Long id) {
        subtemaService.deleteSubtema(id);
        return ResponseEntity.noContent().build();
    }
}