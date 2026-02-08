package com.studora.controller.v1;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.PageResponse;
import com.studora.dto.Views;
import com.studora.dto.simulado.SimuladoDetailDto;
import com.studora.dto.simulado.SimuladoSummaryDto;
import com.studora.dto.request.SimuladoGenerationRequest;
import com.studora.service.SimuladoService;
import com.studora.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/simulados")
@RequiredArgsConstructor
@Tag(name = "Simulados", description = "Endpoints para geração e execução de simulados")
public class SimuladoController {

    private final SimuladoService simuladoService;

    @GetMapping
    @JsonView(Views.Geracao.class)
    @Operation(
        summary = "Listar simulados",
        description = "Retorna uma lista paginada de simulados com seus filtros, mas sem as questões. Retorna 404 se nenhum simulado for encontrado."
    )
    @ApiResponse(responseCode = "200", description = "Lista de simulados")
    @ApiResponse(responseCode = "404", description = "Nenhum simulado encontrado",
        content = @Content(mediaType = "application/problem+json",
            schema = @Schema(implementation = ProblemDetail.class)))
    public PageResponse<SimuladoSummaryDto> listSimulados(
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        org.springframework.data.domain.Page<SimuladoSummaryDto> page = simuladoService.findAll(pageable);
        if (page.isEmpty()) {
            throw new com.studora.exception.ResourceNotFoundException("Nenhum simulado encontrado");
        }
        return new PageResponse<>(page);
    }

    @PostMapping("/gerar")
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Views.Geracao.class)
    @Operation(
        summary = "Gerar um novo simulado",
        responses = {
            @ApiResponse(responseCode = "201", description = "Simulado gerado com sucesso", 
                content = @Content(
                    schema = @Schema(implementation = SimuladoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Simulado Geral 2024\", \"startedAt\": null, \"finishedAt\": null, \"bancaId\": 1, \"disciplinas\": [{\"id\": 1, \"quantidade\": 20}]}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Bad Request\",\"status\":400,\"detail\":\"O nome do simulado é obrigatório\",\"instance\":\"/api/v1/simulados/gerar\"}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Questões insuficientes",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Não foi possível encontrar o número solicitado de questões. Solicitadas: 20, encontradas: 5\",\"instance\":\"/api/v1/simulados/gerar\"}"
                    )))
        }
    )
    public SimuladoDetailDto gerarSimulado(@Valid @RequestBody SimuladoGenerationRequest request) {
        return simuladoService.gerarSimulado(request);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obter detalhes de um simulado",
        responses = {
            @ApiResponse(responseCode = "200", description = "Simulado encontrado", 
                content = @Content(
                    schema = @Schema(implementation = SimuladoDetailDto.class),
                    examples = {
                        @ExampleObject(
                            name = "Gabarito Escondido",
                            value = "{\"id\": 1, \"nome\": \"Simulado PC-SP\", \"questoes\": [{\"id\": 10, \"enunciado\": \"...\", \"alternativas\": [{\"id\": 1, \"texto\": \"...\"}]}]}"
                        ),
                        @ExampleObject(
                            name = "Gabarito Visível",
                            value = "{\"id\": 1, \"nome\": \"Simulado PC-SP\", \"finishedAt\": \"2024-02-06T18:00:00Z\", \"questoes\": [{\"id\": 10, \"enunciado\": \"...\", \"alternativas\": [{\"id\": 1, \"texto\": \"...\", \"correta\": true}]}]}"
                        )
                    }
                )),
            @ApiResponse(responseCode = "404", description = "Simulado não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Simulado com ID: '1'\",\"instance\":\"/api/v1/simulados/1\"}"
                    )))
        }
    )
    public MappingJacksonValue getSimulado(@PathVariable Long id) {
        SimuladoDetailDto detail = simuladoService.getSimuladoDetailById(id);
        MappingJacksonValue wrapper = new MappingJacksonValue(detail);

        if (detail.getFinishedAt() != null) {
            wrapper.setSerializationView(Views.RespostaVisivel.class);
        } else {
            wrapper.setSerializationView(Views.RespostaOculta.class);
        }

        return wrapper;
    }

    @PatchMapping("/{id}/iniciar")
    @JsonView(Views.Summary.class)
    @Operation(
        summary = "Registrar início do simulado",
        responses = {
            @ApiResponse(responseCode = "200", description = "Simulado iniciado", 
                content = @Content(
                    schema = @Schema(implementation = SimuladoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Simulado PC-SP\", \"startedAt\": \"2024-02-06T18:00:00Z\", \"finishedAt\": null}")
                )),
            @ApiResponse(responseCode = "404", description = "Simulado não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Simulado com ID: '1'\",\"instance\":\"/api/v1/simulados/1/iniciar\"}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Simulado já iniciado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Este simulado já foi iniciado.\",\"instance\":\"/api/v1/simulados/1/iniciar\"}"
                    )))
        }
    )
    public SimuladoDetailDto iniciarSimulado(@PathVariable Long id) {
        return simuladoService.iniciarSimulado(id);
    }

    @PatchMapping("/{id}/finalizar")
    @JsonView(Views.Summary.class)
    @Operation(
        summary = "Registrar término do simulado",
        responses = {
            @ApiResponse(responseCode = "200", description = "Simulado finalizado", 
                content = @Content(
                    schema = @Schema(implementation = SimuladoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Simulado PC-SP\", \"startedAt\": \"2024-02-06T18:00:00Z\", \"finishedAt\": \"2024-02-06T20:00:00Z\"}")
                )),
            @ApiResponse(responseCode = "404", description = "Simulado não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Simulado com ID: '1'\",\"instance\":\"/api/v1/simulados/1/finalizar\"}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Questões pendentes",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Não é possível finalizar o simulado: existem 5 questões sem resposta.\",\"instance\":\"/api/v1/simulados/1/finalizar\"}"
                    )))
        }
    )
    public SimuladoDetailDto finalizarSimulado(@PathVariable Long id) {
        return simuladoService.finalizarSimulado(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Excluir um simulado",
        description = "Remove o simulado permanentemente. O histórico de respostas associado é preservado (desvinculado).",
        responses = {
            @ApiResponse(responseCode = "204", description = "Simulado excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Simulado não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Simulado com ID: '1'\",\"instance\":\"/api/v1/simulados/1\"}"
                    )))
        }
    )
    public void deleteSimulado(@PathVariable Long id) {
        simuladoService.delete(id);
    }
}
