package com.studora.service;

import com.studora.dto.BancaDto;
import com.studora.entity.Banca;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.mapper.BancaMapper;
import com.studora.repository.BancaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BancaService {

    private final BancaRepository bancaRepository;
    private final BancaMapper bancaMapper;
    private final com.studora.repository.ConcursoRepository concursoRepository;

    public Page<BancaDto> findAll(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return bancaRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    .map(bancaMapper::toDto);
        }
        return bancaRepository.findAll(pageable)
                .map(bancaMapper::toDto);
    }

    public BancaDto findById(Long id) {
        Banca banca = bancaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", id));
        return bancaMapper.toDto(banca);
    }

    public BancaDto save(BancaDto bancaDto) {
        // Check for duplicate banca name (excluding current banca if updating)
        Optional<Banca> existingBanca = bancaRepository.findByNomeIgnoreCase(bancaDto.getNome());
        if (existingBanca.isPresent() && !existingBanca.get().getId().equals(bancaDto.getId())) {
            throw new ConflictException("Já existe uma banca com o nome '" + bancaDto.getNome() + "'");
        }

        Banca banca;
        if (bancaDto.getId() != null) {
            banca = bancaRepository.findById(bancaDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", bancaDto.getId()));
            bancaMapper.updateEntityFromDto(bancaDto, banca);
        } else {
            banca = bancaMapper.toEntity(bancaDto);
        }
        return bancaMapper.toDto(bancaRepository.save(banca));
    }

    public void deleteById(Long id) {
        if (!bancaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Banca", "ID", id);
        }
        if (concursoRepository.existsByBancaId(id)) {
            throw new ConflictException("Não é possível excluir a banca pois existem concursos associados a ela.");
        }
        bancaRepository.deleteById(id);
    }
}
