package com.studora.service;

import com.studora.dto.SubtemaDto;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubtemaService {

    @Autowired
    private SubtemaRepository subtemaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private com.studora.repository.QuestaoRepository questaoRepository;

    public Page<SubtemaDto> getAllSubtemas(Pageable pageable) {
        return subtemaRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public SubtemaDto getSubtemaById(Long id) {
        Subtema subtema = subtemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", id));
        return convertToDto(subtema);
    }

    public Page<SubtemaDto> getSubtemasByTemaId(Long temaId, Pageable pageable) {
        return subtemaRepository.findByTemaId(temaId, pageable)
                .map(this::convertToDto);
    }

    public SubtemaDto createSubtema(SubtemaDto subtemaDto) {
        Tema tema = temaRepository.findById(subtemaDto.getTemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", subtemaDto.getTemaId()));

        // Check for duplicate subtema name within the same tema (case-insensitive)
        Optional<Subtema> existingSubtema = subtemaRepository.findByTemaIdAndNomeIgnoreCase(subtemaDto.getTemaId(), subtemaDto.getNome());
        if (existingSubtema.isPresent()) {
            throw new ConflictException("Já existe um subtema com o nome '" + subtemaDto.getNome() + "' no tema com ID: " + subtemaDto.getTemaId());
        }

        Subtema subtema = convertToEntity(subtemaDto);
        subtema.setTema(tema);

        Subtema savedSubtema = subtemaRepository.save(subtema);
        return convertToDto(savedSubtema);
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

        // Update fields
        existingSubtema.setTema(tema);
        existingSubtema.setNome(subtemaDto.getNome());

        Subtema updatedSubtema = subtemaRepository.save(existingSubtema);
        return convertToDto(updatedSubtema);
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

    private SubtemaDto convertToDto(Subtema subtema) {
        SubtemaDto dto = new SubtemaDto();
        dto.setId(subtema.getId());
        dto.setTemaId(subtema.getTema().getId());
        dto.setNome(subtema.getNome());
        return dto;
    }

    private Subtema convertToEntity(SubtemaDto dto) {
        return new Subtema(null, dto.getNome()); // tema will be set separately
    }
}