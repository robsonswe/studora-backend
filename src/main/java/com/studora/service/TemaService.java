package com.studora.service;

import com.studora.dto.DificuldadeStatDto;
import com.studora.dto.tema.TemaDetailDto;
import com.studora.dto.tema.TemaSummaryDto;
import com.studora.dto.request.TemaCreateRequest;
import com.studora.dto.request.TemaUpdateRequest;
import com.studora.entity.Dificuldade;
import com.studora.entity.Disciplina;
import com.studora.entity.Resposta;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
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
public class TemaService {

    private final TemaRepository temaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final SubtemaRepository subtemaRepository;
    private final EstudoSubtemaRepository estudoSubtemaRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final TemaMapper temaMapper;
    private final SubtemaService subtemaService;
    private final Executor dbStatsExecutor;

    public TemaService(TemaRepository temaRepository, DisciplinaRepository disciplinaRepository,
                       SubtemaRepository subtemaRepository, EstudoSubtemaRepository estudoSubtemaRepository,
                       QuestaoRepository questaoRepository, RespostaRepository respostaRepository,
                       TemaMapper temaMapper, SubtemaService subtemaService,
                       @Qualifier("dbStatsExecutor") Executor dbStatsExecutor) {
        this.temaRepository = temaRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.subtemaRepository = subtemaRepository;
        this.estudoSubtemaRepository = estudoSubtemaRepository;
        this.questaoRepository = questaoRepository;
        this.respostaRepository = respostaRepository;
        this.temaMapper = temaMapper;
        this.subtemaService = subtemaService;
        this.dbStatsExecutor = dbStatsExecutor;
    }

