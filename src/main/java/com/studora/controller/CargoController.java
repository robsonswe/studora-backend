package com.studora.controller;

import com.studora.dto.CargoDto;
import com.studora.service.CargoService;
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
@RequestMapping("/api/cargos")
@Tag(name = "Cargos", description = "Endpoints para gerenciamento de cargos")
public class CargoController {

    @Autowired
    private CargoService cargoService;

    @Operation(
        summary = "Obter todas as cargos",
        description = "Retorna uma lista com todos os cargos cadastrados",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de cargos retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = CargoDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Analista Judiciário\", \"nivel\": \"Superior\", \"area\": \"Direito\"}, {\"id\": 2, \"nome\": \"Técnico Judiciário\", \"nivel\": \"Médio\", \"area\": \"Administrativa\"}]"
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
    public ResponseEntity<List<CargoDto>> getAllCargos() {
        List<CargoDto> cargos = cargoService.findAll();
        return ResponseEntity.ok(cargos);
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
            @Parameter(description = "ID do cargo a ser buscada", required = true) @PathVariable Long id) {
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
                schema = @Schema(implementation = CargoDto.class)
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
            @RequestBody CargoDto cargoDto) {
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
                schema = @Schema(implementation = CargoDto.class)
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
            @Parameter(description = "ID da cargo a ser atualizada", required = true) @PathVariable Long id,
            @RequestBody CargoDto cargoDto) {
        CargoDto existingCargo = cargoService.findById(id);
        existingCargo.setNome(cargoDto.getNome());
        existingCargo.setNivel(cargoDto.getNivel());
        existingCargo.setArea(cargoDto.getArea());
        CargoDto updatedCargo = cargoService.save(existingCargo);
        return ResponseEntity.ok(updatedCargo);
    }

    @Operation(
        summary = "Excluir cargo",
        description = "Remove um cargo existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Cargo excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Cargo com ID: '1'\",\"instance\":\"/api/cargos/1\"}"
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