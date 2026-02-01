package com.studora.controller;

import com.studora.dto.PageResponse;
import com.studora.dto.RespostaDto;
import com.studora.dto.RespostaComAlternativasDto;
import com.studora.dto.request.RespostaCreateRequest;
import com.studora.dto.request.RespostaUpdateRequest;
import com.studora.service.RespostaService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/respostas")
@CrossOrigin(origins = "*")
@Tag(name = "Respostas", description = "Endpoints para gerenciamento de respostas")
public class RespostaController {

    @Autowired
    private RespostaService respostaService;

    @Operation(
        summary = "Obter todas as respostas",
        description = "Retorna uma página com todas as respostas cadastradas. Suporta paginação e ordenação prioritária.",
        parameters = {
            @Parameter(name = "page", description = "Número da página (0..N)", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "Tamanho da página", schema = @Schema(type = "integer", defaultValue = "20")),
            @Parameter(name = "sort", description = "Campo para ordenação primária", schema = @Schema(type = "string", allowableValues = {"respondidaEm"}, defaultValue = "respondidaEm")),
            @Parameter(name = "direction", description = "Direção da ordenação primária", schema = @Schema(type = "string", allowableValues = {"ASC", "DESC"}, defaultValue = "DESC"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de respostas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class),
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"respondidaEm\": \"2023-06-15T10:30:00\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/respostas\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<RespostaDto>> getAllRespostas(
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(defaultValue = "respondidaEm") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction dir = Sort.Direction.fromString(direction.toUpperCase());
        List<Sort.Order> orders = new ArrayList<>();
        
        // Primary sort chosen by user
        orders.add(new Sort.Order(dir, sort));
        
        // Default tie-breaker: respondidaEm DESC (if not already primary)
        if (!sort.equalsIgnoreCase("respondidaEm")) {
            orders.add(Sort.Order.desc("respondidaEm"));
        }
        
        // Final tie-breaker: ID descending
        orders.add(Sort.Order.desc("id"));
        
        Pageable finalPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
        Page<RespostaDto> respostas = respostaService.findAll(finalPageable);
        return ResponseEntity.ok(new PageResponse<>(respostas));
    }

    @Operation(
        summary = "Obter resposta por ID",
        description = "Retorna uma resposta específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Resposta encontrada e retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = RespostaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"respondidaEm\": \"2023-06-15T10:30:00\"}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Resposta não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Resposta com ID: '123'\",\"instance\":\"/api/respostas/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/respostas/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<RespostaDto> getRespostaById(
            @Parameter(description = "ID da resposta a ser buscada", required = true) @PathVariable Long id) {
        RespostaDto resposta = respostaService.getRespostaById(id);
        return ResponseEntity.ok(resposta);
    }

    @Operation(
        summary = "Obter resposta por questão",
        description = "Retorna a resposta associada a uma questão específica. Cada questão possui no máximo uma resposta.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Resposta retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = RespostaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"respondidaEm\": \"2023-06-15T10:30:00\"}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Resposta não encontrada para a questão",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Resposta com ID da Questão: '1'\",\"instance\":\"/api/respostas/questao/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/respostas/questao/1\"}"
                    )))
        }
    )
    @GetMapping("/questao/{questaoId}")
    public ResponseEntity<RespostaDto> getRespostaByQuestaoId(
            @Parameter(description = "ID da questão para obter a resposta", required = true) @PathVariable Long questaoId) {
        RespostaDto resposta = respostaService.getRespostaByQuestaoId(questaoId);
        return ResponseEntity.ok(resposta);
    }


    @Operation(
        summary = "Criar nova resposta",
        description = "Registra uma nova resposta para uma questão. Não é possível responder a questões anuladas ou que já possuam resposta.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova resposta a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RespostaCreateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova resposta criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = RespostaComAlternativasDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 2, \"questaoId\": 1, \"alternativaId\": 2, \"correta\": true, \"respondidaEm\": \"2023-06-15T10:30:00\", \"alternativas\": [{\"id\": 1, \"ordem\": 1, \"texto\": \"Alternativa A\", \"justificativa\": \"Justificativa A\", \"correta\": false}, {\"id\": 2, \"ordem\": 2, \"texto\": \"Alternativa B\", \"justificativa\": \"Justificativa B\", \"correta\": true}]}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/respostas\",\"errors\":{\"alternativaId\":\"não deve ser nulo\"}}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Não é possível responder a uma questão anulada\",\"instance\":\"/api/respostas\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/respostas\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<RespostaComAlternativasDto> createResposta(
            @Valid @RequestBody RespostaCreateRequest respostaCreateRequest) {
        RespostaComAlternativasDto createdResposta = respostaService.createRespostaWithAlternativas(respostaCreateRequest);
        return new ResponseEntity<>(createdResposta, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar resposta",
        description = "Atualiza a alternativa selecionada em uma resposta existente. O registro de data/hora será atualizado automaticamente.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da resposta (apenas alternativaId pode ser alterado)",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RespostaUpdateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Resposta atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = RespostaComAlternativasDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 2, \"correta\": true, \"respondidaEm\": \"2023-06-15T10:30:00\", \"alternativas\": [{\"id\": 1, \"ordem\": 1, \"texto\": \"Alternativa A\", \"justificativa\": \"Justificativa A\", \"correta\": false}, {\"id\": 2, \"ordem\": 2, \"texto\": \"Alternativa B\", \"justificativa\": \"Justificativa B\", \"correta\": true}]}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/respostas/1\",\"errors\":{\"alternativaId\":\"não deve ser nulo\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Resposta não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Resposta com ID: '1'\",\"instance\":\"/api/respostas/1\"}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Não é possível responder a uma questão anulada\",\"instance\":\"/api/respostas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/respostas/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<RespostaComAlternativasDto> updateResposta(
            @Parameter(description = "ID da resposta a ser atualizada", required = true) @PathVariable Long id,
            @Valid @RequestBody RespostaUpdateRequest respostaUpdateRequest) {
        RespostaComAlternativasDto updatedResposta = respostaService.updateRespostaWithAlternativas(id, respostaUpdateRequest);
        return ResponseEntity.ok(updatedResposta);
    }

    @Operation(
        summary = "Excluir resposta",
        description = "Remove o registro de resposta de uma questão",
        responses = {
            @ApiResponse(responseCode = "204", description = "Resposta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Resposta não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Resposta com ID: '1'\",\"instance\":\"/api/respostas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/respostas/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResposta(
            @Parameter(description = "ID da resposta a ser excluída", required = true) @PathVariable Long id) {
        respostaService.deleteResposta(id);
        return ResponseEntity.noContent().build();
    }
}