package com.studora.controller;

import com.studora.dto.TemaDto;
import com.studora.dto.request.TemaCreateRequest;
import com.studora.dto.request.TemaUpdateRequest;
import com.studora.service.TemaService;
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
@RequestMapping("/api/temas")
@CrossOrigin(origins = "*")
@Tag(name = "Temas", description = "Endpoints para gerenciamento de temas")
public class TemaController {

    @Autowired
    private TemaService temaService;

    @Operation(
        summary = "Obter todos os temas",
        description = "Retorna uma lista com todos os temas cadastrados",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de temas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = TemaDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1}]"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/temas\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<List<TemaDto>> getAllTemas() {
        List<TemaDto> temas = temaService.getAllTemas();
        return ResponseEntity.ok(temas);
    }

    @Operation(
        summary = "Obter tema por ID",
        description = "Retorna um tema específico com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tema encontrado e retornado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = TemaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Tema com ID: '123'\",\"instance\":\"/api/temas/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/temas/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<TemaDto> getTemaById(
            @Parameter(description = "ID do tema a ser buscado", required = true) @PathVariable Long id) {
        TemaDto tema = temaService.getTemaById(id);
        return ResponseEntity.ok(tema);
    }

    @Operation(
        summary = "Obter temas por disciplina",
        description = "Retorna uma lista de temas associados a uma disciplina específica",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de temas retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = TemaDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"nome\": \"Direitos Fundamentais\", \"disciplinaId\": 1}]"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Disciplina não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Disciplina com ID: '1'\",\"instance\":\"/api/temas/disciplina/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/temas/disciplina/1\"}"
                    )))
        }
    )
    @GetMapping("/disciplina/{disciplinaId}")
    public ResponseEntity<List<TemaDto>> getTemasByDisciplinaId(
            @Parameter(description = "ID da disciplina para filtrar temas", required = true) @PathVariable Long disciplinaId) {
        List<TemaDto> temas = temaService.getTemasByDisciplinaId(disciplinaId);
        return ResponseEntity.ok(temas);
    }

    @Operation(
        summary = "Criar novo tema",
        description = "Cria um novo tema com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo tema a ser criado",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TemaCreateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Novo tema criado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = TemaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 2, \"nome\": \"Organização do Estado\", \"disciplinaId\": 1}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/temas\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um tema com este nome na mesma disciplina",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um tema com o nome 'Direitos Fundamentais' na disciplina com ID: 1\",\"instance\":\"/api/temas\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/temas\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<TemaDto> createTema(
            @Valid @RequestBody TemaCreateRequest temaCreateRequest) {
        // Convert the request DTO to the regular DTO for processing
        TemaDto temaDto = new TemaDto();
        temaDto.setDisciplinaId(temaCreateRequest.getDisciplinaId());
        temaDto.setNome(temaCreateRequest.getNome());

        TemaDto createdTema = temaService.createTema(temaDto);
        return new ResponseEntity<>(createdTema, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar tema",
        description = "Atualiza os dados de um tema existente. As regras de validação de nome único dentro da disciplina também se aplicam na atualização.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados do tema",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TemaUpdateRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Tema atualizado com sucesso",
                content = @Content(
                    schema = @Schema(implementation = TemaDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"nome\": \"Direitos Fundamentais Atualizado\", \"disciplinaId\": 1}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/temas/1\",\"errors\":{\"nome\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Tema com ID: '1'\",\"instance\":\"/api/temas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Já existe um tema com este nome na mesma disciplina",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Já existe um tema com o nome 'Direitos Fundamentais' na disciplina com ID: 1\",\"instance\":\"/api/temas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/temas/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<TemaDto> updateTema(
            @Parameter(description = "ID do tema a ser atualizado", required = true) @PathVariable Long id,
            @Valid @RequestBody TemaUpdateRequest temaUpdateRequest) {
        // Convert the request DTO to the regular DTO for processing
        TemaDto temaDto = new TemaDto();
        temaDto.setId(id); // Set the ID from the path parameter
        temaDto.setDisciplinaId(temaUpdateRequest.getDisciplinaId());
        temaDto.setNome(temaUpdateRequest.getNome());

        TemaDto updatedTema = temaService.updateTema(id, temaDto);
        return ResponseEntity.ok(updatedTema);
    }

    @Operation(
        summary = "Excluir tema",
        description = "Remove um tema existente com base no ID fornecido. A exclusão será impedida se houver subtemas associados a este tema.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Tema excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tema não encontrado",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Tema com ID: '1'\",\"instance\":\"/api/temas/1\"}"
                    ))),
            @ApiResponse(responseCode = "409", description = "Conflito - Existem subtemas vinculados a este tema",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Conflito\",\"status\":409,\"detail\":\"Não é possível excluir o tema pois existem subtemas associados a ele.\",\"instance\":\"/api/temas/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/temas/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTema(
            @Parameter(description = "ID do tema a ser excluído", required = true) @PathVariable Long id) {
        temaService.deleteTema(id);
        return ResponseEntity.noContent().build();
    }
}