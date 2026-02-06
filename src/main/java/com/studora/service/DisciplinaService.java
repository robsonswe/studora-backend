package com.studora.service;

import com.studora.dto.disciplina.DisciplinaDetailDto;
import com.studora.dto.disciplina.DisciplinaSummaryDto;
import com.studora.dto.request.DisciplinaCreateRequest;
import com.studora.dto.request.DisciplinaUpdateRequest;
import com.studora.entity.Disciplina;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.DisciplinaMapper;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.TemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DisciplinaService {

    private final DisciplinaRepository disciplinaRepository;
    private final TemaRepository temaRepository;
    private final DisciplinaMapper disciplinaMapper;

    @Transactional(readOnly = true)
    public Page<DisciplinaSummaryDto> findAll(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return disciplinaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    .map(disciplinaMapper::toSummaryDto);
        }
        return disciplinaRepository.findAll(pageable)
                .map(disciplinaMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public DisciplinaDetailDto getDisciplinaDetailById(Long id) {
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));
        return disciplinaMapper.toDetailDto(disciplina);
    }

    public DisciplinaDetailDto create(DisciplinaCreateRequest request) {
        log.info("Criando nova disciplina: {}", request.getNome());
        
        Optional<Disciplina> existing = disciplinaRepository.findByNomeIgnoreCase(request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe uma disciplina com o nome '" + request.getNome() + "'");
        }

        Disciplina disciplina = disciplinaMapper.toEntity(request);
        return disciplinaMapper.toDetailDto(disciplinaRepository.save(disciplina));
    }

    public DisciplinaDetailDto update(Long id, DisciplinaUpdateRequest request) {
        log.info("Atualizando disciplina ID: {}", id);
        
        Disciplina disciplina = disciplinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", id));

        if (request.getNome() != null) {
            Optional<Disciplina> existing = disciplinaRepository.findByNomeIgnoreCase(request.getNome());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new ConflictException("Já existe uma disciplina com o nome '" + request.getNome() + "'");
            }
        }

        disciplinaMapper.updateEntityFromDto(request, disciplina);
        return disciplinaMapper.toDetailDto(disciplinaRepository.save(disciplina));
    }

    public void delete(Long id) {
        log.info("Excluindo disciplina ID: {}", id);
        if (!disciplinaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Disciplina", "ID", id);
        }
        
        if (temaRepository.existsByDisciplinaId(id)) {
            throw new ValidationException("Não é possível excluir uma disciplina que possui temas associados");
        }
        
        disciplinaRepository.deleteById(id);
    }
}