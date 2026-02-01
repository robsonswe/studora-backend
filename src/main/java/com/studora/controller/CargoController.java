package com.studora.controller;

import com.studora.dto.CargoDto;
import com.studora.dto.PageResponse;
import com.studora.dto.QuestaoDto;
import com.studora.dto.request.CargoCreateRequest;
import com.studora.dto.request.CargoUpdateRequest;
import com.studora.service.CargoService;
import com.studora.service.QuestaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cargos")
@Tag(name = "Cargos", description = "Endpoints para gerenciamento de cargos")
public class CargoController {

    @Autowired
    private CargoService cargoService;

    @Autowired
    private QuestaoService questaoService;

    @Operation(
        summary = "Obter todos os cargos",
        description = "Retorna uma página com todos os cargos cadastrados. Suporta paginação.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de cargos retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class),
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Analista Judiciário\", \"nivel\": \"Superior\", \"area\": \"Direito\"}, {\"id\": 2, \"nome\": \"Técnico Judiciário\", \"nivel\": \"Médio\", \"area\": \"Administrativa\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 2, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/cargos\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<CargoDto>> getAllCargos(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<CargoDto> cargos = cargoService.findAll(pageable);
        return ResponseEntity.ok(new PageResponse<>(cargos));
    }

    @Operation(
        summary = "Obter cargo por ID",
        description = "Retorna um cargo específico com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Cargo encontrado e retornado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = CargoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Analista Judiciário\", \"nivel\": \"Superior\", \"area\": \"Direito\"}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Cargo com ID: '123'\",\"instance\":\"/api/cargos/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/cargos/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<CargoDto> getCargoById(
            @Parameter(description = "ID do cargo a ser buscado", required = true) @PathVariable Long id) {
        CargoDto cargo = cargoService.findById(id);
        return ResponseEntity.ok(cargo);
    }

    @Operation(
        summary = "Criar novo cargo",
        description = "Cria um novo cargo com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo cargo a ser criado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CargoCreateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Novo cargo criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = CargoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 3, \"nome\": \"Delegado de Polícia\", \"nivel\": \"Superior\", \"area\": \"Policial\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/cargos\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um cargo com este nome, nível e área",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um cargo com o nome 'Delegado de Polícia', nível 'Superior' e área 'Policial'\",\"instance\":\"/api/cargos\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/cargos\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<CargoDto> createCargo(
            @Valid @RequestBody CargoCreateRequest cargoCreateRequest) {
        // Convert the request DTO to the regular DTO for processing
        CargoDto cargoDto = new CargoDto();
        cargoDto.setNome(cargoCreateRequest.getNome());
        cargoDto.setNivel(cargoCreateRequest.getNivel());
        cargoDto.setArea(cargoCreateRequest.getArea());

        CargoDto createdCargo = cargoService.save(cargoDto);
        return new ResponseEntity<>(createdCargo, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar cargo",
        description = "Atualiza os dados de um cargo existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da cargo",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CargoUpdateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Cargo atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = CargoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Analista Judiciário - Área Judiciária\", \"nivel\": \"Superior\", \"area\": \"Direito\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/cargos/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Cargo com ID: '1'\",\"instance\":\"/api/cargos/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um cargo com este nome, nível e área",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um cargo com o nome 'Analista Judiciário', nível 'Superior' e área 'Direito'\",\"instance\":\"/api/cargos/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/cargos/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<CargoDto> updateCargo(
            @Parameter(description = "ID do cargo a ser atualizado", required = true) @PathVariable Long id,
            @Valid @RequestBody CargoUpdateRequest cargoUpdateRequest) {
        CargoDto existingCargo = cargoService.findById(id);
        existingCargo.setNome(cargoUpdateRequest.getNome());
        existingCargo.setNivel(cargoUpdateRequest.getNivel());
        existingCargo.setArea(cargoUpdateRequest.getArea());
        CargoDto updatedCargo = cargoService.save(existingCargo);
        return ResponseEntity.ok(updatedCargo);
    }

    @Operation(
        summary = "Excluir cargo",
        description = "Remove um cargo existente com base no ID fornecido. A exclusão será impedida se houver concursos associados a este cargo.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Cargo excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Cargo com ID: '1'\",\"instance\":\"/api/cargos/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Existem concursos vinculados a este cargo",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Não é possível excluir o cargo pois existem concursos associados a ele.\",\"instance\":\"/api/cargos/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/cargos/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargo(
            @Parameter(description = "ID do cargo a ser excluído", required = true) @PathVariable Long id) {
        cargoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}