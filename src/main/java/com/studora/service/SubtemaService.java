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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.beans.factory.annotation.Qualifier;

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
public class SubtemaService {

    private final SubtemaRepository subtemaRepository;
    private final TemaRepository temaRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final com.studora.repository.EstudoSubtemaRepository estudoSubtemaRepository;
    private final SubtemaMapper subtemaMapper;
    private final Executor dbStatsExecutor;

    public SubtemaService(SubtemaRepository subtemaRepository, TemaRepository temaRepository,
                          QuestaoRepository questaoRepository, RespostaRepository respostaRepository,
                          com.studora.repository.EstudoSubtemaRepository estudoSubtemaRepository,
                          SubtemaMapper subtemaMapper,
                          @Qualifier("dbStatsExecutor") Executor dbStatsExecutor) {
        this.subtemaRepository = subtemaRepository;
        this.temaRepository = temaRepository;
        this.questaoRepository = questaoRepository;
        this.respostaRepository = respostaRepository;
        this.estudoSubtemaRepository = estudoSubtemaRepository;
        this.subtemaMapper = subtemaMapper;
        this.dbStatsExecutor = dbStatsExecutor;
    }

    @Cacheable(value = "subtema-stats", key = "T(java.util.Objects).hash('all', #nome, #pageable.pageNumber, #pageable.pageSize, #pageable.sort)")
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
        
        var countsFut           = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countBySubtemaIds(ids)), dbStatsExecutor);
        var datesFut            = CompletableFuture.supplyAsync(() -> toDateMap(estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(ids)), dbStatsExecutor);
        var ultimaQuestaoDatesFut = CompletableFuture.supplyAsync(() -> toDateMap(respostaRepository.findLatestResponseDatesBySubtemaIds(ids)), dbStatsExecutor);
        var totalQuestoesFut    = CompletableFuture.supplyAsync(() -> toCountMap(questaoRepository.countQuestoesBySubtemaIds(ids)), dbStatsExecutor);
        var respondidasFut      = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countRespondidasBySubtemaIds(ids)), dbStatsExecutor);
        var acertadasFut        = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countAcertadasBySubtemaIds(ids)), dbStatsExecutor);
        var avgTempoFut         = CompletableFuture.supplyAsync(() -> toDoubleMap(respostaRepository.avgTempoBySubtemaIds(ids)), dbStatsExecutor);
        var dificuldadeFut      = CompletableFuture.supplyAsync(() -> parseDificuldadeStats(respostaRepository.getDificuldadeStatsBySubtemaIds(ids)), dbStatsExecutor);

        CompletableFuture.allOf(countsFut, datesFut, ultimaQuestaoDatesFut, totalQuestoesFut, respondidasFut, acertadasFut, avgTempoFut, dificuldadeFut).join();

        Map<Long, Long> counts = countsFut.join();
        Map<Long, java.time.LocalDateTime> dates = datesFut.join();
        Map<Long, java.time.LocalDateTime> ultimaQuestaoDates = ultimaQuestaoDatesFut.join();
        Map<Long, Long> totalQuestoesMap = totalQuestoesFut.join();
        Map<Long, Long> respondidasMap = respondidasFut.join();
        Map<Long, Long> acertadasMap = acertadasFut.join();
        Map<Long, Double> avgTempoMap = avgTempoFut.join();
        Map<Long, Map<String, DificuldadeStatDto>> dificuldadeMap = dificuldadeFut.join();

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
        dto.setDificuldadeRespostas(parseDificuldadeStats(respostaRepository.getDificuldadeStatsBySubtemaIds(singleId)).getOrDefault(id, initEmptyDificuldadeMap()));

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

    @CacheEvict(value = "subtema-stats", allEntries = true)
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

    @CacheEvict(value = "subtema-stats", allEntries = true)
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

    @Cacheable(value = "subtema-stats", key = "T(java.lang.String).format('tema-%d', #temaId)")
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
        
        var countsFut           = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countBySubtemaIds(ids)), dbStatsExecutor);
        var datesFut            = CompletableFuture.supplyAsync(() -> toDateMap(estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(ids)), dbStatsExecutor);
        var ultimaQuestaoDatesFut = CompletableFuture.supplyAsync(() -> toDateMap(respostaRepository.findLatestResponseDatesBySubtemaIds(ids)), dbStatsExecutor);
        var totalQuestoesFut    = CompletableFuture.supplyAsync(() -> toCountMap(questaoRepository.countQuestoesBySubtemaIds(ids)), dbStatsExecutor);
        var respondidasFut      = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countRespondidasBySubtemaIds(ids)), dbStatsExecutor);
        var acertadasFut        = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countAcertadasBySubtemaIds(ids)), dbStatsExecutor);
        var avgTempoFut         = CompletableFuture.supplyAsync(() -> toDoubleMap(respostaRepository.avgTempoBySubtemaIds(ids)), dbStatsExecutor);
        var dificuldadeFut      = CompletableFuture.supplyAsync(() -> parseDificuldadeStats(respostaRepository.getDificuldadeStatsBySubtemaIds(ids)), dbStatsExecutor);

        CompletableFuture.allOf(countsFut, datesFut, ultimaQuestaoDatesFut, totalQuestoesFut, respondidasFut, acertadasFut, avgTempoFut, dificuldadeFut).join();

        Map<Long, Long> counts = countsFut.join();
        Map<Long, java.time.LocalDateTime> dates = datesFut.join();
        Map<Long, java.time.LocalDateTime> ultimaQuestaoDates = ultimaQuestaoDatesFut.join();
        Map<Long, Long> totalQuestoesMap = totalQuestoesFut.join();
        Map<Long, Long> respondidasMap = respondidasFut.join();
        Map<Long, Long> acertadasMap = acertadasFut.join();
        Map<Long, Double> avgTempoMap = avgTempoFut.join();
        Map<Long, Map<String, DificuldadeStatDto>> dificuldadeMap = dificuldadeFut.join();

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

    @CacheEvict(value = "subtema-stats", allEntries = true)
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

    private Map<String, DificuldadeStatDto> initEmptyDificuldadeMap() {
        Map<String, DificuldadeStatDto> map = new HashMap<>();
        for (Dificuldade d : Dificuldade.values()) {
            map.put(d.name(), new DificuldadeStatDto(0, 0));
        }
        return map;
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