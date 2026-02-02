package com.studora.controller;

import com.studora.dto.PageResponse;
import com.studora.dto.RespostaDto;
import com.studora.dto.RespostaComAlternativasDto;
import com.studora.dto.request.RespostaCreateRequest;
import com.studora.service.RespostaService;
import com.studora.util.PaginationUtils;
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
import java.util.Map;

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
            @Parameter(name = "sort", description = "Campo para ordenação primária", schema = @Schema(type = "string", allowableValues = {"createdAt"}, defaultValue = "createdAt")),
            @Parameter(name = "direction", description = "Direção da ordenação primária", schema = @Schema(type = "string", allowableValues = {"ASC", "DESC"}, defaultValue = "DESC"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de respostas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class),
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"createdAt\": \"2023-06-15T10:30:00\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
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
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        List<Sort.Order> tieBreakers = List.of(
            Sort.Order.desc("createdAt")
        );

        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), tieBreakers);
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
                        value = "{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"createdAt\": \"2023-06-15T10:30:00\"}"
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
        summary = "Obter todas as respostas por questão",
        description = "Retorna a lista de todas as tentativas associadas a uma questão específica, ordenadas da mais recente para a mais antiga.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de respostas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = RespostaDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"createdAt\": \"2023-06-15T10:30:00\"}]"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Questão não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Questão com ID: '1'\",\"instance\":\"/api/respostas/questao/1\"}"
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
    public ResponseEntity<List<RespostaDto>> getRespostasByQuestaoId(
            @Parameter(description = "ID da questão para obter as respostas", required = true) @PathVariable Long questaoId) {
        List<RespostaDto> respostas = respostaService.getRespostasByQuestaoId(questaoId);
        return ResponseEntity.ok(respostas);
    }


    @Operation(
        summary = "Criar nova resposta",
        description = "Registra uma nova tentativa de resposta para uma questão. Não é possível responder a questões anuladas.",
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
                        value = "{\"id\": 2, \"questaoId\": 1, \"alternativaId\": 2, \"correta\": true, \"createdAt\": \"2023-06-15T10:30:00\", \"alternativas\": [{\"id\": 1, \"ordem\": 1, \"texto\": \"Alternativa A\", \"justificativa\": \"Justificativa A\", \"correta\": false}, {\"id\": 2, \"ordem\": 2, \"texto\": \"Alternativa B\", \"justificativa\": \"Justificativa B\", \"correta\": true}]}"
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
        summary = "Excluir resposta",
        description = "Remove o registro de uma tentativa de resposta específica",
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