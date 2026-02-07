package com.studora.controller.v1;

import com.studora.dto.cargo.CargoSummaryDto;
import com.studora.dto.cargo.CargoDetailDto;
import com.studora.dto.PageResponse;
import com.studora.dto.request.CargoCreateRequest;
import com.studora.dto.request.CargoUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.CargoService;
import com.studora.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cargos")
@Tag(name = "Cargos", description = "Endpoints para gerenciamento de cargos")
public class CargoController {

    private final CargoService cargoService;

    public CargoController(CargoService cargoService) {
        this.cargoService = cargoService;
    }

    @Operation(
        summary = "Obter todos os cargos",
        description = "Retorna uma página com todos os cargos cadastrados.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de cargos retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"nome\": \"Analista Judiciário\", \"nivel\": \"Superior\", \"area\": \"Direito\"}, {\"id\": 2, \"nome\": \"Técnico Judiciário\", \"nivel\": \"Médio\", \"area\": \"Administrativa\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 2, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/cargos\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<CargoSummaryDto>> getAllCargos(
            @RequestParam(required = false) String nome,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.asc("nome"),
            Sort.Order.asc("area"),
            Sort.Order.asc("nivel")
        );

        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
        Page<CargoSummaryDto> cargos = cargoService.findAll(nome, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(cargos));
    }

    @Operation(
        summary = "Obter todas as áreas de cargos",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de áreas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(type = "string")),
                    examples = @ExampleObject(value = "[\"Tecnologia da Informação\", \"Judiciária\", \"Administrativa\"]")
                ))
        }
    )
    @GetMapping("/areas")
    public ResponseEntity<List<String>> getAllAreas(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(cargoService.findAllAreas(search));
    }

    @Operation(
        summary = "Obter cargo por ID",
        description = "Retorna um cargo específico com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Cargo encontrado", 
                content = @Content(
                    schema = @Schema(implementation = CargoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Analista Judiciário\", \"nivel\": \"Superior\", \"area\": \"Direito\"}")
                )),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Cargo com ID: '123'\",\"instance\":\"/api/v1/cargos/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<CargoDetailDto> getCargoById(@PathVariable Long id) {
        return ResponseEntity.ok(cargoService.getCargoDetailById(id));
    }

    @Operation(
        summary = "Criar novo cargo",
        responses = {
            @ApiResponse(responseCode = "201", description = "Cargo criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = CargoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 3, \"nome\": \"Delegado de Polícia\", \"nivel\": \"Superior\", \"area\": \"Policial\"}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/cargos\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um cargo com este nome, nível e área",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um cargo com o nome 'Delegado de Polícia', nível 'Superior' e área 'Policial'\",\"instance\":\"/api/v1/cargos\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<CargoDetailDto> createCargo(@Valid @RequestBody CargoCreateRequest request) {
        return new ResponseEntity<>(cargoService.create(request), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar cargo",
        responses = {
            @ApiResponse(responseCode = "200", description = "Cargo atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = CargoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Analista Judiciário - Área Judiciária\", \"nivel\": \"Superior\", \"area\": \"Direito\"}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/cargos/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Cargo com ID: '1'\",\"instance\":\"/api/v1/cargos/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um cargo com este nome, nível e área",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um cargo com o nome 'Analista Judiciário', nível 'Superior' e área 'Direito'\",\"instance\":\"/api/v1/cargos/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<CargoDetailDto> updateCargo(@PathVariable Long id, @Valid @RequestBody CargoUpdateRequest request) {
        return ResponseEntity.ok(cargoService.update(id, request));
    }

    @Operation(
        summary = "Excluir cargo",
        responses = {
            @ApiResponse(responseCode = "204", description = "Cargo excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Cargo com ID: '1'\",\"instance\":\"/api/v1/cargos/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Existem concursos vinculados a este cargo",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Não é possível excluir o cargo pois existem concursos associados a ele.\",\"instance\":\"/api/v1/cargos/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargo(@PathVariable Long id) {
        cargoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
