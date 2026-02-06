package com.studora.service;

import com.studora.dto.tema.TemaDetailDto;
import com.studora.dto.tema.TemaSummaryDto;
import com.studora.dto.request.TemaCreateRequest;
import com.studora.dto.request.TemaUpdateRequest;
import com.studora.entity.Disciplina;
import com.studora.entity.Tema;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.TemaMapper;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.SubtemaRepository;
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
public class TemaService {

    private final TemaRepository temaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final SubtemaRepository subtemaRepository;
    private final TemaMapper temaMapper;

    @Transactional(readOnly = true)
    public Page<TemaSummaryDto> findAll(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return temaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    .map(temaMapper::toSummaryDto);
        }
        return temaRepository.findAll(pageable)
                .map(temaMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public TemaDetailDto getTemaDetailById(Long id) {
        Tema tema = temaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", id));
        return temaMapper.toDetailDto(tema);
    }

    public TemaDetailDto create(TemaCreateRequest request) {
        log.info("Criando novo tema: {} na disciplina ID: {}", request.getNome(), request.getDisciplinaId());
        
        Optional<Tema> existing = temaRepository.findByDisciplinaIdAndNomeIgnoreCase(request.getDisciplinaId(), request.getNome());
        if (existing.isPresent()) {
            throw new ConflictException("Já existe um tema com o nome '" + request.getNome() + "' na disciplina com ID: " + request.getDisciplinaId());
        }

        Disciplina disciplina = disciplinaRepository.findById(request.getDisciplinaId())
                .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", request.getDisciplinaId()));

        Tema tema = temaMapper.toEntity(request);
        tema.setDisciplina(disciplina);
        
        return temaMapper.toDetailDto(temaRepository.save(tema));
    }

    public TemaDetailDto update(Long id, TemaUpdateRequest request) {
        log.info("Atualizando tema ID: {}", id);
        
        Tema tema = temaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tema", "ID", id));

        if (request.getNome() != null || request.getDisciplinaId() != null) {
            Long discId = request.getDisciplinaId() != null ? request.getDisciplinaId() : tema.getDisciplina().getId();
            String nome = request.getNome() != null ? request.getNome() : tema.getNome();
            
            Optional<Tema> existing = temaRepository.findByDisciplinaIdAndNomeIgnoreCase(discId, nome);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new ConflictException("Já existe um tema com o nome '" + nome + "' na disciplina com ID: " + discId);
            }
        }

        if (request.getDisciplinaId() != null) {
            Disciplina disciplina = disciplinaRepository.findById(request.getDisciplinaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disciplina", "ID", request.getDisciplinaId()));
            tema.setDisciplina(disciplina);
        }

        temaMapper.updateEntityFromDto(request, tema);
        return temaMapper.toDetailDto(temaRepository.save(tema));
    }

    public void delete(Long id) {
        log.info("Excluindo tema ID: {}", id);
        if (!temaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tema", "ID", id);
        }
        
        if (subtemaRepository.existsByTemaId(id)) {
            throw new ValidationException("Não é possível excluir um tema que possui subtemas associados");
        }
        
        temaRepository.deleteById(id);
    }
}