package com.studora.service;

import com.studora.dto.DisciplinaDto;
import com.studora.entity.Disciplina;
import com.studora.repository.DisciplinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com ID: " + id));
        return convertToDto(disciplina);
    }
    
    public DisciplinaDto createDisciplina(DisciplinaDto disciplinaDto) {
        Disciplina disciplina = convertToEntity(disciplinaDto);
        Disciplina savedDisciplina = disciplinaRepository.save(disciplina);
        return convertToDto(savedDisciplina);
    }
    
    public DisciplinaDto updateDisciplina(Long id, DisciplinaDto disciplinaDto) {
        Disciplina existingDisciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com ID: " + id));
        
        // Update field
        existingDisciplina.setNome(disciplinaDto.getNome());
        
        Disciplina updatedDisciplina = disciplinaRepository.save(existingDisciplina);
        return convertToDto(updatedDisciplina);
    }
    
    public void deleteDisciplina(Long id) {
        if (!disciplinaRepository.existsById(id)) {
            throw new RuntimeException("Disciplina não encontrada com ID: " + id);
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