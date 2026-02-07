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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questoes")
@Tag(name = "Questoes", description = "Endpoints para gerenciamento de questões")
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
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"enunciado\": \"De acordo com a CF/88...\", \"concursoId\": 1, \"anulada\": false}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                    )
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
    public ResponseEntity<PageResponse<QuestaoSummaryDto>> getQuestoes(
            @ParameterObject @Valid QuestaoFilter filter,
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), List.of());
        Page<QuestaoSummaryDto> questoes = questaoService.findAll(filter, finalPageable);
        return ResponseEntity.ok(new PageResponse<>(questoes));
    }

    @Operation(
        summary = "Obter questão por ID",
        description = "Retorna os detalhes de uma questão. A visibilidade do gabarito depende do histórico de respostas do usuário.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Questão encontrada", 
                content = @Content(
                    schema = @Schema(implementation = QuestaoDetailDto.class),
                    examples = {
                        @ExampleObject(
                            name = "Gabarito Oculto (Ainda não respondida)",
                            value = "{\"id\": 1, \"enunciado\": \"Questão exemplo?\", \"alternativas\": [{\"id\": 1, \"texto\": \"Opção A\"}]}"
                        ),
                        @ExampleObject(
                            name = "Gabarito Visível (Já respondida)",
                            value = "{\"id\": 1, \"enunciado\": \"Questão exemplo?\", \"alternativas\": [{\"id\": 1, \"texto\": \"Opção A\", \"correta\": true, \"justificativa\": \"Explicação...\"}]}"
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
    public MappingJacksonValue getQuestaoById(@PathVariable Long id) {
        QuestaoDetailDto questao = questaoService.getQuestaoDetailById(id);
        MappingJacksonValue wrapper = new MappingJacksonValue(questao);
        
        if (questao.getRespostas() != null && !questao.getRespostas().isEmpty()) {
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
                    examples = @ExampleObject(value = "{\"id\": 2, \"enunciado\": \"Nova questão...\", \"concursoId\": 1}")
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
                    examples = @ExampleObject(value = "{\"id\": 1, \"enunciado\": \"Questão atualizada...\", \"concursoId\": 1}")
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