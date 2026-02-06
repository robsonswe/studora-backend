package com.studora.controller.v1;

import com.studora.dto.PageResponse;
import com.studora.dto.questao.QuestaoCargoDto;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoFilter;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.dto.request.QuestaoCargoCreateRequest;
import com.studora.common.constants.AppConstants;
import com.studora.service.QuestaoService;
import com.studora.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questoes")
@CrossOrigin(origins = "*")
@Tag(name = "Questoes", description = "Endpoints para gerenciamento de questões")
public class QuestaoController {

    private final QuestaoService questaoService;

    public QuestaoController(QuestaoService questaoService) {
        this.questaoService = questaoService;
    }

    @Operation(summary = "Obter questões")
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

    @Operation(summary = "Obter questão por ID")
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

    @Operation(summary = "Criar nova questão")
    @PostMapping
    public ResponseEntity<QuestaoDetailDto> createQuestao(@Valid @RequestBody QuestaoCreateRequest request) {
        return new ResponseEntity<>(questaoService.create(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualizar questão")
    @PutMapping("/{id}")
    public ResponseEntity<QuestaoDetailDto> updateQuestao(@PathVariable Long id, @Valid @RequestBody QuestaoUpdateRequest request) {
        return ResponseEntity.ok(questaoService.update(id, request));
    }

    @Operation(summary = "Excluir questão")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestao(@PathVariable Long id) {
        questaoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Alternar status de desatualizada")
    @PatchMapping("/{id}/desatualizada")
    public ResponseEntity<Void> toggleDesatualizada(@PathVariable Long id) {
        questaoService.toggleDesatualizada(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obter cargos por questão")
    @GetMapping("/{id}/cargos")
    public ResponseEntity<List<QuestaoCargoDto>> getCargosByQuestao(@PathVariable Long id) {
        return ResponseEntity.ok(questaoService.getCargosByQuestaoId(id));
    }

    @Operation(summary = "Adicionar cargo à questão")
    @PostMapping("/{id}/cargos")
    public ResponseEntity<QuestaoCargoDto> addCargoToQuestao(@PathVariable Long id, @Valid @RequestBody QuestaoCargoCreateRequest request) {
        return new ResponseEntity<>(questaoService.addCargoToQuestao(id, request), HttpStatus.CREATED);
    }

    @Operation(summary = "Remover cargo da questão")
    @DeleteMapping("/{questaoId}/cargos/{concursoCargoId}")
    public ResponseEntity<Void> removeCargoFromQuestao(@PathVariable Long questaoId, @PathVariable Long concursoCargoId) {
        questaoService.removeCargoFromQuestao(questaoId, concursoCargoId);
        return ResponseEntity.noContent().build();
    }
}