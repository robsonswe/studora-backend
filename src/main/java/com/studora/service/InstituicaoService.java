package com.studora.service;

import com.studora.dto.InstituicaoDto;
import com.studora.entity.Instituicao;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.mapper.InstituicaoMapper;
import com.studora.repository.InstituicaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InstituicaoService {

    private final InstituicaoRepository instituicaoRepository;
    private final InstituicaoMapper instituicaoMapper;
    private final com.studora.repository.ConcursoRepository concursoRepository;

    public Page<InstituicaoDto> findAll(String nome, Pageable pageable) {
        if (nome != null && !nome.isBlank()) {
            return instituicaoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                    .map(instituicaoMapper::toDto);
        }
        return instituicaoRepository.findAll(pageable)
                .map(instituicaoMapper::toDto);
    }

    public java.util.List<String> findAllAreas(String search) {
        if (search != null && !search.isBlank()) {
            return instituicaoRepository.findDistinctAreas(search);
        }
        return instituicaoRepository.findDistinctAreas();
    }

    public InstituicaoDto findById(Long id) {
        Instituicao instituicao = instituicaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", id));
        return instituicaoMapper.toDto(instituicao);
    }

    public InstituicaoDto save(InstituicaoDto instituicaoDto) {
        // Check for duplicate instituicao name (excluding current instituicao if updating)
        Optional<Instituicao> existingInstituicao = instituicaoRepository.findByNomeIgnoreCase(instituicaoDto.getNome());
        if (existingInstituicao.isPresent() && !existingInstituicao.get().getId().equals(instituicaoDto.getId())) {
            throw new ConflictException("Já existe uma instituição com o nome '" + instituicaoDto.getNome() + "'");
        }

        Instituicao instituicao;
        if (instituicaoDto.getId() != null) {
            instituicao = instituicaoRepository.findById(instituicaoDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", instituicaoDto.getId()));
            instituicaoMapper.updateEntityFromDto(instituicaoDto, instituicao);
        } else {
            instituicao = instituicaoMapper.toEntity(instituicaoDto);
        }
        return instituicaoMapper.toDto(instituicaoRepository.save(instituicao));
    }

    public void deleteById(Long id) {
        if (!instituicaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Instituição", "ID", id);
        }
        if (concursoRepository.existsByInstituicaoId(id)) {
            throw new com.studora.exception.ConflictException("Não é possível excluir a instituição pois existem concursos associados a ela.");
        }
        instituicaoRepository.deleteById(id);
    }
}
