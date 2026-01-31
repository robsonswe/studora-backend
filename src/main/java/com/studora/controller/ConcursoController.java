package com.studora.controller;

import com.studora.dto.ConcursoCargoDto;
import com.studora.dto.ConcursoDto;
import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.dto.request.ConcursoCargoCreateRequest;
import com.studora.service.ConcursoService;
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
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"instituicaoId\": 1, \"bancaId\": 1, \"ano\": 2023}]"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/concursos\"}"
                    )))
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
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"instituicaoId\": 1, \"bancaId\": 1, \"ano\": 2023}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Concurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Concurso com ID: '123'\",\"instance\":\"/api/concursos/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/concursos/123\"}"
                    )))
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
                schema = @Schema(implementation = ConcursoCreateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Novo concurso criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ConcursoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 2, \"instituicaoId\": 2, \"bancaId\": 2, \"ano\": 2024}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/concursos\",\"errors\":{\"ano\":\"deve ser maior que 1900\"}}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Ano do concurso deve ser um valor razoável\",\"instance\":\"/api/concursos\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/concursos\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<ConcursoDto> createConcurso(
            @jakarta.validation.Valid @RequestBody ConcursoCreateRequest concursoCreateRequest) {
        // Convert the request DTO to the regular DTO for processing
        ConcursoDto concursoDto = new ConcursoDto();
        concursoDto.setInstituicaoId(concursoCreateRequest.getInstituicaoId());
        concursoDto.setBancaId(concursoCreateRequest.getBancaId());
        concursoDto.setAno(concursoCreateRequest.getAno());
        concursoDto.setMes(concursoCreateRequest.getMes());
        concursoDto.setEdital(concursoCreateRequest.getEdital());

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
                schema = @Schema(implementation = ConcursoUpdateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Concurso atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ConcursoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"instituicaoId\": 1, \"bancaId\": 1, \"ano\": 2023}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/concursos/1\",\"errors\":{\"ano\":\"deve ser maior que 1900\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Concurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Concurso com ID: '1'\",\"instance\":\"/api/concursos/1\"}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Ano do concurso deve ser um valor razoável\",\"instance\":\"/api/concursos/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/concursos/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ConcursoDto> updateConcurso(
            @Parameter(description = "ID do concurso a ser atualizado", required = true) @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody ConcursoUpdateRequest concursoUpdateRequest) {
        // Convert the request DTO to the regular DTO for processing
        ConcursoDto concursoDto = new ConcursoDto();
        concursoDto.setId(id); // Set the ID from the path parameter
        concursoDto.setInstituicaoId(concursoUpdateRequest.getInstituicaoId());
        concursoDto.setBancaId(concursoUpdateRequest.getBancaId());
        concursoDto.setAno(concursoUpdateRequest.getAno());
        concursoDto.setMes(concursoUpdateRequest.getMes());
        concursoDto.setEdital(concursoUpdateRequest.getEdital());

        ConcursoDto updatedConcurso = concursoService.save(concursoDto);
        return ResponseEntity.ok(updatedConcurso);
    }

    @Operation(
        summary = "Excluir concurso",
        description = "Remove um concurso existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Concurso excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Concurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Concurso com ID: '1'\",\"instance\":\"/api/concursos/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/concursos/1\"}"
                    )))
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
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"concursoId\": 1, \"cargoId\": 1}]"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Concurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Concurso com ID: '1'\",\"instance\":\"/api/concursos/1/cargos\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/concursos/1/cargos\"}"
                    )))
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
                schema = @Schema(implementation = ConcursoCargoCreateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Cargo adicionado ao concurso com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ConcursoCargoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 5, \"concursoId\": 1, \"cargoId\": 2}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/concursos/1/cargos\",\"errors\":{\"cargoId\":\"não deve ser nulo\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Concurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Concurso com ID: '1'\",\"instance\":\"/api/concursos/1/cargos\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/concursos/1/cargos\"}"
                    )))
        }
    )
    @PostMapping("/{id}/cargos")
    public ResponseEntity<ConcursoCargoDto> addCargoToConcurso(
            @Parameter(description = "ID do concurso ao qual o cargo será adicionado", required = true) @PathVariable Long id,
            @RequestBody ConcursoCargoCreateRequest concursoCargoCreateRequest) {
        // Convert the request DTO to the regular DTO for processing
        ConcursoCargoDto concursoCargoDto = new ConcursoCargoDto();
        concursoCargoDto.setConcursoId(id); // Use the path variable, not the request body
        concursoCargoDto.setCargoId(concursoCargoCreateRequest.getCargoId());

        ConcursoCargoDto createdConcursoCargo = concursoService.addCargoToConcurso(concursoCargoDto);
        return new ResponseEntity<>(createdConcursoCargo, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Remover cargo do concurso",
        description = "Desassocia um cargo de um concurso específico",
        responses = {
            @ApiResponse(responseCode = "204", description = "Cargo removido do concurso com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar a associação para remover.\",\"instance\":\"/api/concursos/1/cargos/1\"}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Um concurso deve estar associado a pelo menos um cargo\",\"instance\":\"/api/concursos/1/cargos/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/concursos/1/cargos/1\"}"
                    )))
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
