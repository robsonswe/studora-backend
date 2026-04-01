package com.studora.service;

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
import com.studora.repository.EstudoSubtemaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TemaService {

    private final TemaRepository temaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final SubtemaRepository subtemaRepository;
    private final EstudoSubtemaRepository estudoSubtemaRepository;
    private final TemaMapper temaMapper;
    private final SubtemaService subtemaService;

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
        Map<Long, Long> totalEstudosMap = toCountMap(estudoSubtemaRepository.countByTemaIds(ids));
        Map<Long, LocalDateTime> ultimoEstudoMap = toDateMap(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(ids));
        Map<Long, Long> totalSubtemasMap = toCountMap(subtemaRepository.countByTemaIds(ids));
        Map<Long, Long> subtemasEstudadosMap = toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(ids));

        return page.map(tema -> {
            TemaSummaryDto dto = temaMapper.toSummaryDto(tema);
            dto.setTotalEstudos(totalEstudosMap.getOrDefault(tema.getId(), 0L));
            dto.setUltimoEstudo(ultimoEstudoMap.get(tema.getId()));
            dto.setTotalSubtemas(totalSubtemasMap.getOrDefault(tema.getId(), 0L));
            dto.setSubtemasEstudados(subtemasEstudadosMap.getOrDefault(tema.getId(), 0L));
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
        dto.setTotalSubtemas(toCountMap(subtemaRepository.countByTemaIds(temaIds)).getOrDefault(id, 0L));
        dto.setSubtemasEstudados(toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(temaIds)).getOrDefault(id, 0L));

        // Enrich nested disciplina
        if (dto.getDisciplina() != null) {
            Long discId = dto.getDisciplina().getId();
            List<Long> singleDiscId = List.of(discId);
            dto.getDisciplina().setTotalEstudos(toCountMap(estudoSubtemaRepository.countByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            dto.getDisciplina().setUltimoEstudo(toDateMap(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(singleDiscId)).get(discId));
            dto.getDisciplina().setTotalTemas(toCountMap(temaRepository.countByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            dto.getDisciplina().setTotalSubtemas(toCountMap(subtemaRepository.countByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            dto.getDisciplina().setSubtemasEstudados(toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(singleDiscId)).getOrDefault(discId, 0L));
            dto.getDisciplina().setTemasEstudados(computeTemasEstudadosForDisciplina(discId));
        }

        // Enrich nested subtemas
        dto.setSubtemas(subtemaService.findByTemaId(id));

        return dto;
    }

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
        Map<Long, Long> totalEstudosMap = toCountMap(estudoSubtemaRepository.countByTemaIds(ids));
        Map<Long, LocalDateTime> ultimoEstudoMap = toDateMap(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(ids));
        Map<Long, Long> totalSubtemasMap = toCountMap(subtemaRepository.countByTemaIds(ids));
        Map<Long, Long> subtemasEstudadosMap = toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(ids));

        return temas.stream().map(tema -> {
            TemaSummaryDto dto = temaMapper.toSummaryDto(tema);
            dto.setTotalEstudos(totalEstudosMap.getOrDefault(tema.getId(), 0L));
            dto.setUltimoEstudo(ultimoEstudoMap.get(tema.getId()));
            dto.setTotalSubtemas(totalSubtemasMap.getOrDefault(tema.getId(), 0L));
            dto.setSubtemasEstudados(subtemasEstudadosMap.getOrDefault(tema.getId(), 0L));
            return dto;
        }).collect(Collectors.toList());
    }

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

    private LocalDateTime parseDate(Object val) {
        if (val instanceof LocalDateTime) return (LocalDateTime) val;
        if (val instanceof String) return LocalDateTime.parse((String) val, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return null;
    }

    private long computeTemasEstudadosForDisciplina(Long disciplinaId) {
        List<Tema> temas = temaRepository.findByDisciplinaId(disciplinaId);
        if (temas.isEmpty()) {
            return 0L;
        }
        List<Long> temaIds = temas.stream().map(Tema::getId).toList();
        Map<Long, Long> totalSubtemasByTema = toCountMap(subtemaRepository.countByTemaIds(temaIds));
        Map<Long, Long> studiedSubtemasByTema = toCountMap(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(temaIds));
        return temas.stream().filter(t -> {
            Long total = totalSubtemasByTema.get(t.getId());
            Long studied = studiedSubtemasByTema.get(t.getId());
            return total != null && total > 0 && total.equals(studied);
        }).count();
    }
}
