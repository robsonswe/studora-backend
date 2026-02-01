package com.studora.service;

import com.studora.dto.SubtemaDto;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.mapper.SubtemaMapper;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SubtemaService {

    private final SubtemaRepository subtemaRepository;
    private final TemaRepository temaRepository;
    private final com.studora.repository.QuestaoRepository questaoRepository;
    private final SubtemaMapper subtemaMapper;

    public Page<SubtemaDto> getAllSubtemas(Pageable pageable) {
        return subtemaRepository.findAll(pageable)
                .map(subtemaMapper::toDto);
    }

    public SubtemaDto getSubtemaById(Long id) {
        Subtema subtema = subtemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", id));
        return subtemaMapper.toDto(subtema);
    }

    public Page<SubtemaDto> getSubtemasByTemaId(Long temaId, Pageable pageable) {
        return subtemaRepository.findByTemaId(temaId, pageable)
                .map(subtemaMapper::toDto);
    }

    public SubtemaDto createSubtema(SubtemaDto subtemaDto) {
        Tema tema = temaRepository.findById(subtemaDto.getTemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", subtemaDto.getTemaId()));

        // Check for duplicate subtema name within the same tema (case-insensitive)
        Optional<Subtema> existingSubtema = subtemaRepository.findByTemaIdAndNomeIgnoreCase(subtemaDto.getTemaId(), subtemaDto.getNome());
        if (existingSubtema.isPresent()) {
            throw new ConflictException("Já existe um subtema com o nome '" + subtemaDto.getNome() + "' no tema com ID: " + subtemaDto.getTemaId());
        }

        Subtema subtema = subtemaMapper.toEntity(subtemaDto);
        subtema.setTema(tema);

        Subtema savedSubtema = subtemaRepository.save(subtema);
        return subtemaMapper.toDto(savedSubtema);
    }

    public SubtemaDto updateSubtema(Long id, SubtemaDto subtemaDto) {
        Subtema existingSubtema = subtemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", id));

        Tema tema = temaRepository.findById(subtemaDto.getTemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", subtemaDto.getTemaId()));

        // Check for duplicate subtema name within the same tema (excluding current subtema, case-insensitive)
        Optional<Subtema> duplicateSubtema = subtemaRepository.findByTemaIdAndNomeIgnoreCaseAndIdNot(tema.getId(), subtemaDto.getNome(), id);
        if (duplicateSubtema.isPresent()) {
            throw new ConflictException("Já existe um subtema com o nome '" + subtemaDto.getNome() + "' no tema com ID: " + tema.getId());
        }

        subtemaMapper.updateEntityFromDto(subtemaDto, existingSubtema);
        existingSubtema.setTema(tema);

        Subtema updatedSubtema = subtemaRepository.save(existingSubtema);
        return subtemaMapper.toDto(updatedSubtema);
    }

    public void deleteSubtema(Long id) {
        if (!subtemaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subtema", "ID", id);
        }
        if (questaoRepository.existsBySubtemasId(id)) {
            throw new ConflictException("Não é possível excluir o subtema pois existem questões associadas a ele.");
        }
        subtemaRepository.deleteById(id);
    }
}