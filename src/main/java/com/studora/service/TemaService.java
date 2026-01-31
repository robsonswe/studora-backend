package com.studora.service;

import com.studora.dto.TemaDto;
import com.studora.entity.Disciplina;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.TemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TemaService {

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private com.studora.repository.SubtemaRepository subtemaRepository;

    public List<TemaDto> getAllTemas() {
        return temaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TemaDto getTemaById(Long id) {
        Tema tema = temaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", id));
        return convertToDto(tema);
    }

    public List<TemaDto> getTemasByDisciplinaId(Long disciplinaId) {
        return temaRepository.findByDisciplinaId(disciplinaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TemaDto createTema(TemaDto temaDto) {
        Disciplina disciplina = disciplinaRepository.findById(temaDto.getDisciplinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", temaDto.getDisciplinaId()));

        // Check for duplicate tema name within the same disciplina (case-insensitive)
        Optional<Tema> existingTema = temaRepository.findByDisciplinaIdAndNomeIgnoreCase(temaDto.getDisciplinaId(), temaDto.getNome());
        if (existingTema.isPresent()) {
            throw new ConflictException("Já existe um tema com o nome '" + temaDto.getNome() + "' na disciplina com ID: " + temaDto.getDisciplinaId());
        }

        Tema tema = convertToEntity(temaDto);
        tema.setDisciplina(disciplina);

        Tema savedTema = temaRepository.save(tema);
        return convertToDto(savedTema);
    }

    public TemaDto updateTema(Long id, TemaDto temaDto) {
        Tema existingTema = temaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", id));

        Disciplina disciplina = disciplinaRepository.findById(temaDto.getDisciplinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", temaDto.getDisciplinaId()));

        // Check for duplicate tema name within the same disciplina (excluding current tema, case-insensitive)
        Optional<Tema> duplicateTema = temaRepository.findByDisciplinaIdAndNomeIgnoreCaseAndIdNot(disciplina.getId(), temaDto.getNome(), id);
        if (duplicateTema.isPresent()) {
            throw new ConflictException("Já existe um tema com o nome '" + temaDto.getNome() + "' na disciplina com ID: " + disciplina.getId());
        }

        // Update fields
        existingTema.setDisciplina(disciplina);
        existingTema.setNome(temaDto.getNome());

        Tema updatedTema = temaRepository.save(existingTema);
        return convertToDto(updatedTema);
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