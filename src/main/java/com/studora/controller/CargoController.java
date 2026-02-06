package com.studora.controller;

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
@RequestMapping("/api/cargos")
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
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageResponse.class)))
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

    @Operation(summary = "Obter todas as áreas de cargos")
    @GetMapping("/areas")
    public ResponseEntity<List<String>> getAllAreas(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(cargoService.findAllAreas(search));
    }

    @Operation(
        summary = "Obter cargo por ID",
        description = "Retorna um cargo específico com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Cargo encontrado", content = @Content(schema = @Schema(implementation = CargoDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrada")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<CargoDetailDto> getCargoById(@PathVariable Long id) {
        return ResponseEntity.ok(cargoService.getCargoDetailById(id));
    }

    @Operation(summary = "Criar novo cargo")
    @PostMapping
    public ResponseEntity<CargoDetailDto> createCargo(@Valid @RequestBody CargoCreateRequest request) {
        return new ResponseEntity<>(cargoService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar cargo")
    @PutMapping("/{id}")
    public ResponseEntity<CargoDetailDto> updateCargo(@PathVariable Long id, @Valid @RequestBody CargoUpdateRequest request) {
        return ResponseEntity.ok(cargoService.update(id, request));
    }

    @Operation(summary = "Excluir cargo")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCargo(@PathVariable Long id) {
        cargoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}