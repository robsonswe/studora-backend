package com.studora.controller.v1;

import com.studora.dto.PageResponse;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoFilter;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.QuestaoService;
import com.studora.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.studora.dto.Views;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questoes")
@Tag(name = "Questoes", description = "Endpoints para gerenciamento de questões")
@Slf4j
public class QuestaoController {

    private final QuestaoService questaoService;

    public QuestaoController(QuestaoService questaoService) {
        this.questaoService = questaoService;
    }

    @Operation(
        summary = "Obter questões",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de questões retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "Página de Questões (Gabaritos Ocultos)",
                            value = "{\"content\": [{\"id\": 1, \"enunciado\": \"Questão 1?\", \"concurso\": {\"id\": 10, \"ano\": 2024, \"bancaId\": 2, \"bancaNome\": \"Cebraspe\", \"instituicaoId\": 3, \"instituicaoNome\": \"Policia Federal\", \"instituicaoArea\": \"Segurança\"}, \"subtemas\": [{\"id\": 5, \"nome\": \"Habeas Corpus\", \"temaId\": 20, \"temaNome\": \"Remédios Constitucionais\", \"disciplinaId\": 100, \"disciplinaNome\": \"Direito Constitucional\"}], \"cargos\": [{\"id\": 1, \"nome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\"}], \"anulada\": false, \"desatualizada\": false, \"imageUrl\": null, \"alternativas\": [{\"id\": 1, \"texto\": \"Opção A\"}]}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        ),
                        @ExampleObject(
                            name = "Página de Questões (Gabaritos Visíveis)",
                            value = "{\"content\": [{\"id\": 2, \"enunciado\": \"Questão 2?\", \"concurso\": {\"id\": 10, \"ano\": 2024, \"bancaId\": 2, \"bancaNome\": \"Cebraspe\", \"instituicaoId\": 3, \"instituicaoNome\": \"Policia Federal\", \"instituicaoArea\": \"Segurança\"}, \"subtemas\": [{\"id\": 5, \"nome\": \"Habeas Corpus\", \"temaId\": 20, \"temaNome\": \"Remédios Constitucionais\", \"disciplinaId\": 100, \"disciplinaNome\": \"Direito Constitucional\"}], \"cargos\": [{\"id\": 1, \"nome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\"}], \"anulada\": false, \"desatualizada\": false, \"imageUrl\": \"https://exemplo.com/img.png\", \"alternativas\": [{\"id\": 2, \"texto\": \"Opção B\", \"correta\": true, \"justificativa\": \"...\"}], \"respostas\": [{\"id\": 50, \"correta\": true, \"createdAt\": \"2026-02-07T12:00:00\"}]}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                        )
                    }
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/v1/questoes\"}"
                    )))
        }
    )
    @GetMapping
    @JsonView(Views.QuestaoVisivel.class)
    public ResponseEntity<PageResponse<QuestaoSummaryDto>> getQuestoes(
            @ParameterObject @Valid QuestaoFilter filter,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), List.of());
        Page<QuestaoSummaryDto> questoes = questaoService.findAll(filter, finalPageable);
        
        // Apply visibility rules by nulling fields for those that should be hidden
        java.time.LocalDateTime monthAgo = java.time.LocalDateTime.now().minusMonths(1);
        questoes.getContent().forEach(q -> {
            boolean hasRecent = q.getRespostas() != null && q.getRespostas().stream()
                    .anyMatch(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(monthAgo));
            
            if (!hasRecent) {
                if (q.getAlternativas() != null) {
                    q.getAlternativas().forEach(alt -> {
                        alt.setCorreta(null);
                        alt.setJustificativa(null);
                    });
                }
                q.setRespostas(null);
            }
        });

        return ResponseEntity.ok(new PageResponse<>(questoes));
    }

    @Operation(
        summary = "Obter uma questão aleatória",
        description = "Retorna uma questão aleatória que atenda aos critérios de filtro fornecidos. " +
                      "Questões respondidas nos últimos 30 dias são excluídas da seleção. " +
                      "O gabarito é OCULTADO para as questões retornadas por este endpoint, pois elas ou nunca foram respondidas ou foram respondidas há mais de 30 dias.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Questão encontrada", 
                content = @Content(
                    schema = @Schema(implementation = QuestaoDetailDto.class),
                    examples = {
                        @ExampleObject(
                            name = "Questão Aleatória (Gabarito Oculto)",
                            value = "{\"id\": 1, \"enunciado\": \"Questão aleatória?\", \"concurso\": {\"id\": 10, \"ano\": 2024, \"bancaId\": 2, \"bancaNome\": \"Cebraspe\", \"instituicaoId\": 3, \"instituicaoNome\": \"Policia Federal\", \"instituicaoArea\": \"Segurança\"}, \"subtemas\": [{\"id\": 5, \"nome\": \"Habeas Corpus\", \"temaId\": 20, \"temaNome\": \"Remédios Constitucionais\", \"disciplinaId\": 100, \"disciplinaNome\": \"Direito Constitucional\"}], \"cargos\": [{\"id\": 1, \"nome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\"}], \"anulada\": false, \"desatualizada\": false, \"imageUrl\": null, \"alternativas\": [{\"id\": 1, \"texto\": \"Opção A\"}]}"
                        )
                    }
                )),
            @ApiResponse(responseCode = "404", description = "Nenhuma questão encontrada com os filtros fornecidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar nenhuma questão com os filtros fornecidos.\",\"instance\":\"/api/v1/questoes/random\"}"
                    )))
        }
    )
    @GetMapping("/random")
    public MappingJacksonValue getRandomQuestao(@ParameterObject @Valid com.studora.dto.questao.QuestaoRandomFilter filter) {
        QuestaoDetailDto questao = questaoService.getRandomQuestao(filter);
        return wrapWithView(questao);
    }

    @Operation(
        summary = "Obter questão por ID",
        description = "Retorna os detalhes de uma questão. O gabarito (campo 'correta' e 'justificativa') e o histórico de respostas são VISÍVEIS " +
                      "apenas se a questão tiver sido respondida nos últimos 30 dias (1 mês). Caso contrário (não respondida ou resposta antiga), o gabarito é ocultado. " +
                      "Use o parâmetro 'admin=true' para forçar a exibição do gabarito.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Questão encontrada", 
                content = @Content(
                    schema = @Schema(implementation = QuestaoDetailDto.class),
                    examples = {
                        @ExampleObject(
                            name = "Gabarito Oculto (Não respondida ou resposta antiga)",
                            value = "{\"id\": 1, \"enunciado\": \"Questão exemplo?\", \"concurso\": {\"id\": 10, \"ano\": 2024, \"bancaId\": 2, \"bancaNome\": \"Cebraspe\", \"instituicaoId\": 3, \"instituicaoNome\": \"Policia Federal\", \"instituicaoArea\": \"Segurança\"}, \"subtemas\": [{\"id\": 5, \"nome\": \"Habeas Corpus\", \"temaId\": 20, \"temaNome\": \"Remédios Constitucionais\", \"disciplinaId\": 100, \"disciplinaNome\": \"Direito Constitucional\"}], \"cargos\": [{\"id\": 1, \"nome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\"}], \"anulada\": false, \"desatualizada\": false, \"imageUrl\": null, \"alternativas\": [{\"id\": 1, \"texto\": \"Opção A\"}]}"
                        ),
                        @ExampleObject(
                            name = "Gabarito Visível (Respondida recentemente ou admin=true)",
                            value = "{\"id\": 1, \"enunciado\": \"Questão exemplo?\", \"concurso\": {\"id\": 10, \"ano\": 2024, \"bancaId\": 2, \"bancaNome\": \"Cebraspe\", \"instituicaoId\": 3, \"instituicaoNome\": \"Policia Federal\", \"instituicaoArea\": \"Segurança\"}, \"subtemas\": [{\"id\": 5, \"nome\": \"Habeas Corpus\", \"temaId\": 20, \"temaNome\": \"Remédios Constitucionais\", \"disciplinaId\": 100, \"disciplinaNome\": \"Direito Constitucional\"}], \"cargos\": [{\"id\": 1, \"nome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\"}], \"anulada\": false, \"desatualizada\": false, \"imageUrl\": null, \"alternativas\": [{\"id\": 1, \"texto\": \"Opção A\", \"correta\": true, \"justificativa\": \"Explicação...\"}], \"respostas\": [{\"id\": 10, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"createdAt\": \"2026-02-07T10:00:00\"}]}"
                        )
                    }
                )),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Questão com ID: '123'\",\"instance\":\"/api/v1/questoes/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public MappingJacksonValue getQuestaoById(
            @PathVariable Long id,
            @Parameter(description = "Se verdadeiro, força a exibição do gabarito mesmo sem respostas recentes.")
            @RequestParam(required = false, defaultValue = "false") boolean admin) {
        QuestaoDetailDto questao = questaoService.getQuestaoDetailById(id);
        
        if (admin) {
            MappingJacksonValue wrapper = new MappingJacksonValue(questao);
            wrapper.setSerializationView(com.studora.dto.Views.QuestaoVisivel.class);
            return wrapper;
        }
        
        return wrapWithView(questao);
    }

    private MappingJacksonValue wrapWithView(QuestaoDetailDto questao) {
        MappingJacksonValue wrapper = new MappingJacksonValue(questao);
        
        // Logic: Visible ONLY if has a response more recent than a month
        boolean hasRecent = false;
        if (questao.getRespostas() != null && !questao.getRespostas().isEmpty()) {
            java.time.LocalDateTime monthAgo = java.time.LocalDateTime.now().minusMonths(1);
            hasRecent = questao.getRespostas().stream()
                    .anyMatch(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(monthAgo));
        }
        
        if (hasRecent) {
            wrapper.setSerializationView(com.studora.dto.Views.QuestaoVisivel.class);
        } else {
            wrapper.setSerializationView(com.studora.dto.Views.QuestaoOculta.class);
        }
        
        return wrapper;
    }

    @Operation(
        summary = "Criar nova questão",
        responses = {
            @ApiResponse(responseCode = "201", description = "Questão criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 2, \"enunciado\": \"Nova questão...\", \"concurso\": {\"id\": 10, \"ano\": 2024, \"bancaId\": 2, \"bancaNome\": \"Cebraspe\", \"instituicaoId\": 3, \"instituicaoNome\": \"Policia Federal\", \"instituicaoArea\": \"Segurança\"}, \"subtemas\": [{\"id\": 5, \"nome\": \"Habeas Corpus\", \"temaId\": 20, \"temaNome\": \"Remédios Constitucionais\", \"disciplinaId\": 100, \"disciplinaNome\": \"Direito Constitucional\"}], \"cargos\": [{\"id\": 1, \"nome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\"}], \"anulada\": false, \"desatualizada\": false, \"imageUrl\": null, \"alternativas\": [{\"id\": 10, \"texto\": \"Opção A\", \"correta\": true, \"justificativa\": \"...\"}, {\"id\": 11, \"texto\": \"Opção B\", \"correta\": false, \"justificativa\": \"...\"}]}")
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/questoes\",\"errors\":{\"enunciado\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Uma questão deve ter pelo menos 2 alternativas\",\"instance\":\"/api/v1/questoes\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<QuestaoDetailDto> createQuestao(@Valid @RequestBody QuestaoCreateRequest request) {
        return new ResponseEntity<>(questaoService.create(request), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar questão",
        responses = {
            @ApiResponse(responseCode = "200", description = "Questão atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoDetailDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"enunciado\": \"Questão atualizada...\", \"concurso\": {\"id\": 10, \"ano\": 2024, \"bancaId\": 2, \"bancaNome\": \"Cebraspe\", \"instituicaoId\": 3, \"instituicaoNome\": \"Policia Federal\", \"instituicaoArea\": \"Segurança\"}, \"subtemas\": [{\"id\": 5, \"nome\": \"Habeas Corpus\", \"temaId\": 20, \"temaNome\": \"Remédios Constitucionais\", \"disciplinaId\": 100, \"disciplinaNome\": \"Direito Constitucional\"}], \"cargos\": [{\"id\": 1, \"nome\": \"Agente\", \"nivel\": \"SUPERIOR\", \"area\": \"Policial\"}], \"anulada\": false, \"desatualizada\": false, \"imageUrl\": \"https://example.com/image.png\", \"alternativas\": [{\"id\": 1, \"texto\": \"Opção A\", \"correta\": true, \"justificativa\": \"...\"}, {\"id\": 2, \"texto\": \"Opção B\", \"correta\": false, \"justificativa\": \"...\"}]}")
                )),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Questão com ID: '1'\",\"instance\":\"/api/v1/questoes/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<QuestaoDetailDto> updateQuestao(@PathVariable Long id, @Valid @RequestBody QuestaoUpdateRequest request) {
        log.info("Recebida requisição para atualizar questão ID {}: {}", id, request);
        return ResponseEntity.ok(questaoService.update(id, request));
    }

    @Operation(
        summary = "Excluir questão",
        responses = {
            @ApiResponse(responseCode = "204", description = "Questão excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Questão com ID: '1'\",\"instance\":\"/api/v1/questoes/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestao(@PathVariable Long id) {
        questaoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Alternar status de desatualizada",
        responses = {
            @ApiResponse(responseCode = "204", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada")
        }
    )
    @PatchMapping("/{id}/desatualizada")
    public ResponseEntity<Void> toggleDesatualizada(@PathVariable Long id) {
        questaoService.toggleDesatualizada(id);
        return ResponseEntity.noContent().build();
    }
}