package com.studora.service;

import com.studora.dto.TemaDto;
import com.studora.entity.Disciplina;
import com.studora.entity.Tema;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.TemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TemaService {
    
    @Autowired
    private TemaRepository temaRepository;
    
    @Autowired
    private DisciplinaRepository disciplinaRepository;
    
    public List<TemaDto> getAllTemas() {
        return temaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public TemaDto getTemaById(Long id) {
        Tema tema = temaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tema não encontrado com ID: " + id));
        return convertToDto(tema);
    }
    
    public List<TemaDto> getTemasByDisciplinaId(Long disciplinaId) {
        return temaRepository.findByDisciplinaId(disciplinaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public TemaDto createTema(TemaDto temaDto) {
        Disciplina disciplina = disciplinaRepository.findById(temaDto.getDisciplinaId())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com ID: " + temaDto.getDisciplinaId()));
        
        Tema tema = convertToEntity(temaDto);
        tema.setDisciplina(disciplina);
        
        Tema savedTema = temaRepository.save(tema);
        return convertToDto(savedTema);
    }
    
    public TemaDto updateTema(Long id, TemaDto temaDto) {
        Tema existingTema = temaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tema não encontrado com ID: " + id));
        
        Disciplina disciplina = disciplinaRepository.findById(temaDto.getDisciplinaId())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada com ID: " + temaDto.getDisciplinaId()));
        
        // Update fields
        existingTema.setDisciplina(disciplina);
        existingTema.setNome(temaDto.getNome());
        
        Tema updatedTema = temaRepository.save(existingTema);
        return convertToDto(updatedTema);
    }
    
    public void deleteTema(Long id) {
        if (!temaRepository.existsById(id)) {
            throw new RuntimeException("Tema não encontrado com ID: " + id);
        }
        temaRepository.deleteById(id);
    }
    
    private TemaDto convertToDto(Tema tema) {
        TemaDto dto = new TemaDto();
        dto.setId(tema.getId());
        dto.setDisciplinaId(tema.getDisciplina().getId());
        dto.setNome(tema.getNome());
        return dto;
    }
    
    private Tema convertToEntity(TemaDto dto) {
        return new Tema(null, dto.getNome()); // disciplina will be set separately
    }
}