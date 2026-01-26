package com.studora.service;

import com.studora.dto.ConcursoDto;
import com.studora.entity.Concurso;
import com.studora.repository.ConcursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConcursoService {
    
    @Autowired
    private ConcursoRepository concursoRepository;
    
    public List<ConcursoDto> getAllConcursos() {
        return concursoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ConcursoDto getConcursoById(Long id) {
        Concurso concurso = concursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Concurso não encontrado com ID: " + id));
        return convertToDto(concurso);
    }
    
    public ConcursoDto createConcurso(ConcursoDto concursoDto) {
        if (concursoDto == null) {
            throw new IllegalArgumentException("ConcursoDto não pode ser nulo.");
        }
        Concurso concurso = convertToEntity(concursoDto);
        Concurso savedConcurso = concursoRepository.save(concurso);
        return convertToDto(savedConcurso);
    }
    
    public ConcursoDto updateConcurso(Long id, ConcursoDto concursoDto) {
        Concurso existingConcurso = concursoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Concurso não encontrado com ID: " + id));
        
        // Update fields
        existingConcurso.setNome(concursoDto.getNome());
        existingConcurso.setBanca(concursoDto.getBanca());
        existingConcurso.setAno(concursoDto.getAno());
        existingConcurso.setCargo(concursoDto.getCargo());
        existingConcurso.setNivel(concursoDto.getNivel());
        existingConcurso.setArea(concursoDto.getArea());
        
        Concurso updatedConcurso = concursoRepository.save(existingConcurso);
        return convertToDto(updatedConcurso);
    }
    
    public void deleteConcurso(Long id) {
        if (!concursoRepository.existsById(id)) {
            throw new RuntimeException("Concurso não encontrado com ID: " + id);
        }
        concursoRepository.deleteById(id);
    }
    
    private ConcursoDto convertToDto(Concurso concurso) {
        ConcursoDto dto = new ConcursoDto();
        dto.setId(concurso.getId());
        dto.setNome(concurso.getNome());
        dto.setBanca(concurso.getBanca());
        dto.setAno(concurso.getAno());
        dto.setCargo(concurso.getCargo());
        dto.setNivel(concurso.getNivel());
        dto.setArea(concurso.getArea());
        return dto;
    }
    
    private Concurso convertToEntity(ConcursoDto dto) {
        return new Concurso(dto.getNome(), dto.getBanca(), dto.getAno(), dto.getCargo(), dto.getNivel(), dto.getArea());
    }
}