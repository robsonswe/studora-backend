package com.studora.controller;

import com.studora.dto.AlternativaDto;
import com.studora.dto.QuestaoCargoDto;
import com.studora.dto.QuestaoDto;
import com.studora.dto.QuestaoFilter;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.dto.request.AlternativaCreateRequest;
import com.studora.dto.request.AlternativaUpdateRequest;
import com.studora.dto.request.QuestaoCargoCreateRequest;
import com.studora.service.QuestaoService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/questoes")
@CrossOrigin(origins = "*")
@Tag(name = "Questoes", description = "Endpoints para gerenciamento de questões")
public class QuestaoController {

    @Autowired
    private QuestaoService questaoService;

    @Operation(
        summary = "Obter questões",
        description = "Retorna uma página de questões com base nos filtros fornecidos. Suporta paginação.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de questões retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class),
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta por qual união?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": false, \"alternativas\": [{\"id\": 1, \"ordem\": 1, \"texto\": \"Opção A\", \"justificativa\": \"Justificativa A\", \"correta\": true}, {\"id\": 2, \"ordem\": 2, \"texto\": \"Opção B\", \"justificativa\": \"Justificativa B\", \"correta\": false}]}], \"pageable\": {\"pageNumber\": 0, \"pageSize\": 20}, \"totalElements\": 1, \"totalPages\": 1, \"last\": true, \"size\": 20, \"number\": 0, \"sort\": {\"empty\": true, \"sorted\": false, \"unsorted\": true}, \"numberOfElements\": 1, \"first\": true, \"empty\": false}"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<Page<QuestaoDto>> getQuestoes(
            @ParameterObject @Valid QuestaoFilter filter,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<QuestaoDto> questoes = questaoService.search(filter, pageable);
        return ResponseEntity.ok(questoes);
    }

    @Operation(
        summary = "Obter questão por ID",
        description = "Retorna uma questão específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Questão encontrada e retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta por qual união?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": false, \"alternativas\": [{\"id\": 1, \"ordem\": 1, \"texto\": \"Opção A\", \"justificativa\": \"Justificativa A\", \"correta\": true}, {\"id\": 2, \"ordem\": 2, \"texto\": \"Opção B\", \"justificativa\": \"Justificativa B\", \"correta\": false}]}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Questão com ID: '123'\",\"instance\":\"/api/questoes/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<QuestaoDto> getQuestaoById(
            @Parameter(description = "ID da questão a ser buscada", required = true) @PathVariable Long id) {
        QuestaoDto questao = questaoService.getQuestaoById(id);
        return ResponseEntity.ok(questao);
    }

    @Operation(
        summary = "Criar nova questão",
        description = "Cria uma nova questão com base nos dados fornecidos. A questão deve estar associada a pelo menos um cargo, possuir no mínimo 2 alternativas e exatamente uma alternativa correta (se não for anulada).",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova questão a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestaoCreateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova questão criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 2, \"enunciado\": \"Sobre a organização administrativa do Estado, o que define a descentralização?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/nova-imagem.jpg\", \"subtemaIds\": [3], \"concursoCargoIds\": [1, 2], \"anulada\": false, \"alternativas\": [{\"id\": 1, \"ordem\": 1, \"texto\": \"Opção A\", \"justificativa\": \"Justificativa A\", \"correta\": true}, {\"id\": 2, \"ordem\": 2, \"texto\": \"Opção B\", \"justificativa\": \"Justificativa B\", \"correta\": false}]}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/questoes\",\"errors\":{\"enunciado\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Uma questão deve ter pelo menos 2 alternativas\",\"instance\":\"/api/questoes\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<QuestaoDto> createQuestao(
            @Valid @RequestBody QuestaoCreateRequest questaoCreateRequest) {
        // Convert the request DTO to the regular DTO for processing
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setConcursoId(questaoCreateRequest.getConcursoId());
        questaoDto.setEnunciado(questaoCreateRequest.getEnunciado());
        questaoDto.setAnulada(questaoCreateRequest.getAnulada());
        questaoDto.setImageUrl(questaoCreateRequest.getImageUrl());
        questaoDto.setSubtemaIds(questaoCreateRequest.getSubtemaIds());
        questaoDto.setConcursoCargoIds(questaoCreateRequest.getConcursoCargoIds());
        questaoDto.setAlternativas(questaoCreateRequest.getAlternativas());

        QuestaoDto createdQuestao = questaoService.createQuestao(questaoDto);
        return new ResponseEntity<>(createdQuestao, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar questão",
        description = "Atualiza os dados de uma questão existente. As regras de negócio para alternativas e associação de cargos também se aplicam na atualização. Qualquer atualização removerá as respostas existentes para esta questão.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da questão",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestaoUpdateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Questão atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta pela união indissolúvel de quais entes?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem-atualizada.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": false, \"alternativas\": [{\"id\": 1, \"ordem\": 1, \"texto\": \"Opção A\", \"justificativa\": \"Justificativa A\", \"correta\": true}, {\"id\": 2, \"ordem\": 2, \"texto\": \"Opção B\", \"justificativa\": \"Justificativa B\", \"correta\": false}]}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/questoes/1\",\"errors\":{\"enunciado\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Questão com ID: '1'\",\"instance\":\"/api/questoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Uma questão deve ter exatamente uma alternativa correta\",\"instance\":\"/api/questoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<QuestaoDto> updateQuestao(
            @Parameter(description = "ID da questão a ser atualizada", required = true) @PathVariable Long id,
            @Valid @RequestBody QuestaoUpdateRequest questaoUpdateRequest) {
        // Convert the request DTO to the regular DTO for processing
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setConcursoId(questaoUpdateRequest.getConcursoId());
        questaoDto.setEnunciado(questaoUpdateRequest.getEnunciado());
        questaoDto.setAnulada(questaoUpdateRequest.getAnulada());
        questaoDto.setImageUrl(questaoUpdateRequest.getImageUrl());
        questaoDto.setSubtemaIds(questaoUpdateRequest.getSubtemaIds());
        questaoDto.setConcursoCargoIds(questaoUpdateRequest.getConcursoCargoIds());
        questaoDto.setAlternativas(questaoUpdateRequest.getAlternativas());

        QuestaoDto updatedQuestao = questaoService.updateQuestao(id, questaoDto);
        return ResponseEntity.ok(updatedQuestao);
    }

    @Operation(
        summary = "Excluir questão",
        description = "Remove uma questão existente com base no ID fornecido. Esta operação também removerá todas as alternativas e respostas associadas, além das associações com cargos.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Questão excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Questão com ID: '1'\",\"instance\":\"/api/questoes/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestao(
            @Parameter(description = "ID da questão a ser excluída", required = true) @PathVariable Long id) {
        questaoService.deleteQuestao(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoints for managing cargo associations
    @Operation(
        summary = "Obter cargos por questão",
        description = "Retorna uma lista de associações de cargos para uma questão específica",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de cargos retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoCargoDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"questaoId\": 1, \"concursoCargoId\": 1}]"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Questão com ID: '1'\",\"instance\":\"/api/questoes/1/cargos\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/1/cargos\"}"
                    )))
        }
    )
    @GetMapping("/{id}/cargos")
    public ResponseEntity<List<QuestaoCargoDto>> getCargosByQuestao(
            @Parameter(description = "ID da questão para filtrar cargos", required = true) @PathVariable Long id) {
        List<QuestaoCargoDto> cargos = questaoService.getCargosByQuestaoId(id);
        return ResponseEntity.ok(cargos);
    }

