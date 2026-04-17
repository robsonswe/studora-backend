package com.studora.service;

import com.studora.dto.MetricsLevel;
import com.studora.dto.tema.TemaDetailDto;
import com.studora.dto.tema.TemaSummaryDto;
import com.studora.dto.request.TemaCreateRequest;
import com.studora.dto.request.TemaUpdateRequest;
import com.studora.entity.Disciplina;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.TemaMapper;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.repository.EstudoSubtemaRepository;
import com.studora.repository.specification.TemaSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TemaService {

    private final TemaRepository temaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final SubtemaRepository subtemaRepository;
    private final EstudoSubtemaRepository estudoSubtemaRepository;
    private final TemaMapper temaMapper;
    private final SubtemaService subtemaService;
    private final StatsAssembler statsAssembler;

    @Cacheable(value = "tema-stats", key = "T(java.util.Objects).hash('all', #nome, #disciplinaIds, #pageable.pageNumber, #pageable.pageSize, #pageable.sort, #metrics)")
    @Transactional(readOnly = true)
    public Page<TemaSummaryDto> findAll(String nome, List<Long> disciplinaIds, Pageable pageable, MetricsLevel metrics) {
        Specification<Tema> spec = TemaSpecification.withFilters(nome, disciplinaIds);
        Page<Tema> page = temaRepository.findAll(spec, pageable);

        if (page.isEmpty() || metrics == null) {
            return page.map(temaMapper::toSummaryDto);
        }

        List<Long> ids = page.getContent().stream().map(Tema::getId).collect(Collectors.toList());
        Map<Long, LocalDateTime> dates = toDateMap(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(ids));
        Map<Long, Long> totalSub = toMap(subtemaRepository.countByTemaIds(ids));
        Map<Long, Long> studSub = toMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(ids));

        return page.map(tema -> {
            TemaSummaryDto dto = temaMapper.toSummaryDto(tema);
            dto.setQuestaoStats(statsAssembler.buildStats(tema.getId(), "TEMA", metrics));
            dto.setUltimoEstudo(dates.get(tema.getId()));
            dto.setTotalSubtemas(totalSub.getOrDefault(tema.getId(), 0L));
            dto.setSubtemasEstudados(studSub.getOrDefault(tema.getId(), 0L));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public TemaDetailDto getTemaDetailById(Long id, MetricsLevel metrics) {
        Tema tema = temaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", id));

        TemaDetailDto dto = temaMapper.toDetailDto(tema);
        if (metrics != null) {
            dto.setQuestaoStats(statsAssembler.buildStats(id, "TEMA", metrics));
            dto.setUltimoEstudo(estudoSubtemaRepository.findLatestStudyDateByTemaId(id));
            dto.setTotalSubtemas((long) subtemaRepository.findByTemaId(id).size());
            dto.setSubtemasEstudados(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaId(id));
        }

        dto.setSubtemas(subtemaService.findByTemaId(id, metrics));

        return dto;
    }

    @CacheEvict(value = "tema-stats", allEntries = true)
    public Long create(TemaCreateRequest request) {
        log.info("Criando novo tema: {} na disciplina ID: {}", request.getNome(), request.getDisciplinaId());

        Optional<Tema> existing = temaRepository.findByDisciplinaIdAndNomeIgnoreCase(request.getDisciplinaId(), request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe um tema com o nome '" + request.getNome() + "' na disciplina com ID: " + request.getDisciplinaId());
        }

        Disciplina disciplina = disciplinaRepository.findById(request.getDisciplinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", request.getDisciplinaId()));

        Tema tema = temaMapper.toEntity(request);
        tema.setDisciplina(disciplina);

        return temaRepository.save(tema).getId();
    }

    @CacheEvict(value = "tema-stats", allEntries = true)
    public void update(Long id, TemaUpdateRequest request) {
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
        temaRepository.save(tema);
    }

    @Cacheable(value = "tema-stats", key = "T(java.lang.String).format('disc-%d-%s', #disciplinaId, #metrics)")
    @Transactional(readOnly = true)
    public List<TemaSummaryDto> findByDisciplinaId(Long disciplinaId, MetricsLevel metrics) {
        if (!disciplinaRepository.existsById(disciplinaId)) {
            throw new ResourceNotFoundException("Disciplina", "ID", disciplinaId);
        }
        List<Tema> temas = temaRepository.findByDisciplinaId(disciplinaId);
        
        if (temas.isEmpty() || metrics == null) {
            return temas.stream().map(temaMapper::toSummaryDto).collect(Collectors.toList());
        }

        List<Long> ids = temas.stream().map(Tema::getId).collect(Collectors.toList());
        Map<Long, LocalDateTime> dates = toDateMap(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(ids));
        Map<Long, Long> totalSub = toMap(subtemaRepository.countByTemaIds(ids));
        Map<Long, Long> studSub = toMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(ids));

        return temas.stream().map(tema -> {
            TemaSummaryDto dto = temaMapper.toSummaryDto(tema);
            dto.setQuestaoStats(statsAssembler.buildStats(tema.getId(), "TEMA", metrics));
            dto.setUltimoEstudo(dates.get(tema.getId()));
            dto.setTotalSubtemas(totalSub.getOrDefault(tema.getId(), 0L));
            dto.setSubtemasEstudados(studSub.getOrDefault(tema.getId(), 0L));
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

    private Map<Long, Long> toMap(List<Object[]> rows) {
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
