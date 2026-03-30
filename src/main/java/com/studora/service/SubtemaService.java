package com.studora.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubtemaService {

    private final SubtemaRepository subtemaRepository;
    private final TemaRepository temaRepository;
    private final QuestaoRepository questaoRepository;
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

        List<Long> ids = page.getContent().stream().map(Subtema::getId).collect(java.util.stream.Collectors.toList());
        
        // Fetch counts
        java.util.Map<Long, Long> counts = estudoSubtemaRepository.countBySubtemaIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                    row -> ((Number) row[0]).longValue(), 
                    row -> ((Number) row[1]).longValue()));
        
        // Fetch latest dates
        java.util.Map<Long, java.time.LocalDateTime> dates = estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                    row -> ((Number) row[0]).longValue(), 
                    row -> {
                        Object val = row[1];
                        if (val instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) val;
                        if (val instanceof String) return java.time.LocalDateTime.parse((String) val, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        return null;
                    }));

        return page.map(subtema -> {
            SubtemaSummaryDto dto = subtemaMapper.toSummaryDto(subtema);
            dto.setTotalEstudos(counts.getOrDefault(subtema.getId(), 0L));
            dto.setUltimoEstudo(dates.get(subtema.getId()));
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

        List<Long> ids = subtemas.stream().map(Subtema::getId).collect(java.util.stream.Collectors.toList());
        
        java.util.Map<Long, Long> counts = estudoSubtemaRepository.countBySubtemaIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                    row -> ((Number) row[0]).longValue(), 
                    row -> ((Number) row[1]).longValue()));
        
        java.util.Map<Long, java.time.LocalDateTime> dates = estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(
                    row -> ((Number) row[0]).longValue(), 
                    row -> {
                        Object val = row[1];
                        if (val instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) val;
                        if (val instanceof String) return java.time.LocalDateTime.parse((String) val, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        return null;
                    }));

        return subtemas.stream()
                .map(subtema -> {
                    SubtemaSummaryDto dto = subtemaMapper.toSummaryDto(subtema);
                    dto.setTotalEstudos(counts.getOrDefault(subtema.getId(), 0L));
                    dto.setUltimoEstudo(dates.get(subtema.getId()));
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
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
}