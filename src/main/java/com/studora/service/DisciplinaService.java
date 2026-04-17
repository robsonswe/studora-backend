package com.studora.service;

import com.studora.dto.MetricsLevel;
import com.studora.dto.disciplina.DisciplinaDetailDto;
import com.studora.dto.disciplina.DisciplinaSummaryDto;
import com.studora.dto.request.DisciplinaCreateRequest;
import com.studora.dto.request.DisciplinaUpdateRequest;
import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.dto.tema.TemaSummaryDto;
import com.studora.entity.Disciplina;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.DisciplinaMapper;
import com.studora.mapper.SubtemaMapper;
import com.studora.mapper.TemaMapper;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.repository.EstudoSubtemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final TemaRepository temaRepository;
    private final SubtemaRepository subtemaRepository;
    private final EstudoSubtemaRepository estudoSubtemaRepository;
    private final DisciplinaMapper disciplinaMapper;
    private final TemaMapper temaMapper;
    private final SubtemaMapper subtemaMapper;
    private final StatsAssembler statsAssembler;

    @Cacheable(value = "disciplina-stats", key = "T(java.util.Objects).hash(#nome, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString(), #metrics)")
    @Transactional(readOnly = true)
    public Page<DisciplinaSummaryDto> findAll(String nome, Pageable pageable, MetricsLevel metrics) {
        Page<Disciplina> page;
        if (nome != null && !nome.isBlank()) {
            page = disciplinaRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            page = disciplinaRepository.findAll(pageable);
        }

        if (page.isEmpty() || metrics == null) {
            return page.map(disciplinaMapper::toSummaryDto);
        }

        List<Long> ids = page.getContent().stream().map(Disciplina::getId).collect(Collectors.toList());
        Map<Long, LocalDateTime> dates = toDateMap(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(ids));
        Map<Long, Long> totalTemas = toMap(temaRepository.countByDisciplinaIds(ids));
        Map<Long, Long> totalSub = toMap(subtemaRepository.countByDisciplinaIds(ids));
        Map<Long, Long> studSub = toMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(ids));
        Map<Long, Long> studTemas = toMap(temaRepository.countTemasEstudadosByDisciplinaIds(ids));

        return page.map(disciplina -> {
            DisciplinaSummaryDto dto = disciplinaMapper.toSummaryDto(disciplina);
            dto.setQuestaoStats(statsAssembler.buildStats(disciplina.getId(), "DISCIPLINA", metrics));
            dto.setUltimoEstudo(dates.get(disciplina.getId()));
            dto.setTotalTemas(totalTemas.getOrDefault(disciplina.getId(), 0L));
            dto.setTotalSubtemas(totalSub.getOrDefault(disciplina.getId(), 0L));
            dto.setSubtemasEstudados(studSub.getOrDefault(disciplina.getId(), 0L));
            dto.setTemasEstudados(studTemas.getOrDefault(disciplina.getId(), 0L));
            return dto;
        });
    }

    @Transactional(readOnly = true)
    public DisciplinaDetailDto getDisciplinaDetailById(Long id, MetricsLevel metrics) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));

        DisciplinaDetailDto dto = disciplinaMapper.toDetailDto(disciplina);
        if (metrics != null) {
            dto.setQuestaoStats(statsAssembler.buildStats(id, "DISCIPLINA", metrics));
            dto.setUltimoEstudo(estudoSubtemaRepository.findLatestStudyDateByDisciplinaId(id));
            dto.setTotalTemas((long) temaRepository.findByDisciplinaId(id).size());
            dto.setTotalSubtemas(subtemaRepository.countByDisciplinaIds(List.of(id)).stream()
                    .filter(r -> ((Number)r[0]).longValue() == id)
                    .map(r -> ((Number)r[1]).longValue())
                    .findFirst().orElse(0L));
            dto.setSubtemasEstudados(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaId(id));
            dto.setTemasEstudados(temaRepository.countTemasEstudadosByDisciplinaIds(List.of(id)).stream()
                    .filter(r -> ((Number)r[0]).longValue() == id)
                    .map(r -> ((Number)r[1]).longValue())
                    .findFirst().orElse(0L));
        }

        return dto;
    }

    @CacheEvict(value = "disciplina-stats", allEntries = true)
    public Long create(DisciplinaCreateRequest request) {
        log.info("Criando nova disciplina: {}", request.getNome());

        Optional<Disciplina> existing = disciplinaRepository.findByNomeIgnoreCase(request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe uma disciplina com o nome '" + request.getNome() + "'");
        }

        Disciplina disciplina = disciplinaMapper.toEntity(request);
        return disciplinaRepository.save(disciplina).getId();
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

    @Transactional(readOnly = true)
    public DisciplinaDetailDto getDisciplinaCompleto(Long id, MetricsLevel metrics) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));

        DisciplinaDetailDto dto = disciplinaMapper.toDetailDto(disciplina);
        
        if (metrics != null) {
            dto.setQuestaoStats(statsAssembler.buildStats(id, "DISCIPLINA", metrics));
            dto.setUltimoEstudo(estudoSubtemaRepository.findLatestStudyDateByDisciplinaId(id));
            dto.setTotalTemas((long) temaRepository.findByDisciplinaId(id).size());
            dto.setSubtemasEstudados(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaId(id));
            dto.setTemasEstudados(temaRepository.countTemasEstudadosByDisciplinaIds(List.of(id)).stream()
                    .filter(r -> ((Number)r[0]).longValue() == id)
                    .map(r -> ((Number)r[1]).longValue())
                    .findFirst().orElse(0L));
            
            List<Object[]> subCounts = subtemaRepository.countByDisciplinaIds(List.of(id));
            if (!subCounts.isEmpty()) {
                dto.setTotalSubtemas(((Number) subCounts.get(0)[1]).longValue());
            }
        }

        List<Tema> temas = temaRepository.findByDisciplinaId(id);
        if (temas != null && !temas.isEmpty()) {
            List<Long> temaIds = temas.stream().map(Tema::getId).collect(Collectors.toList());
            Map<Long, LocalDateTime> temaDates = toDateMap(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(temaIds));
            Map<Long, Long> temaTotalSub = toMap(subtemaRepository.countByTemaIds(temaIds));
            Map<Long, Long> temaStudSub = toMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(temaIds));

            dto.setTemas(temas.stream().map(tema -> {
                TemaSummaryDto temaDto = temaMapper.toSummaryDto(tema);
                if (metrics != null) {
                    temaDto.setQuestaoStats(statsAssembler.buildStats(tema.getId(), "TEMA", metrics));
                    temaDto.setUltimoEstudo(temaDates.get(tema.getId()));
                    temaDto.setTotalSubtemas(temaTotalSub.getOrDefault(tema.getId(), 0L));
                    temaDto.setSubtemasEstudados(temaStudSub.getOrDefault(tema.getId(), 0L));
                }
                
                List<Subtema> subtemas = subtemaRepository.findByTemaId(tema.getId());
                if (subtemas != null && !subtemas.isEmpty()) {
                    List<Long> subIds = subtemas.stream().map(Subtema::getId).collect(Collectors.toList());
                    Map<Long, Long> subCounts = toMap(estudoSubtemaRepository.countBySubtemaIds(subIds));
                    Map<Long, LocalDateTime> subDates = toDateMap(estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(subIds));

                    temaDto.setSubtemas(subtemas.stream().map(subtema -> {
                        SubtemaSummaryDto subDto = subtemaMapper.toSummaryDto(subtema);
                        if (metrics != null) {
                            subDto.setQuestaoStats(statsAssembler.buildStats(subtema.getId(), "SUBTEMA", metrics));
                            subDto.setTotalEstudos(subCounts.getOrDefault(subtema.getId(), 0L));
                            subDto.setUltimoEstudo(subDates.get(subtema.getId()));
                        }
                        return subDto;
                    }).collect(Collectors.toList()));
                }
                
                return temaDto;
            }).collect(Collectors.toList()));
        }

        return dto;
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