    @Operation(
        summary = "Adicionar cargo à questão",
        description = "Associa um cargo existente a uma questão específica. O cargo deve pertencer ao mesmo concurso da questão.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do cargo a ser adicionado à questão",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestaoCargoCreateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Cargo adicionado à questão com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoCargoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 2, \"questaoId\": 1, \"concursoCargoId\": 2}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/questoes/1/cargos\",\"errors\":{\"concursoCargoId\":\"não deve ser nulo\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Questão ou Cargo não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Questão com ID: '1'\",\"instance\":\"/api/questoes/1/cargos\"}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Cargo já associado à questão\",\"instance\":\"/api/questoes/1/cargos\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/1/cargos\"}"
                    )))
        }
    )
    @PostMapping("/{id}/cargos")
    public ResponseEntity<QuestaoCargoDto> addCargoToQuestao(
            @Parameter(description = "ID da questão à qual o cargo será adicionado", required = true) @PathVariable Long id,
            @RequestBody QuestaoCargoCreateRequest questaoCargoCreateRequest) {
        // Convert the request DTO to the regular DTO for processing
        QuestaoCargoDto questaoCargoDto = new QuestaoCargoDto();
        questaoCargoDto.setQuestaoId(id); // Use the path variable, not the request body
        questaoCargoDto.setConcursoCargoId(questaoCargoCreateRequest.getConcursoCargoId());

        QuestaoCargoDto createdQuestaoCargo = questaoService.addCargoToQuestao(questaoCargoDto);
        return new ResponseEntity<>(createdQuestaoCargo, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Remover cargo da questão",
        description = "Desassocia um cargo de uma questão específica. A remoção será impedida se este for o único cargo associado à questão.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Cargo removido da questão com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Associação entre questão e cargo não encontrada\",\"instance\":\"/api/questoes/1/cargos/1\"}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Entidade não processável - Regras de negócio violadas",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Uma questão deve estar associada a pelo menos um cargo\",\"instance\":\"/api/questoes/1/cargos/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/1/cargos/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{questaoId}/cargos/{concursoCargoId}")
    public ResponseEntity<Void> removeCargoFromQuestao(
            @Parameter(description = "ID da questão da qual o cargo será removido", required = true) @PathVariable Long questaoId,
            @Parameter(description = "ID do cargo a ser removido da questão", required = true) @PathVariable Long concursoCargoId) {
        questaoService.removeCargoFromQuestao(questaoId, concursoCargoId);
        return ResponseEntity.noContent().build();
    }



}
