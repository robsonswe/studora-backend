package com.studora.service;

import com.studora.dto.PageResponse;
import com.studora.dto.simulado.SimuladoDetailDto;
import com.studora.dto.request.SimuladoGenerationRequest;
import com.studora.dto.simulado.SimuladoSummaryDto;
import com.studora.entity.Questao;
import com.studora.entity.Simulado;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.SimuladoMapper;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SimuladoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SimuladoService {

    private final SimuladoRepository simuladoRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final SimuladoMapper simuladoMapper;

    @Transactional(readOnly = true)
    public Page<SimuladoSummaryDto> findAll(Pageable pageable) {
        return simuladoRepository.findAll(pageable)
                .map(simuladoMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public SimuladoDetailDto getSimuladoDetailById(Long id) {
        Simulado simulado = simuladoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado", "ID", id));
        return simuladoMapper.toDetailDto(simulado);
    }

    public SimuladoDetailDto create(SimuladoSummaryDto request) {
        Simulado simulado = simuladoMapper.toEntity(request);
        return simuladoMapper.toDetailDto(simuladoRepository.save(simulado));
    }

    public SimuladoDetailDto gerarSimulado(SimuladoGenerationRequest request) {
        log.info("Gerando simulado: {}", request.getNome());
        
        int totalSubtemas = request.getSubtemas() != null ? request.getSubtemas().stream().mapToInt(SimuladoGenerationRequest.ItemSelection::getQuantidade).sum() : 0;
        int totalTemas = request.getTemas() != null ? request.getTemas().stream().mapToInt(SimuladoGenerationRequest.ItemSelection::getQuantidade).sum() : 0;
        int totalDisciplinas = request.getDisciplinas() != null ? request.getDisciplinas().stream().mapToInt(SimuladoGenerationRequest.ItemSelection::getQuantidade).sum() : 0;
        
        int totalRequested = totalSubtemas + totalTemas + totalDisciplinas;
        if (totalRequested < 20) {
            throw new ValidationException("O simulado deve ter no total pelo menos 20 questões solicitadas (total atual: " + totalRequested + ").");
        }

        Simulado simulado = new Simulado();
        simulado.setNome(request.getNome());
        Set<Long> collectedIds = new HashSet<>();

        // 1. Subtemas (Mais específicos)
        if (request.getSubtemas() != null) {
            for (var sel : request.getSubtemas()) {
                List<Long> ids = questaoRepository.findIdsBySubtemaWithPreferences(sel.getId(), request, new ArrayList<>(collectedIds), PageRequest.of(0, sel.getQuantidade()));
                collectedIds.addAll(ids);
            }
        }

        // 2. Temas (Excluindo os subtemas já processados se possível)
        if (request.getTemas() != null) {
            List<Long> subtemaIdsToAvoid = request.getSubtemas() != null ? request.getSubtemas().stream().map(SimuladoGenerationRequest.ItemSelection::getId).toList() : List.of();
            for (var sel : request.getTemas()) {
                List<Long> ids = questaoRepository.findIdsByTemaWithPreferences(sel.getId(), subtemaIdsToAvoid, request, new ArrayList<>(collectedIds), PageRequest.of(0, sel.getQuantidade()));
                collectedIds.addAll(ids);
            }
        }

        // 3. Disciplinas (Excluindo temas e subtemas já processados se possível)
        if (request.getDisciplinas() != null) {
            List<Long> temaIdsToAvoid = request.getTemas() != null ? request.getTemas().stream().map(SimuladoGenerationRequest.ItemSelection::getId).toList() : List.of();
            List<Long> subtemaIdsToAvoid = request.getSubtemas() != null ? request.getSubtemas().stream().map(SimuladoGenerationRequest.ItemSelection::getId).toList() : List.of();
            for (var sel : request.getDisciplinas()) {
                List<Long> ids = questaoRepository.findIdsByDisciplinaWithPreferences(sel.getId(), temaIdsToAvoid, subtemaIdsToAvoid, request, new ArrayList<>(collectedIds), PageRequest.of(0, sel.getQuantidade()));
                collectedIds.addAll(ids);
            }
        }

        List<Questao> questions = questaoRepository.findAllById(collectedIds);
        simulado.setQuestoes(new HashSet<>(questions));

        return simuladoMapper.toDetailDto(simuladoRepository.save(simulado));
    }

    public void delete(Long id) {
        log.info("Excluindo simulado ID: {}", id);
        if (!simuladoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Simulado", "ID", id);
        }
        
        respostaRepository.detachSimulado(id);
        simuladoRepository.deleteById(id);
    }

    public SimuladoDetailDto iniciarSimulado(Long id) {
        Simulado simulado = simuladoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado", "ID", id));
        
        if (simulado.getStartedAt() == null) {
            simulado.setStartedAt(LocalDateTime.now());
            simulado = simuladoRepository.save(simulado);
        }
        return simuladoMapper.toDetailDto(simulado);
    }

    public SimuladoDetailDto finalizarSimulado(Long id) {
        Simulado simulado = simuladoRepository.findByIdWithQuestoes(id)
                .orElseThrow(() -> new ResourceNotFoundException("Simulado", "ID", id));
        
        if (simulado.getFinishedAt() != null) {
            return simuladoMapper.toDetailDto(simulado);
        }

        int totalQuestions = simulado.getQuestoes().size();
        int answeredCount = respostaRepository.countBySimuladoId(id);

        if (answeredCount < totalQuestions) {
            throw new ValidationException("Não é possível finalizar o simulado: existem " + (totalQuestions - answeredCount) + " questões sem resposta.");
        }

        simulado.setFinishedAt(LocalDateTime.now());
        simulado = simuladoRepository.save(simulado);
        return simuladoMapper.toDetailDto(simulado);
    }
}