    @Cacheable(value = "tema-stats", key = "T(java.util.Objects).hash('all', #nome, #pageable.pageNumber, #pageable.pageSize, #pageable.sort)")
    @Transactional(readOnly = true)
    public Page<TemaSummaryDto> findAll(String nome, Pageable pageable) {
        Page<Tema> page;
        if (nome != null && !nome.isBlank()) {
            page = temaRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            page = temaRepository.findAll(pageable);
        }

        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> ids = page.getContent().stream().map(Tema::getId).toList();

        var totalEstudosFut    = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countByTemaIds(ids)), dbStatsExecutor);
        var ultimoEstudoFut    = CompletableFuture.supplyAsync(() -> toDateMap(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(ids)), dbStatsExecutor);
        var ultimaQuestaoFut   = CompletableFuture.supplyAsync(() -> toDateMap(respostaRepository.findLatestResponseDatesByTemaIds(ids)), dbStatsExecutor);
        var totalSubtemasFut   = CompletableFuture.supplyAsync(() -> toCountMap(subtemaRepository.countByTemaIds(ids)), dbStatsExecutor);
        var subtemasEstudadosFut = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(ids)), dbStatsExecutor);
        var totalQuestoesFut   = CompletableFuture.supplyAsync(() -> toCountMap(questaoRepository.countQuestoesByTemaIds(ids)), dbStatsExecutor);
        var respondidasFut     = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countRespondidasByTemaIds(ids)), dbStatsExecutor);
        var acertadasFut       = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countAcertadasByTemaIds(ids)), dbStatsExecutor);
        var avgTempoFut        = CompletableFuture.supplyAsync(() -> toDoubleMap(respostaRepository.avgTempoByTemaIds(ids)), dbStatsExecutor);
        var dificuldadeFut     = CompletableFuture.supplyAsync(() -> parseDificuldadeStats(respostaRepository.getDificuldadeStatsByTemaIds(ids)), dbStatsExecutor);

        CompletableFuture.allOf(totalEstudosFut, ultimoEstudoFut, ultimaQuestaoFut, totalSubtemasFut, subtemasEstudadosFut, totalQuestoesFut, respondidasFut, acertadasFut, avgTempoFut, dificuldadeFut).join();

        Map<Long, Long> totalEstudosMap = totalEstudosFut.join();
        Map<Long, LocalDateTime> ultimoEstudoMap = ultimoEstudoFut.join();
        Map<Long, LocalDateTime> ultimaQuestaoMap = ultimaQuestaoFut.join();
        Map<Long, Long> totalSubtemasMap = totalSubtemasFut.join();
        Map<Long, Long> subtemasEstudadosMap = subtemasEstudadosFut.join();
        Map<Long, Long> totalQuestoesMap = totalQuestoesFut.join();
        Map<Long, Long> respondidasMap = respondidasFut.join();
        Map<Long, Long> acertadasMap = acertadasFut.join();
        Map<Long, Double> avgTempoMap = avgTempoFut.join();
        Map<Long, Map<String, DificuldadeStatDto>> dificuldadeMap = dificuldadeFut.join();

        return page.map(tema -> {
            Long temaId = tema.getId();
            TemaSummaryDto dto = temaMapper.toSummaryDto(tema);
            dto.setTotalEstudos(totalEstudosMap.getOrDefault(temaId, 0L));
            dto.setUltimoEstudo(ultimoEstudoMap.get(temaId));
            dto.setUltimaQuestao(ultimaQuestaoMap.get(temaId));
            dto.setTotalSubtemas(totalSubtemasMap.getOrDefault(temaId, 0L));
            dto.setSubtemasEstudados(subtemasEstudadosMap.getOrDefault(temaId, 0L));
            dto.setTotalQuestoes(totalQuestoesMap.getOrDefault(temaId, 0L));
            dto.setQuestoesRespondidas(respondidasMap.getOrDefault(temaId, 0L));
            dto.setQuestoesAcertadas(acertadasMap.getOrDefault(temaId, 0L));
            dto.setMediaTempoResposta(avgTempoMap.containsKey(temaId) ? avgTempoMap.get(temaId).intValue() : null);
            dto.setDificuldadeRespostas(dificuldadeMap.getOrDefault(temaId, Collections.emptyMap()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public TemaDetailDto getTemaDetailById(Long id) {
        Tema tema = temaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", id));

        TemaDetailDto dto = temaMapper.toDetailDto(tema);

        // Enrich with study stats
        List<Long> temaIds = List.of(id);
        dto.setTotalEstudos(toCountMap(estudoSubtemaRepository.countByTemaIds(temaIds)).getOrDefault(id, 0L));
        dto.setUltimoEstudo(toDateMap(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(temaIds)).get(id));
        dto.setUltimaQuestao(toDateMap(respostaRepository.findLatestResponseDatesByTemaIds(temaIds)).get(id));
        dto.setTotalSubtemas(toCountMap(subtemaRepository.countByTemaIds(temaIds)).getOrDefault(id, 0L));
        dto.setSubtemasEstudados(toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(temaIds)).getOrDefault(id, 0L));

        // Enrich questao stats for tema
        dto.setTotalQuestoes(toCountMap(questaoRepository.countQuestoesByTemaIds(temaIds)).getOrDefault(id, 0L));
        dto.setQuestoesRespondidas(toCountMap(respostaRepository.countRespondidasByTemaIds(temaIds)).getOrDefault(id, 0L));
        dto.setQuestoesAcertadas(toCountMap(respostaRepository.countAcertadasByTemaIds(temaIds)).getOrDefault(id, 0L));
        Map<Long, Double> avgTempoMap = toDoubleMap(respostaRepository.avgTempoByTemaIds(temaIds));
        dto.setMediaTempoResposta(avgTempoMap.containsKey(id) ? avgTempoMap.get(id).intValue() : null);
        dto.setDificuldadeRespostas(parseDificuldadeStats(respostaRepository.getDificuldadeStatsByTemaIds(temaIds)).getOrDefault(id, initEmptyDificuldadeMap()));

        // Enrich nested disciplina
        if (dto.getDisciplina() != null) {
            Long discId = dto.getDisciplina().getId();
            List<Long> singleDiscId = List.of(discId);
            dto.getDisciplina().setTotalEstudos(toCountMap(estudoSubtemaRepository.countByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            dto.getDisciplina().setUltimoEstudo(toDateMap(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(singleDiscId)).get(discId));
            dto.getDisciplina().setUltimaQuestao(toDateMap(respostaRepository.findLatestResponseDatesByDisciplinaIds(singleDiscId)).get(discId));
            dto.getDisciplina().setTotalTemas(toCountMap(temaRepository.countByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            dto.getDisciplina().setTotalSubtemas(toCountMap(subtemaRepository.countByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            dto.getDisciplina().setSubtemasEstudados(toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            dto.getDisciplina().setTemasEstudados(toCountMap(temaRepository.countTemasEstudadosByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));

            // Enrich disciplina questao stats
            dto.getDisciplina().setTotalQuestoes(toCountMap(questaoRepository.countQuestoesByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            dto.getDisciplina().setQuestoesRespondidas(toCountMap(respostaRepository.countRespondidasByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            dto.getDisciplina().setQuestoesAcertadas(toCountMap(respostaRepository.countAcertadasByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            Map<Long, Double> discAvgTempo = toDoubleMap(respostaRepository.avgTempoByDisciplinaIds(singleDiscId));
            dto.getDisciplina().setMediaTempoResposta(discAvgTempo.containsKey(discId) ? discAvgTempo.get(discId).intValue() : null);
            dto.getDisciplina().setDificuldadeRespostas(parseDificuldadeStats(respostaRepository.getDificuldadeStatsByDisciplinaIds(singleDiscId)).getOrDefault(discId, initEmptyDificuldadeMap()));
        }

        // Enrich nested subtemas
        dto.setSubtemas(subtemaService.findByTemaId(id));

        return dto;
    }

    @CacheEvict(value = "tema-stats", allEntries = true)
    public TemaDetailDto create(TemaCreateRequest request) {
        log.info("Criando novo tema: {} na disciplina ID: {}", request.getNome(), request.getDisciplinaId());

        Optional<Tema> existing = temaRepository.findByDisciplinaIdAndNomeIgnoreCase(request.getDisciplinaId(), request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe um tema com o nome '" + request.getNome() + "' na disciplina com ID: " + request.getDisciplinaId());
        }

        Disciplina disciplina = disciplinaRepository.findById(request.getDisciplinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", request.getDisciplinaId()));

        Tema tema = temaMapper.toEntity(request);
        tema.setDisciplina(disciplina);

        return temaMapper.toDetailDto(temaRepository.save(tema));
    }

    @CacheEvict(value = "tema-stats", allEntries = true)
    public TemaDetailDto update(Long id, TemaUpdateRequest request) {
        log.info("Atualizando tema ID: {}", id);

        Tema tema = temaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", id));

        if (request.getNome() != null || request.getDisciplinaId() != null) {
            Long discId = request.getDisciplinaId() != null ? request.getDisciplinaId() : tema.getDisciplina().getId();
            String nome = request.getNome() != null ? request.getNome() : tema.getNome();

            Optional<Tema> existing = temaRepository.findByDisciplinaIdAndNomeIgnoreCase(discId, nome);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new ConflictException("Já existe um tema com o nome '" + nome + "' na disciplina com ID: " + discId);
            }
        }

        if (request.getDisciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findById(request.getDisciplinaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", request.getDisciplinaId()));
            tema.setDisciplina(disciplina);
        }

        temaMapper.updateEntityFromDto(request, tema);
        return temaMapper.toDetailDto(temaRepository.save(tema));
    }

    @Cacheable(value = "tema-stats", key = "T(java.lang.String).format('disc-%d', #disciplinaId)")
    @Transactional(readOnly = true)
    public List<TemaSummaryDto> findByDisciplinaId(Long disciplinaId) {
        if (!disciplinaRepository.existsById(disciplinaId)) {
            throw new ResourceNotFoundException("Disciplina", "ID", disciplinaId);
        }
        List<Tema> temas = temaRepository.findByDisciplinaId(disciplinaId);
        if (temas.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = temas.stream().map(Tema::getId).toList();

        var totalEstudosFut    = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countByTemaIds(ids)), dbStatsExecutor);
        var ultimoEstudoFut    = CompletableFuture.supplyAsync(() -> toDateMap(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(ids)), dbStatsExecutor);
        var ultimaQuestaoFut   = CompletableFuture.supplyAsync(() -> toDateMap(respostaRepository.findLatestResponseDatesByTemaIds(ids)), dbStatsExecutor);
        var totalSubtemasFut   = CompletableFuture.supplyAsync(() -> toCountMap(subtemaRepository.countByTemaIds(ids)), dbStatsExecutor);
        var subtemasEstudadosFut = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(ids)), dbStatsExecutor);
        var totalQuestoesFut   = CompletableFuture.supplyAsync(() -> toCountMap(questaoRepository.countQuestoesByTemaIds(ids)), dbStatsExecutor);
        var respondidasFut     = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countRespondidasByTemaIds(ids)), dbStatsExecutor);
        var acertadasFut       = CompletableFuture.supplyAsync(() -> toCountMap(respostaRepository.countAcertadasByTemaIds(ids)), dbStatsExecutor);
        var avgTempoFut        = CompletableFuture.supplyAsync(() -> toDoubleMap(respostaRepository.avgTempoByTemaIds(ids)), dbStatsExecutor);
        var dificuldadeFut     = CompletableFuture.supplyAsync(() -> parseDificuldadeStats(respostaRepository.getDificuldadeStatsByTemaIds(ids)), dbStatsExecutor);

        CompletableFuture.allOf(totalEstudosFut, ultimoEstudoFut, ultimaQuestaoFut, totalSubtemasFut, subtemasEstudadosFut, totalQuestoesFut, respondidasFut, acertadasFut, avgTempoFut, dificuldadeFut).join();

        Map<Long, Long> totalEstudosMap = totalEstudosFut.join();
        Map<Long, LocalDateTime> ultimoEstudoMap = ultimoEstudoFut.join();
        Map<Long, LocalDateTime> ultimaQuestaoMap = ultimaQuestaoFut.join();
        Map<Long, Long> totalSubtemasMap = totalSubtemasFut.join();
        Map<Long, Long> subtemasEstudadosMap = subtemasEstudadosFut.join();
        Map<Long, Long> totalQuestoesMap = totalQuestoesFut.join();
        Map<Long, Long> respondidasMap = respondidasFut.join();
        Map<Long, Long> acertadasMap = acertadasFut.join();
        Map<Long, Double> avgTempoMap = avgTempoFut.join();
        Map<Long, Map<String, DificuldadeStatDto>> dificuldadeMap = dificuldadeFut.join();

        return temas.stream().map(tema -> {
            Long temaId = tema.getId();
            TemaSummaryDto dto = temaMapper.toSummaryDto(tema);
            dto.setTotalEstudos(totalEstudosMap.getOrDefault(temaId, 0L));
            dto.setUltimoEstudo(ultimoEstudoMap.get(temaId));
            dto.setUltimaQuestao(ultimaQuestaoMap.get(temaId));
            dto.setTotalSubtemas(totalSubtemasMap.getOrDefault(temaId, 0L));
            dto.setSubtemasEstudados(subtemasEstudadosMap.getOrDefault(temaId, 0L));
            dto.setTotalQuestoes(totalQuestoesMap.getOrDefault(temaId, 0L));
            dto.setQuestoesRespondidas(respondidasMap.getOrDefault(temaId, 0L));
            dto.setQuestoesAcertadas(acertadasMap.getOrDefault(temaId, 0L));
            dto.setMediaTempoResposta(avgTempoMap.containsKey(temaId) ? avgTempoMap.get(temaId).intValue() : null);
            dto.setDificuldadeRespostas(dificuldadeMap.getOrDefault(temaId, Collections.emptyMap()));
            return dto;
        }).collect(Collectors.toList());
    }

    @CacheEvict(value = "tema-stats", allEntries = true)
    public void delete(Long id) {
        log.info("Excluindo tema ID: {}", id);
        if (!temaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tema", "ID", id);
        }

        if (subtemaRepository.existsByTemaId(id)) {
            throw new ValidationException("Não é possível excluir um tema que possui subtemas associados");
        }

        temaRepository.deleteById(id);
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
