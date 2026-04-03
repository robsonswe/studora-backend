package com.studora.service;

import com.studora.dto.DificuldadeStatDto;
import com.studora.dto.subtema.SubtemaDetailDto;
import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.dto.request.SubtemaCreateRequest;
import com.studora.dto.request.SubtemaUpdateRequest;
import com.studora.entity.Dificuldade;
import com.studora.entity.Resposta;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.SubtemaMapper;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubtemaService {

    private final SubtemaRepository subtemaRepository;
    private final TemaRepository temaRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final com.studora.repository.EstudoSubtemaRepository estudoSubtemaRepository;
    private final SubtemaMapper subtemaMapper;

    @Transactional(readOnly = true)
    public Page<SubtemaSummaryDto> findAll(String nome, Pageable pageable) {
        Page<Subtema> page;
        if (nome != null && !nome.isBlank()) {
            page = subtemaRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            page = subtemaRepository.findAll(pageable);
        }

        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> ids = page.getContent().stream().map(Subtema::getId).collect(Collectors.toList());
        
        // Fetch study counts
        Map<Long, Long> counts = toCountMap(estudoSubtemaRepository.countBySubtemaIds(ids));
        
        // Fetch latest dates
        Map<Long, java.time.LocalDateTime> dates = toDateMap(estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(ids));
        Map<Long, java.time.LocalDateTime> ultimaQuestaoDates = toDateMap(respostaRepository.findLatestResponseDatesBySubtemaIds(ids));

        // Fetch questao stats
        Map<Long, Long> totalQuestoesMap = toCountMap(questaoRepository.countQuestoesBySubtemaIds(ids));
        Map<Long, Long> respondidasMap = toCountMap(respostaRepository.countRespondidasBySubtemaIds(ids));
        Map<Long, Long> acertadasMap = toCountMap(respostaRepository.countAcertadasBySubtemaIds(ids));
        Map<Long, Double> avgTempoMap = toDoubleMap(respostaRepository.avgTempoBySubtemaIds(ids));

        // Fetch difficulty stats
        List<Resposta> allRespostas = respostaRepository.findAllBySubtemaIdsWithDetails(ids);
        Map<Long, Map<String, DificuldadeStatDto>> dificuldadeMap = computeDificuldadeStatsBatch(allRespostas,
                r -> r.getQuestao().getSubtemas().stream().map(Subtema::getId).toList());

        return page.map(subtema -> {
            Long subId = subtema.getId();
            SubtemaSummaryDto dto = subtemaMapper.toSummaryDto(subtema);
            dto.setTotalEstudos(counts.getOrDefault(subId, 0L));
            dto.setUltimoEstudo(dates.get(subId));
            dto.setUltimaQuestao(ultimaQuestaoDates.get(subId));
            dto.setTotalQuestoes(totalQuestoesMap.getOrDefault(subId, 0L));
            dto.setQuestoesRespondidas(respondidasMap.getOrDefault(subId, 0L));
            dto.setQuestoesAcertadas(acertadasMap.getOrDefault(subId, 0L));
            dto.setMediaTempoResposta(avgTempoMap.containsKey(subId) ? avgTempoMap.get(subId).intValue() : null);
            dto.setDificuldadeRespostas(dificuldadeMap.getOrDefault(subId, Collections.emptyMap()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public SubtemaDetailDto getSubtemaDetailById(Long id) {
        Subtema subtema = subtemaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", id));
        
        SubtemaDetailDto dto = subtemaMapper.toDetailDto(subtema);
        dto.setTotalEstudos(estudoSubtemaRepository.countBySubtemaId(id));
        dto.setUltimoEstudo(estudoSubtemaRepository.findFirstBySubtemaIdOrderByCreatedAtDesc(id)
                .map(com.studora.entity.EstudoSubtema::getCreatedAt).orElse(null));
        dto.setUltimaQuestao(toDateMap(respostaRepository.findLatestResponseDatesBySubtemaIds(List.of(id))).get(id));

        // Enrich questao stats for this subtema
        List<Long> singleId = List.of(id);
        dto.setTotalQuestoes(toCountMap(questaoRepository.countQuestoesBySubtemaIds(singleId)).getOrDefault(id, 0L));
        dto.setQuestoesRespondidas(toCountMap(respostaRepository.countRespondidasBySubtemaIds(singleId)).getOrDefault(id, 0L));
        dto.setQuestoesAcertadas(toCountMap(respostaRepository.countAcertadasBySubtemaIds(singleId)).getOrDefault(id, 0L));
        Map<Long, Double> avgTempoMap = toDoubleMap(respostaRepository.avgTempoBySubtemaIds(singleId));
        dto.setMediaTempoResposta(avgTempoMap.containsKey(id) ? avgTempoMap.get(id).intValue() : null);
        List<Resposta> respostas = respostaRepository.findAllBySubtemaIdsWithDetails(singleId);
        dto.setDificuldadeRespostas(computeDificuldadeStatsFromResponses(respostas));

        // Enrich nested tema
        if (dto.getTema() != null) {
            Long temaId = dto.getTema().getId();
            List<Long> singleTemaId = List.of(temaId);
            dto.getTema().setTotalEstudos(toCountMap(estudoSubtemaRepository.countByTemaIds(singleTemaId)).getOrDefault(temaId, 0L));
            dto.getTema().setUltimoEstudo(toDateMap(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(singleTemaId)).get(temaId));
            dto.getTema().setUltimaQuestao(toDateMap(respostaRepository.findLatestResponseDatesByTemaIds(singleTemaId)).get(temaId));
            dto.getTema().setTotalSubtemas(toCountMap(subtemaRepository.countByTemaIds(singleTemaId)).getOrDefault(temaId, 0L));
            dto.getTema().setSubtemasEstudados(toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(singleTemaId)).getOrDefault(temaId, 0L));
        }

        return dto;
    }

    public SubtemaDetailDto create(SubtemaCreateRequest request) {
        log.info("Criando novo subtema: {} no tema ID: {}", request.getNome(), request.getTemaId());
        
        Optional<Subtema> existing = subtemaRepository.findByTemaIdAndNomeIgnoreCase(request.getTemaId(), request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe um subtema com o nome '" + request.getNome() + "' no tema com ID: " + request.getTemaId());
        }

        Tema tema = temaRepository.findById(request.getTemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", request.getTemaId()));

        Subtema subtema = subtemaMapper.toEntity(request);
        subtema.setTema(tema);
        
        return subtemaMapper.toDetailDto(subtemaRepository.save(subtema));
    }

    public SubtemaDetailDto update(Long id, SubtemaUpdateRequest request) {
        log.info("Atualizando subtema ID: {}", id);
        
        Subtema subtema = subtemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", id));

        if (request.getNome() != null || request.getTemaId() != null) {
            Long tId = request.getTemaId() != null ? request.getTemaId() : subtema.getTema().getId();
            String nome = request.getNome() != null ? request.getNome() : subtema.getNome();
            
            Optional<Subtema> existing = subtemaRepository.findByTemaIdAndNomeIgnoreCase(tId, nome);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new ConflictException("Já existe um subtema com o nome '" + nome + "' no tema com ID: " + tId);
            }
        }

        if (request.getTemaId() != null) {
            Tema tema = temaRepository.findById(request.getTemaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", request.getTemaId()));
            subtema.setTema(tema);
        }

        subtemaMapper.updateEntityFromDto(request, subtema);
        return subtemaMapper.toDetailDto(subtemaRepository.save(subtema));
    }

    @Transactional(readOnly = true)
    public List<SubtemaSummaryDto> findByTemaId(Long temaId) {
        if (!temaRepository.existsById(temaId)) {
            throw new ResourceNotFoundException("Tema", "ID", temaId);
        }
        List<Subtema> subtemas = subtemaRepository.findByTemaId(temaId);
        if (subtemas.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        List<Long> ids = subtemas.stream().map(Subtema::getId).collect(Collectors.toList());
        
        Map<Long, Long> counts = toCountMap(estudoSubtemaRepository.countBySubtemaIds(ids));
        Map<Long, java.time.LocalDateTime> dates = toDateMap(estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(ids));
        Map<Long, java.time.LocalDateTime> ultimaQuestaoDates = toDateMap(respostaRepository.findLatestResponseDatesBySubtemaIds(ids));

        // Fetch questao stats
        Map<Long, Long> totalQuestoesMap = toCountMap(questaoRepository.countQuestoesBySubtemaIds(ids));
        Map<Long, Long> respondidasMap = toCountMap(respostaRepository.countRespondidasBySubtemaIds(ids));
        Map<Long, Long> acertadasMap = toCountMap(respostaRepository.countAcertadasBySubtemaIds(ids));
        Map<Long, Double> avgTempoMap = toDoubleMap(respostaRepository.avgTempoBySubtemaIds(ids));

        // Fetch difficulty stats
        List<Resposta> allRespostas = respostaRepository.findAllBySubtemaIdsWithDetails(ids);
        Map<Long, Map<String, DificuldadeStatDto>> dificuldadeMap = computeDificuldadeStatsBatch(allRespostas,
                r -> r.getQuestao().getSubtemas().stream().map(Subtema::getId).toList());

        return subtemas.stream()
                .map(subtema -> {
                    Long subId = subtema.getId();
                    SubtemaSummaryDto dto = subtemaMapper.toSummaryDto(subtema);
                    dto.setTotalEstudos(counts.getOrDefault(subId, 0L));
                    dto.setUltimoEstudo(dates.get(subId));
                    dto.setUltimaQuestao(ultimaQuestaoDates.get(subId));
                    dto.setTotalQuestoes(totalQuestoesMap.getOrDefault(subId, 0L));
                    dto.setQuestoesRespondidas(respondidasMap.getOrDefault(subId, 0L));
                    dto.setQuestoesAcertadas(acertadasMap.getOrDefault(subId, 0L));
                    dto.setMediaTempoResposta(avgTempoMap.containsKey(subId) ? avgTempoMap.get(subId).intValue() : null);
                    dto.setDificuldadeRespostas(dificuldadeMap.getOrDefault(subId, Collections.emptyMap()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        log.info("Excluindo subtema ID: {}", id);
        if (!subtemaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subtema", "ID", id);
        }
        
        if (questaoRepository.existsBySubtemasId(id)) {
            throw new ValidationException("Não é possível excluir um subtema que possui questões associadas");
        }
        
        subtemaRepository.deleteById(id);
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).longValue()));
    }

    private Map<Long, java.time.LocalDateTime> toDateMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> parseDate(row[1])));
    }

    private Map<Long, Double> toDoubleMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).doubleValue()));
    }

    private java.time.LocalDateTime parseDate(Object val) {
        if (val instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) val;
        if (val instanceof String) return java.time.LocalDateTime.parse((String) val, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return null;
    }

    private Map<String, DificuldadeStatDto> computeDificuldadeStatsFromResponses(List<Resposta> responses) {
        Map<String, DificuldadeStatDto> result = new HashMap<>();
        for (Dificuldade d : Dificuldade.values()) {
            result.put(d.name(), new DificuldadeStatDto(0, 0));
        }

        // Group by questao, take most recent per question
        Map<Long, Resposta> latestByQuestao = new HashMap<>();
        for (Resposta r : responses) {
            latestByQuestao.merge(r.getQuestao().getId(), r, (existing, candidate) ->
                    candidate.getCreatedAt().isAfter(existing.getCreatedAt()) ? candidate : existing);
        }

        // Group by dificuldade
        for (Resposta r : latestByQuestao.values()) {
            String diff = r.getDificuldade() != null ? r.getDificuldade().name() : Dificuldade.MEDIA.name();
            DificuldadeStatDto stat = result.get(diff);
            stat.setTotal(stat.getTotal() + 1);
            if (isCorrect(r)) {
                stat.setCorretas(stat.getCorretas() + 1);
            }
        }
        return result;
    }

    private Map<Long, Map<String, DificuldadeStatDto>> computeDificuldadeStatsBatch(
            List<Resposta> responses, java.util.function.Function<Resposta, List<Long>> scopeIdsExtractor) {

        // Group responses by scope entity ID
        Map<Long, List<Resposta>> byScope = new HashMap<>();
        for (Resposta r : responses) {
            for (Long scopeId : scopeIdsExtractor.apply(r)) {
                byScope.computeIfAbsent(scopeId, k -> new java.util.ArrayList<>()).add(r);
            }
        }

        Map<Long, Map<String, DificuldadeStatDto>> result = new HashMap<>();
        for (var entry : byScope.entrySet()) {
            result.put(entry.getKey(), computeDificuldadeStatsFromResponses(entry.getValue()));
        }
        return result;
    }

    private boolean isCorrect(Resposta r) {
        if (r.getAlternativaEscolhida() == null) return false;
        return Boolean.TRUE.equals(r.getAlternativaEscolhida().getCorreta());
    }
}