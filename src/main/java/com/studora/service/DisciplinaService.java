package com.studora.service;

import com.studora.dto.DificuldadeStatDto;
import com.studora.dto.MetricsLevel;
import com.studora.dto.disciplina.DisciplinaDetailDto;
import com.studora.dto.disciplina.DisciplinaSummaryDto;
import com.studora.dto.request.DisciplinaCreateRequest;
import com.studora.dto.request.DisciplinaUpdateRequest;
import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.dto.tema.TemaSummaryDto;
import com.studora.entity.Dificuldade;
import com.studora.entity.Disciplina;
import com.studora.entity.Resposta;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.DisciplinaMapper;
import com.studora.mapper.SubtemaMapper;
import com.studora.mapper.TemaMapper;
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
    private final TemaMapper temaMapper;
    private final SubtemaMapper subtemaMapper;
    private final TemaService temaService;
    private final Executor dbStatsExecutor;

    public DisciplinaService(DisciplinaRepository disciplinaRepository, TemaRepository temaRepository,
                             SubtemaRepository subtemaRepository, EstudoSubtemaRepository estudoSubtemaRepository,
                             QuestaoRepository questaoRepository, RespostaRepository respostaRepository,
                             DisciplinaMapper disciplinaMapper, TemaMapper temaMapper, SubtemaMapper subtemaMapper,
                             TemaService temaService,
                             @Qualifier("dbStatsExecutor") Executor dbStatsExecutor) {
        this.disciplinaRepository = disciplinaRepository;
        this.temaRepository = temaRepository;
        this.subtemaRepository = subtemaRepository;
        this.estudoSubtemaRepository = estudoSubtemaRepository;
        this.questaoRepository = questaoRepository;
        this.respostaRepository = respostaRepository;
        this.disciplinaMapper = disciplinaMapper;
        this.temaMapper = temaMapper;
        this.subtemaMapper = subtemaMapper;
        this.temaService = temaService;
        this.dbStatsExecutor = dbStatsExecutor;
    }

    @Cacheable(value = "disciplina-stats", key = "T(java.util.Objects).hash(#nome, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString(), #metrics)")
    @Transactional(readOnly = true)
    public Page<DisciplinaSummaryDto> findAll(String nome, Pageable pageable, MetricsLevel metrics) {
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

        // Lean tier: skip all metric queries
        if (metrics == null) {
            return page.map(disciplina -> {
                DisciplinaSummaryDto dto = disciplinaMapper.toSummaryDto(disciplina);
                return dto;
            });
        }

        boolean isFull = metrics == MetricsLevel.FULL;

        var totalEstudosFut    = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countByDisciplinaIds(ids)), dbStatsExecutor);
        var ultimoEstudoFut    = CompletableFuture.supplyAsync(() -> toDateMap(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(ids)), dbStatsExecutor);
        var totalSubtemasFut   = CompletableFuture.supplyAsync(() -> toCountMap(subtemaRepository.countByDisciplinaIds(ids)), dbStatsExecutor);
        var subtemasEstudadosFut = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(ids)), dbStatsExecutor);

        var totalQuestoesFut   = isFull ? CompletableFuture.supplyAsync(() -> toCountMap(questaoRepository.countQuestoesByDisciplinaIds(ids)), dbStatsExecutor) : null;
        var ultimaQuestaoFut   = isFull ? CompletableFuture.supplyAsync(() -> toDateMap(respostaRepository.findLatestResponseDatesByDisciplinaIds(ids)), dbStatsExecutor) : null;
        var totalTemasFut      = isFull ? CompletableFuture.supplyAsync(() -> toCountMap(temaRepository.countByDisciplinaIds(ids)), dbStatsExecutor) : null;
        var temasEstudadosFut  = isFull ? CompletableFuture.supplyAsync(() -> toCountMap(temaRepository.countTemasEstudadosByDisciplinaIds(ids)), dbStatsExecutor) : null;
        var respondidasFut     = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countRespondidasByDisciplinaIds(ids)), dbStatsExecutor);
        var acertadasFut       = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countAcertadasByDisciplinaIds(ids)), dbStatsExecutor);
        var avgTempoFut        = isFull ? CompletableFuture.supplyAsync(() -> toDoubleMap(respostaRepository.avgTempoByDisciplinaIds(ids)), dbStatsExecutor) : null;
        var dificuldadeFut     = isFull ? CompletableFuture.supplyAsync(() -> parseDificuldadeStats(respostaRepository.getDificuldadeStatsByDisciplinaIds(ids)), dbStatsExecutor) : null;

        CompletableFuture.allOf(
                totalEstudosFut, ultimoEstudoFut, totalSubtemasFut, subtemasEstudadosFut, respondidasFut, acertadasFut,
                isFull ? ultimaQuestaoFut : CompletableFuture.completedFuture(null),
                isFull ? totalTemasFut : CompletableFuture.completedFuture(null),
                isFull ? temasEstudadosFut : CompletableFuture.completedFuture(null),
                isFull ? totalQuestoesFut : CompletableFuture.completedFuture(null),
                isFull ? avgTempoFut : CompletableFuture.completedFuture(null),
                isFull ? dificuldadeFut : CompletableFuture.completedFuture(null)
        ).join();

        Map<Long, Long> totalEstudosMap = totalEstudosFut.join();
        Map<Long, LocalDateTime> ultimoEstudoMap = ultimoEstudoFut.join();
        Map<Long, Long> totalSubtemasMap = totalSubtemasFut.join();
        Map<Long, Long> subtemasEstudadosMap = subtemasEstudadosFut.join();
        Map<Long, Long> respondidasMap = respondidasFut.join();
        Map<Long, Long> acertadasMap = acertadasFut.join();

        Map<Long, LocalDateTime> ultimaQuestaoMap = isFull ? ultimaQuestaoFut.join() : Collections.emptyMap();
        Map<Long, Long> totalTemasMap = isFull ? totalTemasFut.join() : Collections.emptyMap();
        Map<Long, Long> temasEstudadosMap = isFull ? temasEstudadosFut.join() : Collections.emptyMap();
        Map<Long, Long> totalQuestoesMap = isFull ? totalQuestoesFut.join() : Collections.emptyMap();
        Map<Long, Double> avgTempoMap = isFull ? avgTempoFut.join() : Collections.emptyMap();
        Map<Long, Map<String, DificuldadeStatDto>> dificuldadeMap = isFull ? dificuldadeFut.join() : Collections.emptyMap();

        return page.map(disciplina -> {
            Long discId = disciplina.getId();
            DisciplinaSummaryDto dto = disciplinaMapper.toSummaryDto(disciplina);

            // Summary tier fields
            dto.setTotalEstudos(totalEstudosMap.getOrDefault(discId, 0L));
            dto.setUltimoEstudo(ultimoEstudoMap.get(discId));
            dto.setTotalSubtemas(totalSubtemasMap.getOrDefault(discId, 0L));
            dto.setSubtemasEstudados(subtemasEstudadosMap.getOrDefault(discId, 0L));
            dto.setQuestoesRespondidas(respondidasMap.getOrDefault(discId, 0L));
            dto.setQuestoesAcertadas(acertadasMap.getOrDefault(discId, 0L));

            // Full tier fields
            if (isFull) {
                dto.setUltimaQuestao(ultimaQuestaoMap.get(discId));
                dto.setTotalTemas(totalTemasMap.getOrDefault(discId, 0L));
                dto.setTemasEstudados(temasEstudadosMap.getOrDefault(discId, 0L));
                dto.setTotalQuestoes(totalQuestoesMap.getOrDefault(discId, 0L));
                dto.setMediaTempoResposta(avgTempoMap.containsKey(discId) ? avgTempoMap.get(discId).intValue() : null);
                dto.setDificuldadeRespostas(dificuldadeMap.getOrDefault(discId, Collections.emptyMap()));
            }
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public DisciplinaDetailDto getDisciplinaDetailById(Long id, MetricsLevel metrics) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));

        DisciplinaDetailDto dto = disciplinaMapper.toDetailDto(disciplina);
        boolean isFull = metrics != null;

        if (isFull) {
            List<Long> discIds = List.of(id);
            dto.setTotalEstudos(toCountMap(estudoSubtemaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setUltimoEstudo(toDateMap(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(discIds)).get(id));
            dto.setUltimaQuestao(toDateMap(respostaRepository.findLatestResponseDatesByDisciplinaIds(discIds)).get(id));
            dto.setTotalTemas(toCountMap(temaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setTotalSubtemas(toCountMap(subtemaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setSubtemasEstudados(toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setTemasEstudados(toCountMap(temaRepository.countTemasEstudadosByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setTotalQuestoes(toCountMap(questaoRepository.countQuestoesByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setQuestoesRespondidas(toCountMap(respostaRepository.countRespondidasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setQuestoesAcertadas(toCountMap(respostaRepository.countAcertadasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            Map<Long, Double> avgTempoMap = toDoubleMap(respostaRepository.avgTempoByDisciplinaIds(discIds));
            dto.setMediaTempoResposta(avgTempoMap.containsKey(id) ? avgTempoMap.get(id).intValue() : null);
            dto.setDificuldadeRespostas(parseDificuldadeStats(respostaRepository.getDificuldadeStatsByDisciplinaIds(discIds)).get(id));
        }

        // Enrich nested temas with tier-aware call
        dto.setTemas(temaService.findByDisciplinaId(id, metrics));

        return dto;
    }

    @CacheEvict(value = "disciplina-stats", allEntries = true)
    public void create(DisciplinaCreateRequest request) {
        log.info("Criando nova disciplina: {}", request.getNome());

        Optional<Disciplina> existing = disciplinaRepository.findByNomeIgnoreCase(request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe uma disciplina com o nome '" + request.getNome() + "'");
        }

        Disciplina disciplina = disciplinaMapper.toEntity(request);
        disciplinaRepository.save(disciplina);
    }

    @CacheEvict(value = "disciplina-stats", allEntries = true)
    public void update(Long id, DisciplinaUpdateRequest request) {
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
        disciplinaRepository.save(disciplina);
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

    /**
     * Returns the full disciplina tree (disciplina → temas → subtemas) in one response.
     * Default metrics level is {@code FULL} since the sole consumer is the detail page.
     */
    @Transactional(readOnly = true)
    public DisciplinaDetailDto getDisciplinaCompleto(Long id, MetricsLevel metrics) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));

        DisciplinaDetailDto dto = disciplinaMapper.toDetailDto(disciplina);

        // Enrich disciplina root metrics (only if full)
        boolean isFull = metrics == MetricsLevel.FULL;
        if (isFull) {
            List<Long> discIds = List.of(id);
            dto.setTotalEstudos(toCountMap(estudoSubtemaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setUltimoEstudo(toDateMap(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(discIds)).get(id));
            dto.setUltimaQuestao(toDateMap(respostaRepository.findLatestResponseDatesByDisciplinaIds(discIds)).get(id));
            dto.setTotalTemas(toCountMap(temaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setTotalSubtemas(toCountMap(subtemaRepository.countByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setSubtemasEstudados(toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setTemasEstudados(toCountMap(temaRepository.countTemasEstudadosByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setTotalQuestoes(toCountMap(questaoRepository.countQuestoesByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setQuestoesRespondidas(toCountMap(respostaRepository.countRespondidasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            dto.setQuestoesAcertadas(toCountMap(respostaRepository.countAcertadasByDisciplinaIds(discIds)).getOrDefault(id, 0L));
            Map<Long, Double> avgTempoMap = toDoubleMap(respostaRepository.avgTempoByDisciplinaIds(discIds));
            dto.setMediaTempoResposta(avgTempoMap.containsKey(id) ? avgTempoMap.get(id).intValue() : null);
            dto.setDificuldadeRespostas(parseDificuldadeStats(respostaRepository.getDificuldadeStatsByDisciplinaIds(discIds)).get(id));
        }

        // Enrich nested temas with subtemas
        List<Tema> temas = temaRepository.findByDisciplinaId(id);
        if (!temas.isEmpty()) {
            List<Long> temaIds = temas.stream().map(Tema::getId).toList();
            dto.setTemas(temaIds.stream().map(temaId -> {
                Tema tema = temas.stream().filter(t -> t.getId().equals(temaId)).findFirst().orElse(null);
                if (tema == null) return null;

                TemaSummaryDto temaDto = temaMapper.toSummaryDto(tema);

                if (isFull) {
                    List<Long> singleTemaId = List.of(temaId);
                    temaDto.setTotalEstudos(toCountMap(estudoSubtemaRepository.countByTemaIds(singleTemaId)).getOrDefault(temaId, 0L));
                    temaDto.setUltimoEstudo(toDateMap(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(singleTemaId)).get(temaId));
                    temaDto.setUltimaQuestao(toDateMap(respostaRepository.findLatestResponseDatesByTemaIds(singleTemaId)).get(temaId));
                    temaDto.setTotalSubtemas(toCountMap(subtemaRepository.countByTemaIds(singleTemaId)).getOrDefault(temaId, 0L));
                    temaDto.setSubtemasEstudados(toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(singleTemaId)).getOrDefault(temaId, 0L));
                    temaDto.setTotalQuestoes(toCountMap(questaoRepository.countQuestoesByTemaIds(singleTemaId)).getOrDefault(temaId, 0L));
                    temaDto.setQuestoesRespondidas(toCountMap(respostaRepository.countRespondidasByTemaIds(singleTemaId)).getOrDefault(temaId, 0L));
                    temaDto.setQuestoesAcertadas(toCountMap(respostaRepository.countAcertadasByTemaIds(singleTemaId)).getOrDefault(temaId, 0L));
                    Map<Long, Double> temaAvgTempo = toDoubleMap(respostaRepository.avgTempoByTemaIds(singleTemaId));
                    temaDto.setMediaTempoResposta(temaAvgTempo.containsKey(temaId) ? temaAvgTempo.get(temaId).intValue() : null);
                    temaDto.setDificuldadeRespostas(parseDificuldadeStats(respostaRepository.getDificuldadeStatsByTemaIds(singleTemaId)).get(temaId));
                }

                // Enrich nested subtemas
                List<Subtema> subtemas = subtemaRepository.findByTemaId(temaId);
                if (!subtemas.isEmpty()) {
                    List<Long> subtemaIds = subtemas.stream().map(Subtema::getId).toList();
                    List<SubtemaSummaryDto> subtemaDtos = subtemaIds.stream().map(subId -> {
                        Subtema sub = subtemas.stream().filter(s -> s.getId().equals(subId)).findFirst().orElse(null);
                        if (sub == null) return null;
                        SubtemaSummaryDto subDto = subtemaMapper.toSummaryDto(sub);

                        if (isFull) {
                            List<Long> singleSubId = List.of(subId);
                            subDto.setTotalEstudos(toCountMap(estudoSubtemaRepository.countBySubtemaIds(singleSubId)).getOrDefault(subId, 0L));
                            subDto.setUltimoEstudo(toDateMap(estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(singleSubId)).get(subId));
                            subDto.setUltimaQuestao(toDateMap(respostaRepository.findLatestResponseDatesBySubtemaIds(singleSubId)).get(subId));
                            subDto.setTotalQuestoes(toCountMap(questaoRepository.countQuestoesBySubtemaIds(singleSubId)).getOrDefault(subId, 0L));
                            subDto.setQuestoesRespondidas(toCountMap(respostaRepository.countRespondidasBySubtemaIds(singleSubId)).getOrDefault(subId, 0L));
                            subDto.setQuestoesAcertadas(toCountMap(respostaRepository.countAcertadasBySubtemaIds(singleSubId)).getOrDefault(subId, 0L));
                            Map<Long, Double> subAvgTempo = toDoubleMap(respostaRepository.avgTempoBySubtemaIds(singleSubId));
                            subDto.setMediaTempoResposta(subAvgTempo.containsKey(subId) ? subAvgTempo.get(subId).intValue() : null);
                            subDto.setDificuldadeRespostas(parseDificuldadeStats(respostaRepository.getDificuldadeStatsBySubtemaIds(singleSubId)).get(subId));
                        }

                        return subDto;
                    }).filter(s -> s != null).collect(Collectors.toList());
                    temaDto.setSubtemas(subtemaDtos);
                } else {
                    temaDto.setSubtemas(Collections.emptyList());
                }

                return temaDto;
            }).filter(t -> t != null).collect(Collectors.toList()));
        } else {
            dto.setTemas(Collections.emptyList());
        }

        return dto;
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