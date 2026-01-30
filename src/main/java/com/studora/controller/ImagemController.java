package com.studora.controller;

import com.studora.dto.ImagemDto;
import com.studora.service.ImagemService;
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
@RequestMapping("/api/imagens")
@CrossOrigin(origins = "*")
@Tag(name = "Imagens", description = "Endpoints para gerenciamento de imagens")
public class ImagemController {

    @Autowired
    private ImagemService imagemService;

    @Operation(
        summary = "Obter todas as imagens",
        description = "Retorna uma lista com todas as imagens cadastradas",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de imagens retornada com sucesso",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = ImagemDto.class)),
                    examples = @ExampleObject(
                        value = "[{\"id\": 1, \"url\": \"http://example.com/image.png\", \"descricao\": \"Imagem de teste\"}]"
                    )
                )),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/imagens\"}"
                    )))
        }
    )
    @GetMapping
    public ResponseEntity<List<ImagemDto>> getAllImagens() {
        List<ImagemDto> imagens = imagemService.getAllImagens();
        return ResponseEntity.ok(imagens);
    }

    @Operation(
        summary = "Obter imagem por ID",
        description = "Retorna uma imagem específica com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "200", description = "Imagem encontrada e retornada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ImagemDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"url\": \"http://example.com/image.png\", \"descricao\": \"Imagem de teste\"}"
                    )
                )),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Imagem com ID: '123'\",\"instance\":\"/api/imagens/123\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/imagens/123\"}"
                    )))
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ImagemDto> getImagemById(
            @Parameter(description = "ID da imagem a ser buscada", required = true) @PathVariable Long id) {
        ImagemDto imagem = imagemService.getImagemById(id);
        return ResponseEntity.ok(imagem);
    }

    @Operation(
        summary = "Criar nova imagem",
        description = "Cria uma nova imagem com base nos dados fornecidos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados da nova imagem a ser criada",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ImagemDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Nova imagem criada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ImagemDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 2, \"url\": \"http://example.com/image2.png\", \"descricao\": \"Nova imagem\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/imagens\",\"errors\":{\"url\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/imagens\"}"
                    )))
        }
    )
    @PostMapping
    public ResponseEntity<ImagemDto> createImagem(
            @Valid @RequestBody ImagemDto imagemDto) {
        ImagemDto createdImagem = imagemService.createImagem(imagemDto);
        return new ResponseEntity<>(createdImagem, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Atualizar imagem",
        description = "Atualiza os dados de uma imagem existente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados atualizados da imagem",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ImagemDto.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Imagem atualizada com sucesso",
                content = @Content(
                    schema = @Schema(implementation = ImagemDto.class),
                    examples = @ExampleObject(
                        value = "{\"id\": 1, \"url\": \"http://example.com/image_updated.png\", \"descricao\": \"Imagem atualizada\"}"
                    )
                )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro de validação\",\"status\":400,\"detail\":\"Um ou mais campos apresentam erros de validação.\",\"instance\":\"/api/imagens/1\",\"errors\":{\"url\":\"não deve estar em branco\"}}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Imagem com ID: '1'\",\"instance\":\"/api/imagens/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/imagens/1\"}"
                    )))
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ImagemDto> updateImagem(
            @Parameter(description = "ID da imagem a ser atualizada", required = true) @PathVariable Long id,
            @Valid @RequestBody ImagemDto imagemDto) {
        ImagemDto updatedImagem = imagemService.updateImagem(id, imagemDto);
        return ResponseEntity.ok(updatedImagem);
    }

    @Operation(
        summary = "Excluir imagem",
        description = "Remove uma imagem existente com base no ID fornecido",
        responses = {
            @ApiResponse(responseCode = "204", description = "Imagem excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Recurso não encontrado\",\"status\":404,\"detail\":\"Não foi possível encontrar Imagem com ID: '1'\",\"instance\":\"/api/imagens/1\"}"
                    ))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(
                        value = "{\"type\":\"about:blank\",\"title\":\"Erro interno no servidor\",\"status\":500,\"detail\":\"Ocorreu um erro inesperado no servidor.\",\"instance\":\"/api/imagens/1\"}"
                    )))
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImagem(
            @Parameter(description = "ID da imagem a ser excluída", required = true) @PathVariable Long id) {
        imagemService.deleteImagem(id);
        return ResponseEntity.noContent().build();
    }
}