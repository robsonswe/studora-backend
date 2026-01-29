package com.studora.service;

import com.studora.dto.InstituicaoDto;
import com.studora.entity.Instituicao;
import com.studora.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstituicaoService {

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    public List<InstituicaoDto> findAll() {
        return instituicaoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public InstituicaoDto findById(Long id) {
        return instituicaoRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    public InstituicaoDto save(InstituicaoDto instituicaoDto) {
        Instituicao instituicao = convertToEntity(instituicaoDto);
        return convertToDto(instituicaoRepository.save(instituicao));
    }

    public void deleteById(Long id) {
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
