package com.studora.service;

import com.studora.dto.SubtemaDto;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubtemaService {
    
    @Autowired
    private SubtemaRepository subtemaRepository;
    
    @Autowired
    private TemaRepository temaRepository;
    
    public List<SubtemaDto> getAllSubtemas() {
        return subtemaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public SubtemaDto getSubtemaById(Long id) {
        Subtema subtema = subtemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", id));
        return convertToDto(subtema);
    }
    
    public List<SubtemaDto> getSubtemasByTemaId(Long temaId) {
        return subtemaRepository.findByTemaId(temaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public SubtemaDto createSubtema(SubtemaDto subtemaDto) {
        Tema tema = temaRepository.findById(subtemaDto.getTemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", subtemaDto.getTemaId()));
        
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