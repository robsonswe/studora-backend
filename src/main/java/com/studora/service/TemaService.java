package com.studora.service;

import com.studora.dto.TemaDto;
import com.studora.entity.Disciplina;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.mapper.TemaMapper;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.TemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TemaService {

    private final TemaRepository temaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final com.studora.repository.SubtemaRepository subtemaRepository;
    private final TemaMapper temaMapper;

    public Page<TemaDto> getAllTemas(Pageable pageable) {
        return temaRepository.findAll(pageable)
                .map(temaMapper::toDto);
    }

    public TemaDto getTemaById(Long id) {
        Tema tema = temaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", id));
        return temaMapper.toDto(tema);
    }

    public Page<TemaDto> getTemasByDisciplinaId(Long disciplinaId, Pageable pageable) {
        return temaRepository.findByDisciplinaId(disciplinaId, pageable)
                .map(temaMapper::toDto);
    }

    public TemaDto createTema(TemaDto temaDto) {
        Disciplina disciplina = disciplinaRepository.findById(temaDto.getDisciplinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", temaDto.getDisciplinaId()));

        // Check for duplicate tema name within the same disciplina (case-insensitive)
        Optional<Tema> existingTema = temaRepository.findByDisciplinaIdAndNomeIgnoreCase(temaDto.getDisciplinaId(), temaDto.getNome());
        if (existingTema.isPresent()) {
            throw new ConflictException("Já existe um tema com o nome '" + temaDto.getNome() + "' na disciplina com ID: " + temaDto.getDisciplinaId());
        }

        Tema tema = temaMapper.toEntity(temaDto);
        tema.setDisciplina(disciplina);

        Tema savedTema = temaRepository.save(tema);
        return temaMapper.toDto(savedTema);
    }

    public TemaDto updateTema(Long id, TemaDto temaDto) {
        Tema existingTema = temaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", id));

        Disciplina disciplina = disciplinaRepository.findById(temaDto.getDisciplinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", temaDto.getDisciplinaId()));

        // Check for duplicate tema name within the same disciplina (excluding current tema, case-insensitive)
        Optional<Tema> duplicateTema = temaRepository.findByDisciplinaIdAndNomeIgnoreCaseAndIdNot(disciplina.getId(), temaDto.getNome(), id);
        if (duplicateTema.isPresent()) {
            throw new ConflictException("Já existe um tema com o nome '" + temaDto.getNome() + "' na disciplina com ID: " + disciplina.getId());
        }

        // Update fields
        temaMapper.updateEntityFromDto(temaDto, existingTema);
        existingTema.setDisciplina(disciplina);

        Tema updatedTema = temaRepository.save(existingTema);
        return temaMapper.toDto(updatedTema);
    }

    public void deleteTema(Long id) {
        if (!temaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tema", "ID", id);
        }
        if (subtemaRepository.existsByTemaId(id)) {
            throw new ConflictException("Não é possível excluir o tema pois existem subtemas associados a ele.");
        }
        temaRepository.deleteById(id);
    }
}