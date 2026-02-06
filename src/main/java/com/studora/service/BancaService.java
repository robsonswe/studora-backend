package com.studora.service;

import com.studora.dto.banca.BancaDetailDto;
import com.studora.dto.banca.BancaSummaryDto;
import com.studora.dto.request.BancaCreateRequest;
import com.studora.dto.request.BancaUpdateRequest;
import com.studora.entity.Banca;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.BancaMapper;
import com.studora.repository.BancaRepository;
import com.studora.repository.ConcursoRepository;
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
public class BancaService {

    private final BancaRepository bancaRepository;
    private final BancaMapper bancaMapper;
    private final ConcursoRepository concursoRepository;

    @Transactional(readOnly = true)
    public Page<BancaSummaryDto> findAll(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return bancaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    .map(bancaMapper::toSummaryDto);
        }
        return bancaRepository.findAll(pageable)
                .map(bancaMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public BancaDetailDto getBancaDetailById(Long id) {
        Banca banca = bancaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", id));
        return bancaMapper.toDetailDto(banca);
    }

    public BancaDetailDto create(BancaCreateRequest request) {
        log.info("Criando nova banca: {}", request.getNome());
        
        Optional<Banca> existingBanca = bancaRepository.findByNomeIgnoreCase(request.getNome());
        if (existingBanca.isPresent()) {
            throw new ConflictException("Já existe uma banca com o nome '" + request.getNome() + "'");
        }

        Banca banca = bancaMapper.toEntity(request);
        return bancaMapper.toDetailDto(bancaRepository.save(banca));
    }

    public BancaDetailDto update(Long id, BancaUpdateRequest request) {
        log.info("Atualizando banca ID: {}", id);
        
        Banca banca = bancaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", id));

        if (request.getNome() != null) {
            Optional<Banca> existingBanca = bancaRepository.findByNomeIgnoreCase(request.getNome());
            if (existingBanca.isPresent() && !existingBanca.get().getId().equals(id)) {
                throw new ConflictException("Já existe uma banca com o nome '" + request.getNome() + "'");
            }
        }

        bancaMapper.updateEntityFromDto(request, banca);
        return bancaMapper.toDetailDto(bancaRepository.save(banca));
    }

    public void delete(Long id) {
        log.info("Excluindo banca ID: {}", id);
        if (!bancaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Banca", "ID", id);
        }
        
        // Check for associated exams
        if (concursoRepository.existsByBancaId(id)) {
            throw new ValidationException("Não é possível excluir uma banca que possui concursos associados");
        }
        
        bancaRepository.deleteById(id);
    }
}
