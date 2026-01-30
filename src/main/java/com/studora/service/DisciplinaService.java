package com.studora.service;

import com.studora.dto.DisciplinaDto;
import com.studora.entity.Disciplina;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DisciplinaService {
    
    @Autowired
    private DisciplinaRepository disciplinaRepository;
    
    public List<DisciplinaDto> getAllDisciplinas() {
        return disciplinaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public DisciplinaDto getDisciplinaById(Long id) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));
        return convertToDto(disciplina);
    }
    
    public DisciplinaDto createDisciplina(DisciplinaDto disciplinaDto) {
        // Check for duplicate disciplina name
        Optional<Disciplina> existingDisciplina = disciplinaRepository.findByNome(disciplinaDto.getNome());
        if (existingDisciplina.isPresent()) {
            throw new ConflictException("Já existe uma disciplina com o nome '" + disciplinaDto.getNome() + "'");
        }

        Disciplina disciplina = convertToEntity(disciplinaDto);
        Disciplina savedDisciplina = disciplinaRepository.save(disciplina);
        return convertToDto(savedDisciplina);
    }
    
    public DisciplinaDto updateDisciplina(Long id, DisciplinaDto disciplinaDto) {
        Disciplina existingDisciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));

        // Check for duplicate disciplina name (excluding current disciplina)
        Optional<Disciplina> duplicateDisciplina = disciplinaRepository.findByNome(disciplinaDto.getNome());
        if (duplicateDisciplina.isPresent() && !duplicateDisciplina.get().getId().equals(id)) {
            throw new ConflictException("Já existe uma disciplina com o nome '" + disciplinaDto.getNome() + "'");
        }

        // Update field
        existingDisciplina.setNome(disciplinaDto.getNome());

        Disciplina updatedDisciplina = disciplinaRepository.save(existingDisciplina);
        return convertToDto(updatedDisciplina);
    }
    
    public void deleteDisciplina(Long id) {
        if (!disciplinaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Disciplina", "ID", id);
        }
        disciplinaRepository.deleteById(id);
    }
    
    private DisciplinaDto convertToDto(Disciplina disciplina) {
        DisciplinaDto dto = new DisciplinaDto();
        dto.setId(disciplina.getId());
        dto.setNome(disciplina.getNome());
        return dto;
    }
    
    private Disciplina convertToEntity(DisciplinaDto dto) {
        return new Disciplina(dto.getNome());
    }
}