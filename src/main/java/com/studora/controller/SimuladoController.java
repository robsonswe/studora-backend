package com.studora.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import com.studora.dto.SimuladoDto;
import com.studora.dto.request.SimuladoGenerationRequest;
import com.studora.service.SimuladoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/simulados")
@CrossOrigin(origins = "*")
@Tag(name = "Simulados", description = "Endpoints para gerenciamento de simulados (provas mock)")
public class SimuladoController {

    @Autowired
    private SimuladoService simuladoService;

    @Operation(
        summary = "Gerar novo simulado",
        description = "Cria um novo simulado com base em blocos de critérios. Aplica a lógica de 'Venn Diagram' para maximizar a cobertura de conteúdo.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Parâmetros para geração do simulado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SimuladoGenerationRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Simulado gerado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SimuladoDto.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "id": 1,
                          "nome": "Simulado Geral 2024"
                        }
                        """
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "type": "about:blank",
                          "title": "Erro de validação",
                          "status": 400,
                          "detail": "Um ou mais campos apresentam erros de validação.",
                          "instance": "/api/simulados/gerar",
                          "errors": {
                            "nome": "não deve estar em branco"
                          }
                        }
                        """
                    ))),
            @ApiResponse(responseCode = "422", description = "Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "type": "about:blank",
                          "title": "Entidade não processável",
                          "status": 422,
                          "detail": "Não foi possível encontrar o mínimo de 20 questões que atendam aos critérios (encontradas: 5).",
                          "instance": "/api/simulados/gerar"
                        }
                        """
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "type": "about:blank",
                          "title": "Erro interno no servidor",
                          "status": 500,
                          "detail": "Ocorreu um erro inesperado no servidor.",
                          "instance": "/api/simulados/gerar"
                        }
                        """
                    )))
        }
    )
    @PostMapping("/gerar")
    @JsonView(Views.Summary.class)
    public ResponseEntity<SimuladoDto> gerarSimulado(@Valid @RequestBody SimuladoGenerationRequest request) {
        SimuladoDto simulado = simuladoService.gerarSimulado(request);
        return new ResponseEntity<>(simulado, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Iniciar simulado",
        description = "Registra o horário de início de um simulado. Oculta respostas corretas e justificativas.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Simulado iniciado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SimuladoDto.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "id": 1,
                          "nome": "Simulado PC-SP",
                          "startedAt": "2023-06-15T10:30:00",
                          "finishedAt": null,
                          "questoes": [
                            {
                              "id": 101,
                              "concursoId": 1,
                              "enunciado": "Qual o princípio constitucional da publicidade?",
                              "anulada": false,
                              "desatualizada": false,
                              "alternativas": [
                                {
                                  "id": 501,
                                  "questaoId": 101,
                                  "ordem": 1,
                                  "texto": "Princípio A"
                                },
                                {
                                  "id": 502,
                                  "questaoId": 101,
                                  "ordem": 2,
                                  "texto": "Princípio B"
                                }
                              ]
                            }
                          ]
                        }
                        """
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Simulado não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "type": "about:blank",
                          "title": "Recurso não encontrado",
                          "status": 404,
                          "detail": "Não foi possível encontrar Simulado com ID: '1'",
                          "instance": "/api/simulados/1/iniciar"
                        }
                        """
                    ))),
            @ApiResponse(responseCode = "422", description = "Simulado já foi iniciado anteriormente",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "type": "about:blank",
                          "title": "Entidade não processável",
                          "status": 422,
                          "detail": "Este simulado já foi iniciado.",
                          "instance": "/api/simulados/1/iniciar"
                        }
                        """
                    )))
        }
    )
    @PatchMapping("/{id}/iniciar")
    @JsonView(Views.SimuladoIniciado.class)
    public ResponseEntity<SimuladoDto> iniciarSimulado(
            @Parameter(description = "ID do simulado a iniciar", required = true) @PathVariable Long id) {
        SimuladoDto simulado = simuladoService.iniciarSimulado(id);
        return ResponseEntity.ok(simulado);
    }

    @Operation(
        summary = "Finalizar simulado",
        description = "Registra o horário de término. Libera a visualização das respostas corretas e justificativas.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Simulado finalizado com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SimuladoDto.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "id": 1,
                          "nome": "Simulado PC-SP",
                          "startedAt": "2023-06-15T10:30:00",
                          "finishedAt": "2023-06-15T12:30:00",
                          "questoes": [
                            {
                              "id": 101,
                              "concursoId": 1,
                              "enunciado": "Qual o princípio constitucional da publicidade?",
                              "anulada": false,
                              "desatualizada": false,
                              "alternativas": [
                                {
                                  "id": 501,
                                  "questaoId": 101,
                                  "ordem": 1,
                                  "texto": "Princípio A",
                                  "correta": true,
                                  "justificativa": "Justificativa do Princípio A"
                                }
                              ],
                              "resposta": {
                                "id": 901,
                                "questaoId": 101,
                                "alternativaId": 501,
                                "correta": true,
                                "tempoRespostaSegundos": 45,
                                "createdAt": "2023-06-15T10:35:00"
                              }
                            }
                          ]
                        }
                        """
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Simulado não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "type": "about:blank",
                          "title": "Recurso não encontrado",
                          "status": 404,
                          "detail": "Não foi possível encontrar Simulado com ID: '1'",
                          "instance": "/api/simulados/1/finalizar"
                        }
                        """
                    ))),
            @ApiResponse(responseCode = "422", description = "Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "type": "about:blank",
                          "title": "Entidade não processável",
                          "status": 422,
                          "detail": "Não é possível finalizar um simulado que não foi iniciado.",
                          "instance": "/api/simulados/1/finalizar"
                        }
                        """
                    )))
        }
    )
    @PatchMapping("/{id}/finalizar")
    @JsonView(Views.SimuladoFinalizado.class)
    public ResponseEntity<SimuladoDto> finalizarSimulado(
            @Parameter(description = "ID do simulado a finalizar", required = true) @PathVariable Long id) {
        SimuladoDto simulado = simuladoService.finalizarSimulado(id);
        return ResponseEntity.ok(simulado);
    }

    @Operation(
        summary = "Obter resultado do simulado",
        description = "Retorna os detalhes do simulado. Se finalizado, mostra respostas; caso contrário, as oculta.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Dados do simulado retornados com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SimuladoDto.class),
                    examples = {
                        @ExampleObject(
                            name = "Simulado em andamento", 
                            value = """
                            {
                              "id": 1,
                              "nome": "Simulado PC-SP",
                              "startedAt": "2023-06-15T10:30:00",
                              "finishedAt": null,
                              "questoes": [
                                {
                                  "id": 101,
                                  "concursoId": 1,
                                  "enunciado": "Qual o princípio constitucional da publicidade?",
                                  "anulada": false,
                                  "desatualizada": false,
                                  "alternativas": [
                                    {
                                      "id": 501,
                                      "questaoId": 101,
                                      "ordem": 1,
                                      "texto": "Princípio A"
                                    },
                                    {
                                      "id": 502,
                                      "questaoId": 101,
                                      "ordem": 2,
                                      "texto": "Princípio B"
                                    }
                                  ]
                                }
                              ]
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Simulado finalizado", 
                            value = """
                            {
                              "id": 1,
                              "nome": "Simulado PC-SP",
                              "startedAt": "2023-06-15T10:30:00",
                              "finishedAt": "2023-06-15T12:30:00",
                              "questoes": [
                                {
                                  "id": 101,
                                  "concursoId": 1,
                                  "enunciado": "...",
                                  "alternativas": [
                                    {
                                      "id": 501,
                                      "correta": true,
                                      "justificativa": "..."
                                    }
                                  ],
                                  "resposta": {
                                    "id": 901,
                                    "alternativaId": 501,
                                    "correta": true
                                  }
                                }
                              ]
                            }
                            """
                        )
                    }
                )),
            @ApiResponse(responseCode = "404", description = "Simulado não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "type": "about:blank",
                          "title": "Recurso não encontrado",
                          "status": 404,
                          "detail": "Não foi possível encontrar Simulado com ID: '1'",
                          "instance": "/api/simulados/1"
                        }
                        """
                    )))
        }
    )
    @GetMapping("/{id}")
    public MappingJacksonValue getSimulado(@PathVariable Long id) {
        SimuladoDto simulado = simuladoService.getSimuladoResult(id);
        MappingJacksonValue wrapper = new MappingJacksonValue(simulado);
        
        if (simulado.getFinishedAt() != null) {
            wrapper.setSerializationView(Views.SimuladoFinalizado.class);
        } else {
            wrapper.setSerializationView(Views.SimuladoIniciado.class);
        }
        
        return wrapper;
    }

    @Operation(
        summary = "Excluir simulado",
        description = "Remove o simulado, mas mantém o histórico de respostas (desassociando-as).",
        responses = {
            @ApiResponse(responseCode = "204", description = "Simulado excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Simulado não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "type": "about:blank",
                          "title": "Recurso não encontrado",
                          "status": 404,
                          "detail": "Não foi possível encontrar Simulado com ID: '1'",
                          "instance": "/api/simulados/1"
                        }
                        """
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimulado(
            @Parameter(description = "ID do simulado a ser excluído", required = true) @PathVariable Long id) {
        simuladoService.deleteSimulado(id);
        return ResponseEntity.noContent().build();
    }
}
