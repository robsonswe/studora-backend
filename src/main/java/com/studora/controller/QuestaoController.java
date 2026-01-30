package com.studora.controller;

import com.studora.dto.QuestaoCargoDto;
import com.studora.dto.QuestaoDto;
import com.studora.service.QuestaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Autowired;
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
        summary = "Obter todas as questões",
        description = "Retorna uma lista com todas as questões cadastradas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de questões retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta por qual união?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": false}]"
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
    public ResponseEntity<List<QuestaoDto>> getAllQuestoes() {
        List<QuestaoDto> questoes = questaoService.getAllQuestoes();
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
                        value = "{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta por qual união?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": false}"
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
        summary = "Obter questões por ID do concurso",
        description = "Retorna uma lista de questões associadas a um concurso específico",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de questões retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta por qual união?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": false}]"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Concurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Concurso com ID: '1'\",\"instance\":\"/api/questoes/concurso/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/concurso/1\"}"
                    )))
        }
    )
    @GetMapping("/concurso/{concursoId}")
    public ResponseEntity<List<QuestaoDto>> getQuestoesByConcursoId(
            @Parameter(description = "ID do concurso para filtrar questões", required = true) @PathVariable Long concursoId) {
        List<QuestaoDto> questoes = questaoService.getQuestoesByConcursoId(concursoId);
        return ResponseEntity.ok(questoes);
    }

    @Operation(
        summary = "Obter questões por ID do subtema",
        description = "Retorna uma lista de questões associadas a um subtema específico",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de questões retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta por qual união?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": false}]"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Subtema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Subtema com ID: '1'\",\"instance\":\"/api/questoes/subtema/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/subtema/1\"}"
                    )))
        }
    )
    @GetMapping("/subtema/{subtemaId}")
    public ResponseEntity<List<QuestaoDto>> getQuestoesBySubtemaId(
            @Parameter(description = "ID do subtema para filtrar questões", required = true) @PathVariable Long subtemaId) {
        List<QuestaoDto> questoes = questaoService.getQuestoesBySubtemaId(subtemaId);
        return ResponseEntity.ok(questoes);
    }

    @Operation(
        summary = "Obter questões anuladas",
        description = "Retorna uma lista de questões que foram anuladas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de questões anuladas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta por qual união?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": true}]"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/anuladas\"}"
                    )))
        }
    )
    @GetMapping("/anuladas")
    public ResponseEntity<List<QuestaoDto>> getQuestoesAnuladas() {
        List<QuestaoDto> questoes = questaoService.getQuestoesAnuladas();
        return ResponseEntity.ok(questoes);
    }

    @Operation(
        summary = "Obter questões por ID do tema",
        description = "Retorna uma lista de questões associadas a um tema específico",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de questões retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta por qual união?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": false}]"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Tema com ID: '1'\",\"instance\":\"/api/questoes/tema/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/tema/1\"}"
                    )))
        }
    )
    @GetMapping("/tema/{temaId}")
    public ResponseEntity<List<QuestaoDto>> getQuestoesByTemaId(
            @Parameter(description = "ID do tema para filtrar questões", required = true) @PathVariable Long temaId) {
        List<QuestaoDto> questoes = questaoService.getQuestoesByTemaId(temaId);
        return ResponseEntity.ok(questoes);
    }

    @Operation(
        summary = "Obter questões por ID da disciplina",
        description = "Retorna uma lista de questões associadas a uma disciplina específica",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de questões retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = QuestaoDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta por qual união?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": false}]"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Disciplina não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Disciplina com ID: '1'\",\"instance\":\"/api/questoes/disciplina/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/questoes/disciplina/1\"}"
                    )))
        }
    )
    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<QuestaoDto>> getQuestoesByDisciplinaId(
            @Parameter(description = "ID da disciplina para filtrar questões", required = true) @PathVariable Long disciplinaId) {
        List<QuestaoDto> questoes = questaoService.getQuestoesByDisciplinaId(disciplinaId);
        return ResponseEntity.ok(questoes);
    }

    @Operation(
        summary = "Criar nova questão",
        description = "Cria uma nova questão com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova questão a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestaoDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova questão criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 2, \"enunciado\": \"Sobre a organização administrativa do Estado, o que define a descentralização?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/nova-imagem.jpg\", \"subtemaIds\": [3], \"concursoCargoIds\": [1, 2], \"anulada\": false}"
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
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Uma questão deve estar associada a pelo menos um cargo\",\"instance\":\"/api/questoes\"}"
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
            @Valid @RequestBody QuestaoDto questaoDto) {
        QuestaoDto createdQuestao = questaoService.createQuestao(questaoDto);
        return new ResponseEntity<>(createdQuestao, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar questão",
        description = "Atualiza os dados de uma questão existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da questão",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestaoDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Questão atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = QuestaoDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"enunciado\": \"De acordo com a CF/88, o Brasil é uma República Federativa composta pela união indissolúvel de quais entes?\", \"concursoId\": 1, \"imageUrl\": \"https://exemplo.com/imagem-atualizada.jpg\", \"subtemaIds\": [1, 2], \"concursoCargoIds\": [1], \"anulada\": false}"
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
                        value = "{\"type\":\"about:blank\",\"title\":\"Entidade não processável\",\"status\":422,\"detail\":\"Uma questão deve estar associada a pelo menos um cargo\",\"instance\":\"/api/questoes/1\"}"
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
            @Valid @RequestBody QuestaoDto questaoDto) {
        QuestaoDto updatedQuestao = questaoService.updateQuestao(id, questaoDto);
        return ResponseEntity.ok(updatedQuestao);
    }

    @Operation(
        summary = "Excluir questão",
        description = "Remove uma questão existente com base no ID fornecido",
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
        description = "Retorna uma lista de cargos associados a uma questão específica",
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
        description = "Associa um cargo existente a uma questão específica",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do cargo a ser adicionado à questão",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestaoCargoDto.class)
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
            @ApiResponse(responseCode = "404", description = "Questão não encontrada",
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
            @RequestBody QuestaoCargoDto questaoCargoDto) {
        questaoCargoDto.setQuestaoId(id); // Ensure the questao ID matches the path variable
        QuestaoCargoDto createdQuestaoCargo = questaoService.addCargoToQuestao(questaoCargoDto);
        return new ResponseEntity<>(createdQuestaoCargo, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Remover cargo da questão",
        description = "Desassocia um cargo de uma questão específica",
        responses = {
            @ApiResponse(responseCode = "204", description = "Cargo removido da questão com sucesso"),
            @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar a associação para remover.\",\"instance\":\"/api/questoes/1/cargos/1\"}"
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
