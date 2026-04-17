package com.studora.service;

import com.studora.dto.subtema.EstudoSubtemaDto;
import com.studora.entity.EstudoSubtema;
import com.studora.entity.Subtema;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.EstudoSubtemaRepository;
import com.studora.repository.SubtemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EstudoSubtemaService {

    private final EstudoSubtemaRepository estudoSubtemaRepository;
    private final SubtemaRepository subtemaRepository;

    @CacheEvict(value = {"disciplina-stats", "tema-stats", "subtema-stats"}, allEntries = true)
    public Long markAsStudied(Long subtemaId) {
        log.info("Marcando subtema ID: {} como estudado", subtemaId);
        Subtema subtema = subtemaRepository.findById(subtemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", subtemaId));
        
        EstudoSubtema estudo = new EstudoSubtema(subtema);
        estudo = estudoSubtemaRepository.save(estudo);
        estudoSubtemaRepository.flush();
        return estudo.getId();
    }

    @CacheEvict(value = {"disciplina-stats", "tema-stats", "subtema-stats"}, allEntries = true)
    public void deleteEstudo(Long estudoId) {
        log.info("Excluindo sessão de estudo ID: {}", estudoId);
        if (!estudoSubtemaRepository.existsById(estudoId)) {
            throw new ResourceNotFoundException("EstudoSubtema", "ID", estudoId);
        }
        estudoSubtemaRepository.deleteById(estudoId);
        estudoSubtemaRepository.flush();
    }

    @Transactional(readOnly = true)
    public List<EstudoSubtemaDto> getEstudosBySubtema(Long subtemaId) {
        return estudoSubtemaRepository.findBySubtemaIdOrderByCreatedAtDesc(subtemaId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getStudyCount(Long subtemaId) {
        return estudoSubtemaRepository.countBySubtemaId(subtemaId);
    }

    private EstudoSubtemaDto toDto(EstudoSubtema entity) {
        EstudoSubtemaDto dto = new EstudoSubtemaDto();
        dto.setId(entity.getId());
        dto.setSubtemaId(entity.getSubtema().getId());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
