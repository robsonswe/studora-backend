package com.studora.controller;

import com.studora.dto.CargoDto;
import com.studora.dto.ErrorResponse;
import com.studora.service.CargoService;
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
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = CargoDto.class)),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "[\"id\": 1, \"nome\": \"Analista\", \"nivel\": \"Superior\", \"area\": \"TI\"]"
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
                content = @Content(schema = @Schema(implementation = CargoDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"id\": 1, \"nome\": \"Analista\", \"nivel\": \"Superior\", \"area\": \"TI\"}"
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
                content = @Content(schema = @Schema(implementation = CargoDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"id\": 2, \"nome\": \"Técnico\", \"nivel\": \"Médio\", \"area\": \"Administrativa\"}"
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
                content = @Content(schema = @Schema(implementation = CargoDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"id\": 1, \"nome\": \"Analista Sênior\", \"nivel\": \"Superior\", \"area\": \"TI\"}"
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
            @ApiResponse(responseCode = "204", description = "Cargo excluído com sucesso")
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargo(
            @Parameter(description = "ID do cargo a ser excluído", required = true) @PathVariable Long id) {
        cargoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
