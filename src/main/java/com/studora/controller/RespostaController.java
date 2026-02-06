package com.studora.controller;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/respostas")
@Tag(name = "Respostas", description = "Endpoints para gerenciamento de respostas dos usuários")
public class RespostaController {

    private final RespostaService respostaService;

    public RespostaController(RespostaService respostaService) {
        this.respostaService = respostaService;
    }

    @Operation(summary = "Obter todas as respostas")
    @GetMapping
    public ResponseEntity<PageResponse<RespostaSummaryDto>> getAllRespostas(
            @Parameter(hidden = true) @PageableDefault(size = AppConstants.DEFAULT_PAGE_SIZE) Pageable pageable,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Pageable finalPageable = PaginationUtils.applyPrioritySort(pageable, sort, direction, Map.of(), List.of());
        Page<RespostaSummaryDto> respostas = respostaService.findAll(finalPageable);
        return ResponseEntity.ok(new PageResponse<>(respostas));
    }

    @Operation(summary = "Obter resposta por ID")
    @GetMapping("/{id}")
    public ResponseEntity<RespostaSummaryDto> getRespostaById(@PathVariable Long id) {
        return ResponseEntity.ok(respostaService.getRespostaSummaryById(id));
    }

    @Operation(summary = "Obter respostas por questão")
    @GetMapping("/questao/{questaoId}")
    public ResponseEntity<List<RespostaSummaryDto>> getRespostasByQuestaoId(@PathVariable Long questaoId) {
        return ResponseEntity.ok(respostaService.getRespostasByQuestaoId(questaoId));
    }

    @Operation(summary = "Criar nova resposta")
    @PostMapping
    public ResponseEntity<RespostaDetailDto> createResposta(@Valid @RequestBody RespostaCreateRequest request) {
        return new ResponseEntity<>(respostaService.createResposta(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Excluir resposta")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResposta(@PathVariable Long id) {
        respostaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}