package com.studora.controller;

import com.studora.dto.ErrorResponse;
import com.studora.dto.TemaDto;
import com.studora.service.TemaService;
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
@RequestMapping("/api/temas")
@CrossOrigin(origins = "*")
@Tag(name = "Temas", description = "Endpoints para gerenciamento de temas")
public class TemaController {

    @Autowired
    private TemaService temaService;

    @Operation(
        summary = "Obter todas as temas",
        description = "Retorna uma lista com todas as temas cadastrados",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de temas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = TemaDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping
    public ResponseEntity<List<TemaDto>> getAllTemas() {
        List<TemaDto> temas = temaService.getAllTemas();
        return ResponseEntity.ok(temas);
    }

    @Operation(
        summary = "Obter tema por ID",
        description = "Retorna um tema específico com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tema encontrado e retornado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = TemaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1}"
                    )
                ))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<TemaDto> getTemaById(
            @Parameter(description = "ID do tema a ser buscada", required = true) @PathVariable Long id) {
        TemaDto tema = temaService.getTemaById(id);
        return ResponseEntity.ok(tema);
    }

    @Operation(
        summary = "Obter temas por ID da disciplina",
        description = "Retorna uma lista de temas associados a uma disciplina específica",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de temas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = TemaDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<TemaDto>> getTemasByDisciplinaId(
            @Parameter(description = "ID da disciplina para filtrar temas", required = true) @PathVariable Long disciplinaId) {
        List<TemaDto> temas = temaService.getTemasByDisciplinaId(disciplinaId);
        return ResponseEntity.ok(temas);
    }

    @Operation(
        summary = "Criar novo tema",
        description = "Cria um novo tema com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo tema a ser criado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TemaDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Novo tema criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = TemaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 2, \"nome\": \"Organização do Estado\", \"disciplinaId\": 1}"
                    )
                ))
        }
    )
    @PostMapping
    public ResponseEntity<TemaDto> createTema(
            @Valid @RequestBody TemaDto temaDto) {
        TemaDto createdTema = temaService.createTema(temaDto);
        return new ResponseEntity<>(createdTema, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar tema",
        description = "Atualiza os dados de um tema existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados do tema",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TemaDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Tema atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = TemaDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Direitos Fundamentais Atualizado\", \"disciplinaId\": 1}"
                    )
                ))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<TemaDto> updateTema(
            @Parameter(description = "ID do tema a ser atualizado", required = true) @PathVariable Long id,
            @Valid @RequestBody TemaDto temaDto) {
        TemaDto updatedTema = temaService.updateTema(id, temaDto);
        return ResponseEntity.ok(updatedTema);
    }

    @Operation(
        summary = "Excluir tema",
        description = "Remove um tema existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Tema excluído com sucesso")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTema(
            @Parameter(description = "ID do tema a ser excluído", required = true) @PathVariable Long id) {
        temaService.deleteTema(id);
        return ResponseEntity.noContent().build();
    }
}