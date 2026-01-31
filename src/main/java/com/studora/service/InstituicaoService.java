package com.studora.service;

import com.studora.dto.InstituicaoDto;
import com.studora.entity.Instituicao;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InstituicaoService {

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private com.studora.repository.ConcursoRepository concursoRepository;

    public List<InstituicaoDto> findAll() {
        return instituicaoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public InstituicaoDto findById(Long id) {
        Instituicao instituicao = instituicaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", id));
        return convertToDto(instituicao);
    }

    public InstituicaoDto save(InstituicaoDto instituicaoDto) {
        // Check for duplicate instituicao name (excluding current instituicao if updating)
        Optional<Instituicao> existingInstituicao = instituicaoRepository.findByNome(instituicaoDto.getNome());
        if (existingInstituicao.isPresent() && !existingInstituicao.get().getId().equals(instituicaoDto.getId())) {
            throw new ConflictException("Já existe uma instituição com o nome '" + instituicaoDto.getNome() + "'");
        }

        Instituicao instituicao;
        if (instituicaoDto.getId() != null) {
            instituicao = instituicaoRepository.findById(instituicaoDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", instituicaoDto.getId()));
            instituicao.setNome(instituicaoDto.getNome());
        } else {
            instituicao = convertToEntity(instituicaoDto);
        }
        return convertToDto(instituicaoRepository.save(instituicao));
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

    private InstituicaoDto convertToDto(Instituicao instituicao) {
        InstituicaoDto instituicaoDto = new InstituicaoDto();
        instituicaoDto.setId(instituicao.getId());
        instituicaoDto.setNome(instituicao.getNome());
        return instituicaoDto;
    }

    private Instituicao convertToEntity(InstituicaoDto instituicaoDto) {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(instituicaoDto.getId());
        instituicao.setNome(instituicaoDto.getNome());
        return instituicao;
    }
}
