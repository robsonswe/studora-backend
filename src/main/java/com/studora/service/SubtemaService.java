package com.studora.service;

import com.studora.dto.MetricsLevel;
import com.studora.dto.subtema.SubtemaDetailDto;
import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.dto.request.SubtemaCreateRequest;
import com.studora.dto.request.SubtemaUpdateRequest;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.SubtemaMapper;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.repository.EstudoSubtemaRepository;
import com.studora.repository.specification.SubtemaSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final EstudoSubtemaRepository estudoSubtemaRepository;
    private final SubtemaMapper subtemaMapper;
    private final StatsAssembler statsAssembler;
    private final Executor dbStatsExecutor;

    public SubtemaService(SubtemaRepository subtemaRepository, TemaRepository temaRepository,
                          QuestaoRepository questaoRepository,
                          EstudoSubtemaRepository estudoSubtemaRepository,
                          SubtemaMapper subtemaMapper,
                          StatsAssembler statsAssembler,
                          @Qualifier("dbStatsExecutor") Executor dbStatsExecutor) {
        this.subtemaRepository = subtemaRepository;
        this.temaRepository = temaRepository;
        this.questaoRepository = questaoRepository;
        this.estudoSubtemaRepository = estudoSubtemaRepository;
        this.subtemaMapper = subtemaMapper;
        this.statsAssembler = statsAssembler;
        this.dbStatsExecutor = dbStatsExecutor;
    }

    @Cacheable(value = "subtema-stats", key = "T(java.util.Objects).hash('all', #nome, #temaIds, #disciplinaIds, #pageable.pageNumber, #pageable.pageSize, #pageable.sort, #metrics)")
    @Transactional(readOnly = true)
    public Page<SubtemaSummaryDto> findAll(String nome, List<Long> temaIds, List<Long> disciplinaIds, Pageable pageable, MetricsLevel metrics) {
        Specification<Subtema> spec = SubtemaSpecification.withFilters(nome, temaIds, disciplinaIds);
        Page<Subtema> page = subtemaRepository.findAll(spec, pageable);

        if (page.isEmpty() || metrics == null) {
            return page.map(subtemaMapper::toSummaryDto);
        }

        List<Long> ids = page.getContent().stream().map(Subtema::getId).collect(Collectors.toList());
        
        var countsFut = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countBySubtemaIds(ids)), dbStatsExecutor);
        var datesFut  = CompletableFuture.supplyAsync(() -> toDateMap(estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(ids)), dbStatsExecutor);

        CompletableFuture.allOf(countsFut, datesFut).join();

        Map<Long, Long> counts = countsFut.join();
        Map<Long, LocalDateTime> dates = datesFut.join();

        return page.map(subtema -> {
            SubtemaSummaryDto dto = subtemaMapper.toSummaryDto(subtema);
            dto.setQuestaoStats(statsAssembler.buildStats(subtema.getId(), "SUBTEMA", metrics));
            dto.setTotalEstudos(counts.getOrDefault(subtema.getId(), 0L));
            dto.setUltimoEstudo(dates.get(subtema.getId()));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public SubtemaDetailDto getSubtemaDetailById(Long id, MetricsLevel metrics) {
        Subtema subtema = subtemaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", id));

        SubtemaDetailDto dto = subtemaMapper.toDetailDto(subtema);

        if (metrics != null) {
            dto.setQuestaoStats(statsAssembler.buildStats(id, "SUBTEMA", metrics));
            dto.setTotalEstudos(estudoSubtemaRepository.countBySubtemaId(id));
            dto.setUltimoEstudo(estudoSubtemaRepository.findFirstBySubtemaIdOrderByCreatedAtDesc(id)
                    .map(com.studora.entity.EstudoSubtema::getCreatedAt).orElse(null));
        }

        return dto;
    }

    @CacheEvict(value = "subtema-stats", allEntries = true)
    public Long create(SubtemaCreateRequest request) {
        log.info("Criando novo subtema: {} no tema ID: {}", request.getNome(), request.getTemaId());

        Optional<Subtema> existing = subtemaRepository.findByTemaIdAndNomeIgnoreCase(request.getTemaId(), request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe um subtema com o nome '" + request.getNome() + "' no tema com ID: " + request.getTemaId());
        }

        Tema tema = temaRepository.findById(request.getTemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", request.getTemaId()));

        Subtema subtema = subtemaMapper.toEntity(request);
        subtema.setTema(tema);

        return subtemaRepository.save(subtema).getId();
    }

    @CacheEvict(value = "subtema-stats", allEntries = true)
    public void update(Long id, SubtemaUpdateRequest request) {
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
        subtemaRepository.save(subtema);
    }

    @Cacheable(value = "subtema-stats", key = "T(java.lang.String).format('tema-%d-%s', #temaId, #metrics)")
    @Transactional(readOnly = true)
    public List<SubtemaSummaryDto> findByTemaId(Long temaId, MetricsLevel metrics) {
        if (!temaRepository.existsById(temaId)) {
            throw new ResourceNotFoundException("Tema", "ID", temaId);
        }
        List<Subtema> subtemas = subtemaRepository.findByTemaId(temaId);
        
        if (subtemas.isEmpty() || metrics == null) {
            return subtemas.stream().map(subtemaMapper::toSummaryDto).collect(Collectors.toList());
        }

        List<Long> ids = subtemas.stream().map(Subtema::getId).collect(Collectors.toList());
        
        var countsFut = CompletableFuture.supplyAsync(() -> toCountMap(estudoSubtemaRepository.countBySubtemaIds(ids)), dbStatsExecutor);
        var datesFut  = CompletableFuture.supplyAsync(() -> toDateMap(estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(ids)), dbStatsExecutor);

        CompletableFuture.allOf(countsFut, datesFut).join();

        Map<Long, Long> counts = countsFut.join();
        Map<Long, LocalDateTime> dates = datesFut.join();

        return subtemas.stream()
                .map(subtema -> {
                    SubtemaSummaryDto dto = subtemaMapper.toSummaryDto(subtema);
                    dto.setQuestaoStats(statsAssembler.buildStats(subtema.getId(), "SUBTEMA", metrics));
                    dto.setTotalEstudos(counts.getOrDefault(subtema.getId(), 0L));
                    dto.setUltimoEstudo(dates.get(subtema.getId()));
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

    private Map<Long, LocalDateTime> toDateMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> parseDate(row[1])));
    }

    private LocalDateTime parseDate(Object val) {
        if (val instanceof LocalDateTime) return (LocalDateTime) val;
        if (val instanceof String) return LocalDateTime.parse((String) val, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (val instanceof java.sql.Timestamp) return ((java.sql.Timestamp) val).toLocalDateTime();
        return null;
    }
}
