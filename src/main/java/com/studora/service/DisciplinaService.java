package com.studora.service;

import com.studora.dto.DificuldadeStatDto;
import com.studora.dto.disciplina.DisciplinaDetailDto;
import com.studora.dto.disciplina.DisciplinaSummaryDto;
import com.studora.dto.request.DisciplinaCreateRequest;
import com.studora.dto.request.DisciplinaUpdateRequest;
import com.studora.entity.Dificuldade;
import com.studora.entity.Disciplina;
import com.studora.entity.Resposta;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.DisciplinaMapper;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.EstudoSubtemaRepository;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final TemaRepository temaRepository;
    private final SubtemaRepository subtemaRepository;
    private final EstudoSubtemaRepository estudoSubtemaRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final DisciplinaMapper disciplinaMapper;
    private final TemaService temaService;

    @Transactional(readOnly = true)
    public Page<DisciplinaSummaryDto> findAll(String nome, Pageable pageable) {
        Page<Disciplina> page;
        if (nome != null && !nome.isBlank()) {
            page = disciplinaRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            page = disciplinaRepository.findAll(pageable);
        }

        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> ids = page.getContent().stream().map(Disciplina::getId).toList();
        Map<Long, Long> totalEstudosMap = toCountMap(estudoSubtemaRepository.countByDisciplinaIds(ids));
        Map<Long, LocalDateTime> ultimoEstudoMap = toDateMap(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(ids));
        Map<Long, Long> totalTemasMap = toCountMap(temaRepository.countByDisciplinaIds(ids));
        Map<Long, Long> totalSubtemasMap = toCountMap(subtemaRepository.countByDisciplinaIds(ids));
        Map<Long, Long> subtemasEstudadosMap = toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(ids));

        // temasEstudados: count of temas where ALL subtemas have been studied at least once
        Map<Long, Long> temasEstudadosMap = computeTemasEstudadosByDisciplina(ids);

        // Fetch questao stats
        Map<Long, Long> totalQuestoesMap = toCountMap(questaoRepository.countQuestoesByDisciplinaIds(ids));
        Map<Long, Long> respondidasMap = toCountMap(respostaRepository.countRespondidasByDisciplinaIds(ids));
        Map<Long, Long> acertadasMap = toCountMap(respostaRepository.countAcertadasByDisciplinaIds(ids));
        Map<Long, Double> avgTempoMap = toDoubleMap(respostaRepository.avgTempoByDisciplinaIds(ids));

        // Fetch difficulty stats
        List<Resposta> allRespostas = respostaRepository.findAllByDisciplinaIdsWithDetails(ids);
        Map<Long, Map<String, DificuldadeStatDto>> dificuldadeMap = computeDificuldadeStatsBatch(allRespostas,
                r -> r.getQuestao().getSubtemas().stream().map(s -> s.getTema().getDisciplina().getId()).distinct().toList());

        return page.map(disciplina -> {
            Long discId = disciplina.getId();
            DisciplinaSummaryDto dto = disciplinaMapper.toSummaryDto(disciplina);
            dto.setTotalEstudos(totalEstudosMap.getOrDefault(discId, 0L));
            dto.setUltimoEstudo(ultimoEstudoMap.get(discId));
            dto.setTotalTemas(totalTemasMap.getOrDefault(discId, 0L));
            dto.setTotalSubtemas(totalSubtemasMap.getOrDefault(discId, 0L));
            dto.setSubtemasEstudados(subtemasEstudadosMap.getOrDefault(discId, 0L));
            dto.setTemasEstudados(temasEstudadosMap.getOrDefault(discId, 0L));
            dto.setTotalQuestoes(totalQuestoesMap.getOrDefault(discId, 0L));
            dto.setQuestoesRespondidas(respondidasMap.getOrDefault(discId, 0L));
            dto.setQuestoesAcertadas(acertadasMap.getOrDefault(discId, 0L));
            dto.setMediaTempoResposta(avgTempoMap.containsKey(discId) ? avgTempoMap.get(discId).intValue() : null);
            dto.setDificuldadeRespostas(dificuldadeMap.getOrDefault(discId, Collections.emptyMap()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public DisciplinaDetailDto getDisciplinaDetailById(Long id) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));

        DisciplinaDetailDto dto = disciplinaMapper.toDetailDto(disciplina);

        // Enrich with study stats
        List<Long> discIds = List.of(id);
        dto.setTotalEstudos(toCountMap(estudoSubtemaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setUltimoEstudo(toDateMap(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(discIds)).get(id));
        dto.setTotalTemas(toCountMap(temaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setTotalSubtemas(toCountMap(subtemaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setSubtemasEstudados(toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setTemasEstudados(computeTemasEstudadosByDisciplina(discIds).getOrDefault(id, 0L));

        // Enrich questao stats for disciplina
        dto.setTotalQuestoes(toCountMap(questaoRepository.countQuestoesByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setQuestoesRespondidas(toCountMap(respostaRepository.countRespondidasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setQuestoesAcertadas(toCountMap(respostaRepository.countAcertadasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        Map<Long, Double> avgTempoMap = toDoubleMap(respostaRepository.avgTempoByDisciplinaIds(discIds));
        dto.setMediaTempoResposta(avgTempoMap.containsKey(id) ? avgTempoMap.get(id).intValue() : null);
        List<Resposta> discRespostas = respostaRepository.findAllByDisciplinaIdsWithDetails(discIds);
        dto.setDificuldadeRespostas(computeDificuldadeStatsFromResponses(discRespostas));

        // Enrich nested temas
        dto.setTemas(temaService.findByDisciplinaId(id));

        return dto;
    }

    public DisciplinaDetailDto create(DisciplinaCreateRequest request) {
        log.info("Criando nova disciplina: {}", request.getNome());

        Optional<Disciplina> existing = disciplinaRepository.findByNomeIgnoreCase(request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe uma disciplina com o nome '" + request.getNome() + "'");
        }

        Disciplina disciplina = disciplinaMapper.toEntity(request);
        return disciplinaMapper.toDetailDto(disciplinaRepository.save(disciplina));
    }

    public DisciplinaDetailDto update(Long id, DisciplinaUpdateRequest request) {
        log.info("Atualizando disciplina ID: {}", id);

        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));

        if (request.getNome() != null) {
            Optional<Disciplina> existing = disciplinaRepository.findByNomeIgnoreCase(request.getNome());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new ConflictException("Já existe uma disciplina com o nome '" + request.getNome() + "'");
            }
        }

        disciplinaMapper.updateEntityFromDto(request, disciplina);
        return disciplinaMapper.toDetailDto(disciplinaRepository.save(disciplina));
    }

    public void delete(Long id) {
        log.info("Excluindo disciplina ID: {}", id);
        if (!disciplinaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Disciplina", "ID", id);
        }

        if (temaRepository.existsByDisciplinaId(id)) {
            throw new ValidationException("Não é possível excluir uma disciplina que possui temas associados");
        }

        disciplinaRepository.deleteById(id);
    }

    /**
     * For each disciplina, counts how many of its temas have ALL their subtemas studied at least once.
     * A tema without subtemas is NOT counted as "estudado".
     */
    private Map<Long, Long> computeTemasEstudadosByDisciplina(List<Long> disciplinaIds) {
        // 1. Get all temas for these disciplinas
        List<com.studora.entity.Tema> temas = temaRepository.findByDisciplinaIds(disciplinaIds);
        if (temas.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> temaIds = temas.stream().map(com.studora.entity.Tema::getId).toList();

        // 2. Count total subtemas per tema
        Map<Long, Long> totalSubtemasByTema = toCountMap(subtemaRepository.countByTemaIds(temaIds));

        // 3. Count studied subtemas per tema
        Map<Long, Long> studiedSubtemasByTema = toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(temaIds));

        // 4. For each disciplina, count temas where all subtemas are studied
        return temas.stream()
                .filter(t -> {
                    Long total = totalSubtemasByTema.get(t.getId());
                    Long studied = studiedSubtemasByTema.get(t.getId());
                    return total != null && total > 0 && total.equals(studied);
                })
                .collect(Collectors.groupingBy(
                        t -> t.getDisciplina().getId(),
                        Collectors.counting()));
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).longValue()));
    }

    private Map<Long, LocalDateTime> toDateMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> parseDate(row[1])));
    }

    private Map<Long, Double> toDoubleMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).doubleValue()));
    }

    private LocalDateTime parseDate(Object val) {
        if (val instanceof LocalDateTime) return (LocalDateTime) val;
        if (val instanceof String) return LocalDateTime.parse((String) val, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return null;
    }

    private Map<String, DificuldadeStatDto> computeDificuldadeStatsFromResponses(List<Resposta> responses) {
        Map<String, DificuldadeStatDto> result = new HashMap<>();
        for (Dificuldade d : Dificuldade.values()) {
            result.put(d.name(), new DificuldadeStatDto(0, 0));
        }

        Map<Long, Resposta> latestByQuestao = new HashMap<>();
        for (Resposta r : responses) {
            latestByQuestao.merge(r.getQuestao().getId(), r, (existing, candidate) ->
                    candidate.getCreatedAt().isAfter(existing.getCreatedAt()) ? candidate : existing);
        }

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
        Map<Long, List<Resposta>> byScope = new HashMap<>();
        for (Resposta r : responses) {
            for (Long scopeId : scopeIdsExtractor.apply(r)) {
                byScope.computeIfAbsent(scopeId, k -> new ArrayList<>()).add(r);
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