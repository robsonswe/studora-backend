package com.studora.controller.v1;

import com.studora.dto.PageResponse;
import com.studora.dto.resposta.RespostaSummaryDto;
import com.studora.dto.resposta.RespostaDetailDto;
import com.studora.dto.request.RespostaCreateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.RespostaService;
import com.studora.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/respostas")
@Tag(name = "Respostas", description = "Endpoints para gerenciamento de respostas dos usuários")
public class RespostaController {

    private final RespostaService respostaService;

    public RespostaController(RespostaService respostaService) {
        this.respostaService = respostaService;
    }

    @Operation(
        summary = "Obter todas as respostas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Página de respostas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"content\": [{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"tempoRespostaSegundos\": 45, \"dificuldade\": \"MEDIA\"}], \"pageNumber\": 0, \"pageSize\": 20, \"totalElements\": 1, \"totalPages\": 1, \"last\": true}"
                    )
                ))
        }
    )
    @GetMapping
    public ResponseEntity<PageResponse<RespostaSummaryDto>> getAllRespostas(
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), List.of());
        Page<RespostaSummaryDto> respostas = respostaService.findAll(finalPageable);
        return ResponseEntity.ok(new PageResponse<>(respostas));
    }

    @Operation(
        summary = "Obter resposta por ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Resposta encontrada", 
                content = @Content(
                    schema = @Schema(implementation = RespostaSummaryDto.class),
                    examples = @ExampleObject(value = "{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"tempoRespostaSegundos\": 45, \"dificuldade\": \"MEDIA\"}")
                )),
            @ApiResponse(responseCode = "404", description = "Resposta não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Resposta não encontrada com o ID fornecido.\",\"instance\":\"/api/v1/respostas/1\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<RespostaSummaryDto> getRespostaById(@PathVariable Long id) {
        return ResponseEntity.ok(respostaService.getRespostaSummaryById(id));
    }

    @Operation(
        summary = "Obter respostas por questão",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de respostas retornada com sucesso",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = RespostaSummaryDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"tempoRespostaSegundos\": 45, \"dificuldade\": \"MEDIA\"}]"
                    )
                ))
        }
    )
    @GetMapping("/questao/{questaoId}")
    public ResponseEntity<List<RespostaSummaryDto>> getRespostasByQuestaoId(@PathVariable Long questaoId) {
        return ResponseEntity.ok(respostaService.getRespostasByQuestaoId(questaoId));
    }

    @Operation(
        summary = "Criar nova resposta",
        responses = {
            @ApiResponse(responseCode = "201", description = "Resposta criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = RespostaDetailDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"questaoId\": 1, \"alternativaId\": 1, \"correta\": true, \"justificativa\": \"Raciocínio lógico...\", \"tempoRespostaSegundos\": 45, \"simuladoId\": 1, \"dificuldade\": \"MEDIA\", \"createdAt\": \"2026-02-08T23:43:45.023Z\", \"alternativas\": [{\"id\": 1, \"ordem\": 1, \"texto\": \"A resposta correta é a opção A\", \"correta\": true, \"justificativa\": \"Esta é a alternativa correta porque...\"}]}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/v1/respostas\",\"errors\":{\"questaoId\":\"não deve ser nulo\"}}"
                    ))),
            @ApiResponse(responseCode = "422", description = "Regras de negócio violadas (ex: questão anulada)",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Não é possível responder a uma questão que foi anulada ou está desatualizada.\",\"instance\":\"/api/v1/respostas\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<RespostaDetailDto> createResposta(@Valid @RequestBody RespostaCreateRequest request) {
        return new ResponseEntity<>(respostaService.createResposta(request), HttpStatus.CREATED);
    }

    @Operation(
        summary = "Excluir resposta",
        responses = {
            @ApiResponse(responseCode = "204", description = "Resposta excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Resposta não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Resposta não encontrada com o ID fornecido.\",\"instance\":\"/api/v1/respostas/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResposta(@PathVariable Long id) {
        respostaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
