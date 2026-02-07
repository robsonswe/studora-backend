package com.studora.controller.v1;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import com.studora.dto.simulado.SimuladoDetailDto;
import com.studora.dto.request.SimuladoGenerationRequest;
import com.studora.service.SimuladoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/gerar")
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(Views.Summary.class)
    @Operation(
        summary = "Gerar um novo simulado",
        responses = {
            @ApiResponse(responseCode = "201", description = "Simulado gerado com sucesso", 
                content = @Content(
                    schema = @Schema(implementation = SimuladoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Simulado Geral 2024\", \"status\": \"GERADO\"}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Questões insuficientes",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Não há questões suficientes para os critérios fornecidos.\",\"instance\":\"/api/v1/simulados/gerar\"}"
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
                            name = "Simulado em andamento",
                            value = "{\"id\": 1, \"nome\": \"Simulado PC-SP\", \"questoes\": [{\"id\": 10, \"enunciado\": \"...\"}]}"
                        ),
                        @ExampleObject(
                            name = "Simulado finalizado",
                            value = "{\"id\": 1, \"nome\": \"Simulado PC-SP\", \"finishedAt\": \"2024-02-06T18:00:00Z\", \"score\": 85.0}"
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
    @JsonView(Views.RespostaOculta.class)
    @Operation(
        summary = "Registrar início do simulado",
        responses = {
            @ApiResponse(responseCode = "200", description = "Simulado iniciado", 
                content = @Content(
                    schema = @Schema(implementation = SimuladoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Simulado PC-SP\", \"startedAt\": \"2024-02-06T18:00:00Z\"}")
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
    @JsonView(Views.RespostaVisivel.class)
    @Operation(
        summary = "Registrar término do simulado",
        responses = {
            @ApiResponse(responseCode = "200", description = "Simulado finalizado", 
                content = @Content(
                    schema = @Schema(implementation = SimuladoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"nome\": \"Simulado PC-SP\", \"finishedAt\": \"2024-02-06T20:00:00Z\"}")
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
