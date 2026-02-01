package com.studora.service;

import com.studora.dto.DisciplinaDto;
import com.studora.entity.Disciplina;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.mapper.DisciplinaMapper;
import com.studora.repository.DisciplinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final DisciplinaMapper disciplinaMapper;
    private final com.studora.repository.TemaRepository temaRepository;

    public Page<DisciplinaDto> findAll(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return disciplinaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    .map(disciplinaMapper::toDto);
        }
        return disciplinaRepository.findAll(pageable)
                .map(disciplinaMapper::toDto);
    }

    public DisciplinaDto getDisciplinaById(Long id) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));
        return disciplinaMapper.toDto(disciplina);
    }

    public DisciplinaDto createDisciplina(DisciplinaDto disciplinaDto) {
        // Check for duplicate disciplina name (case-insensitive)
        Optional<Disciplina> existingDisciplina = disciplinaRepository.findByNomeIgnoreCase(disciplinaDto.getNome());
        if (existingDisciplina.isPresent()) {
            throw new ConflictException("Já existe uma disciplina com o nome '" + disciplinaDto.getNome() + "'");
        }

        Disciplina disciplina = disciplinaMapper.toEntity(disciplinaDto);
        Disciplina savedDisciplina = disciplinaRepository.save(disciplina);
        return disciplinaMapper.toDto(savedDisciplina);
    }

    public DisciplinaDto updateDisciplina(Long id, DisciplinaDto disciplinaDto) {
        Disciplina existingDisciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));

        // Check for duplicate disciplina name (excluding current disciplina, case-insensitive)
        Optional<Disciplina> duplicateDisciplina = disciplinaRepository.findByNomeIgnoreCaseAndIdNot(disciplinaDto.getNome(), id);
        if (duplicateDisciplina.isPresent()) {
            throw new ConflictException("Já existe uma disciplina com o nome '" + disciplinaDto.getNome() + "'");
        }

        disciplinaMapper.updateEntityFromDto(disciplinaDto, existingDisciplina);
        Disciplina updatedDisciplina = disciplinaRepository.save(existingDisciplina);
        return disciplinaMapper.toDto(updatedDisciplina);
    }

    public void deleteDisciplina(Long id) {
        if (!disciplinaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Disciplina", "ID", id);
        }
        if (temaRepository.existsByDisciplinaId(id)) {
            throw new ConflictException("Não é possível excluir a disciplina pois existem temas associados a ela.");
        }
        disciplinaRepository.deleteById(id);
    }
}