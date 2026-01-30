package com.studora.controller;

import com.studora.dto.ConcursoCargoDto;
import com.studora.dto.ConcursoDto;
import com.studora.dto.ErrorResponse;
import com.studora.service.ConcursoService;
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
@RequestMapping("/api/concursos")
@Tag(name = "Concursos", description = "Endpoints para gerenciamento de concursos")
public class ConcursoController {

    @Autowired
    private ConcursoService concursoService;

    @Operation(
        summary = "Obter todas as concursos",
        description = "Retorna uma lista com todas as concursos cadastrados",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de concursos retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = ConcursoDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"instituicaoId\": 1, \"bancaId\": 1, \"ano\": 2023}]"
                    )
                ))
        }
    )
    @GetMapping
    public ResponseEntity<List<ConcursoDto>> getAllConcursos() {
        List<ConcursoDto> concursos = concursoService.findAll();
        return ResponseEntity.ok(concursos);
    }

    @Operation(
        summary = "Obter concurso por ID",
        description = "Retorna um concurso específico com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Concurso encontrado e retornado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ConcursoDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"instituicaoId\": 1, \"bancaId\": 1, \"ano\": 2023}"
                    )
                ))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ConcursoDto> getConcursoById(
            @Parameter(description = "ID do concurso a ser buscado", required = true) @PathVariable Long id) {
        ConcursoDto concurso = concursoService.findById(id);
        return ResponseEntity.ok(concurso);
    }

    @Operation(
        summary = "Criar novo concurso",
        description = "Cria um novo concurso com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo concurso a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConcursoDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Novo concurso criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ConcursoDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 2, \"instituicaoId\": 2, \"bancaId\": 2, \"ano\": 2024}"
                    )
                ))
        }
    )
    @PostMapping
    public ResponseEntity<ConcursoDto> createConcurso(
            @RequestBody ConcursoDto concursoDto) {
        ConcursoDto createdConcurso = concursoService.save(concursoDto);
        return new ResponseEntity<>(createdConcurso, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar concurso",
        description = "Atualiza os dados de um concurso existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados do concurso",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConcursoDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Concurso atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ConcursoDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 1, \"instituicaoId\": 1, \"bancaId\": 1, \"ano\": 2023}"
                    )
                ))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ConcursoDto> updateConcurso(
            @Parameter(description = "ID do concurso a ser atualizado", required = true) @PathVariable Long id,
            @RequestBody ConcursoDto concursoDto) {
        concursoDto.setId(id);
        ConcursoDto updatedConcurso = concursoService.save(concursoDto);
        return ResponseEntity.ok(updatedConcurso);
    }

    @Operation(
        summary = "Excluir concurso",
        description = "Remove um concurso existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Concurso excluído com sucesso")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConcurso(
            @Parameter(description = "ID do concurso a ser excluído", required = true) @PathVariable Long id) {
        concursoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints for managing cargo associations
    @Operation(
        summary = "Obter cargos por concurso",
        description = "Retorna uma lista de cargos associados a um concurso específico",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de cargos retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = ConcursoCargoDto.class)),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "[{\"id\": 1, \"concursoId\": 1, \"cargoId\": 1}]"
                    )
                ))
        }
    )
    @GetMapping("/{id}/cargos")
    public ResponseEntity<List<ConcursoCargoDto>> getCargosByConcurso(
            @Parameter(description = "ID do concurso para filtrar cargos", required = true) @PathVariable Long id) {
        List<ConcursoCargoDto> cargos = concursoService.getCargosByConcursoId(id);
        return ResponseEntity.ok(cargos);
    }

    @Operation(
        summary = "Adicionar cargo ao concurso",
        description = "Associa um cargo existente a um concurso específico",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do cargo a ser adicionado ao concurso",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConcursoCargoDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Cargo adicionado ao concurso com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ConcursoCargoDto.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = "{\"id\": 5, \"concursoId\": 1, \"cargoId\": 2}"
                    )
                ))
        }
    )
    @PostMapping("/{id}/cargos")
    public ResponseEntity<ConcursoCargoDto> addCargoToConcurso(
            @Parameter(description = "ID do concurso ao qual o cargo será adicionado", required = true) @PathVariable Long id,
            @RequestBody ConcursoCargoDto concursoCargoDto) {
        concursoCargoDto.setConcursoId(id); // Ensure the concurso ID matches the path variable
        ConcursoCargoDto createdConcursoCargo = concursoService.addCargoToConcurso(concursoCargoDto);
        return new ResponseEntity<>(createdConcursoCargo, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Remover cargo do concurso",
        description = "Desassocia um cargo de um concurso específico",
        responses = {
            @ApiResponse(responseCode = "204", description = "Cargo removido do concurso com sucesso")
        }
    )
    @DeleteMapping("/{concursoId}/cargos/{cargoId}")
    public ResponseEntity<Void> removeCargoFromConcurso(
            @Parameter(description = "ID do concurso do qual o cargo será removido", required = true) @PathVariable Long concursoId,
            @Parameter(description = "ID do cargo a ser removido do concurso", required = true) @PathVariable Long cargoId) {
        concursoService.removeCargoFromConcurso(concursoId, cargoId);
        return ResponseEntity.noContent().build();
    }
}
