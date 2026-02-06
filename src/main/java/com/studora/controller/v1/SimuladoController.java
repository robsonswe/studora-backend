package com.studora.controller.v1;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import com.studora.dto.simulado.SimuladoDetailDto;
import com.studora.dto.request.SimuladoGenerationRequest;
import com.studora.service.SimuladoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @Operation(summary = "Gerar um novo simulado")
    public SimuladoDetailDto gerarSimulado(@Valid @RequestBody SimuladoGenerationRequest request) {
        return simuladoService.gerarSimulado(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter detalhes de um simulado")
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
    @Operation(summary = "Registrar início do simulado")
    public SimuladoDetailDto iniciarSimulado(@PathVariable Long id) {
        return simuladoService.iniciarSimulado(id);
    }

    @PatchMapping("/{id}/finalizar")
    @JsonView(Views.RespostaVisivel.class)
    @Operation(summary = "Registrar término do simulado")
    public SimuladoDetailDto finalizarSimulado(@PathVariable Long id) {
        return simuladoService.finalizarSimulado(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um simulado")
    public void deleteSimulado(@PathVariable Long id) {
        simuladoService.delete(id);
    }
}