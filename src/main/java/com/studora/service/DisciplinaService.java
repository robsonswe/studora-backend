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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
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
    private final Executor dbStatsExecutor;

    public DisciplinaService(DisciplinaRepository disciplinaRepository, TemaRepository temaRepository,
                             SubtemaRepository subtemaRepository, EstudoSubtemaRepository estudoSubtemaRepository,
                             QuestaoRepository questaoRepository, RespostaRepository respostaRepository,
                             DisciplinaMapper disciplinaMapper, TemaService temaService,
                             @Qualifier("dbStatsExecutor") Executor dbStatsExecutor) {
        this.disciplinaRepository = disciplinaRepository;
        this.temaRepository = temaRepository;
        this.subtemaRepository = subtemaRepository;
        this.estudoSubtemaRepository = estudoSubtemaRepository;
        this.questaoRepository = questaoRepository;
        this.respostaRepository = respostaRepository;
        this.disciplinaMapper = disciplinaMapper;
        this.temaService = temaService;
        this.dbStatsExecutor = dbStatsExecutor;
    }

    @Cacheable(value = "disciplina-stats", key = "T(java.util.Objects).hash(#nome, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
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

        var totalEstudosFut    = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countByDisciplinaIds(ids)), dbStatsExecutor);
        var ultimoEstudoFut    = CompletableFuture.supplyAsync(() -> toDateMap(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(ids)), dbStatsExecutor);
        var ultimaQuestaoFut   = CompletableFuture.supplyAsync(() -> toDateMap(respostaRepository.findLatestResponseDatesByDisciplinaIds(ids)), dbStatsExecutor);
        var totalTemasFut      = CompletableFuture.supplyAsync(() -> toCountMap(temaRepository.countByDisciplinaIds(ids)), dbStatsExecutor);
        var totalSubtemasFut   = CompletableFuture.supplyAsync(() -> toCountMap(subtemaRepository.countByDisciplinaIds(ids)), dbStatsExecutor);
        var subtemasEstudadosFut = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(ids)), dbStatsExecutor);
        var temasEstudadosFut  = CompletableFuture.supplyAsync(() -> toCountMap(temaRepository.countTemasEstudadosByDisciplinaIds(ids)), dbStatsExecutor);
        
        var totalQuestoesFut   = CompletableFuture.supplyAsync(() -> toCountMap(questaoRepository.countQuestoesByDisciplinaIds(ids)), dbStatsExecutor);
        var respondidasFut     = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countRespondidasByDisciplinaIds(ids)), dbStatsExecutor);
        var acertadasFut       = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countAcertadasByDisciplinaIds(ids)), dbStatsExecutor);
        var avgTempoFut        = CompletableFuture.supplyAsync(() -> toDoubleMap(respostaRepository.avgTempoByDisciplinaIds(ids)), dbStatsExecutor);
        
        var dificuldadeFut     = CompletableFuture.supplyAsync(() -> parseDificuldadeStats(respostaRepository.getDificuldadeStatsByDisciplinaIds(ids)), dbStatsExecutor);

        CompletableFuture.allOf(totalEstudosFut, ultimoEstudoFut, ultimaQuestaoFut, totalTemasFut, totalSubtemasFut, subtemasEstudadosFut, temasEstudadosFut, totalQuestoesFut, respondidasFut, acertadasFut, avgTempoFut, dificuldadeFut).join();

        Map<Long, Long> totalEstudosMap = totalEstudosFut.join();
        Map<Long, LocalDateTime> ultimoEstudoMap = ultimoEstudoFut.join();
        Map<Long, LocalDateTime> ultimaQuestaoMap = ultimaQuestaoFut.join();
        Map<Long, Long> totalTemasMap = totalTemasFut.join();
        Map<Long, Long> totalSubtemasMap = totalSubtemasFut.join();
        Map<Long, Long> subtemasEstudadosMap = subtemasEstudadosFut.join();
        Map<Long, Long> temasEstudadosMap = temasEstudadosFut.join();
        Map<Long, Long> totalQuestoesMap = totalQuestoesFut.join();
        Map<Long, Long> respondidasMap = respondidasFut.join();
        Map<Long, Long> acertadasMap = acertadasFut.join();
        Map<Long, Double> avgTempoMap = avgTempoFut.join();
        Map<Long, Map<String, DificuldadeStatDto>> dificuldadeMap = dificuldadeFut.join();

        return page.map(disciplina -> {
            Long discId = disciplina.getId();
            DisciplinaSummaryDto dto = disciplinaMapper.toSummaryDto(disciplina);
            dto.setTotalEstudos(totalEstudosMap.getOrDefault(discId, 0L));
            dto.setUltimoEstudo(ultimoEstudoMap.get(discId));
            dto.setUltimaQuestao(ultimaQuestaoMap.get(discId));
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
        dto.setUltimaQuestao(toDateMap(respostaRepository.findLatestResponseDatesByDisciplinaIds(discIds)).get(id));
        dto.setTotalTemas(toCountMap(temaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setTotalSubtemas(toCountMap(subtemaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setSubtemasEstudados(toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setTemasEstudados(toCountMap(temaRepository.countTemasEstudadosByDisciplinaIds(discIds)).getOrDefault(id, 0L));

        // Enrich questao stats for disciplina
        dto.setTotalQuestoes(toCountMap(questaoRepository.countQuestoesByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setQuestoesRespondidas(toCountMap(respostaRepository.countRespondidasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        dto.setQuestoesAcertadas(toCountMap(respostaRepository.countAcertadasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
        Map<Long, Double> avgTempoMap = toDoubleMap(respostaRepository.avgTempoByDisciplinaIds(discIds));
        dto.setMediaTempoResposta(avgTempoMap.containsKey(id) ? avgTempoMap.get(id).intValue() : null);
        
        dto.setDificuldadeRespostas(parseDificuldadeStats(respostaRepository.getDificuldadeStatsByDisciplinaIds(discIds)).getOrDefault(id, initEmptyDificuldadeMap()));

        // Enrich nested temas
        dto.setTemas(temaService.findByDisciplinaId(id));

        return dto;
    }

    @CacheEvict(value = "disciplina-stats", allEntries = true)
    public DisciplinaDetailDto create(DisciplinaCreateRequest request) {
        log.info("Criando nova disciplina: {}", request.getNome());

        Optional<Disciplina> existing = disciplinaRepository.findByNomeIgnoreCase(request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe uma disciplina com o nome '" + request.getNome() + "'");
        }

        Disciplina disciplina = disciplinaMapper.toEntity(request);
        return disciplinaMapper.toDetailDto(disciplinaRepository.save(disciplina));
    }

    @CacheEvict(value = "disciplina-stats", allEntries = true)
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

    @CacheEvict(value = "disciplina-stats", allEntries = true)
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

    private Map<String, DificuldadeStatDto> initEmptyDificuldadeMap() {
        Map<String, DificuldadeStatDto> map = new HashMap<>();
        for (Dificuldade d : Dificuldade.values()) {
            map.put(d.name(), new DificuldadeStatDto(0, 0));
        }
        return map;
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

    private Map<Long, Map<String, DificuldadeStatDto>> parseDificuldadeStats(List<Object[]> rows) {
        Map<Long, Map<String, DificuldadeStatDto>> result = new HashMap<>();
        for (Object[] row : rows) {
            Long scopeId = ((Number) row[0]).longValue();
            int diffId = ((Number) row[1]).intValue();
            int total = ((Number) row[2]).intValue();
            int corretas = row[3] != null ? ((Number) row[3]).intValue() : 0;
            
            String diffName = Dificuldade.fromId(diffId).name();
            
            result.computeIfAbsent(scopeId, k -> initEmptyDificuldadeMap())
                  .put(diffName, new DificuldadeStatDto(total, corretas));
        }
        return result;
    }
}