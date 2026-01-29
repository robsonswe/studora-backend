package com.studora.service;



import com.studora.dto.ConcursoDto;

import com.studora.entity.Banca;

import com.studora.entity.Concurso;

import com.studora.entity.Instituicao;

import com.studora.repository.BancaRepository;

import com.studora.repository.ConcursoRepository;

import com.studora.repository.InstituicaoRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;



import java.util.List;

import java.util.stream.Collectors;



@Service

public class ConcursoService {

    

    @Autowired

    private ConcursoRepository concursoRepository;



    @Autowired

    private InstituicaoRepository instituicaoRepository;



    @Autowired

    private BancaRepository bancaRepository;

    

    public List<ConcursoDto> findAll() {

        return concursoRepository.findAll().stream()

                .map(this::convertToDto)

                .collect(Collectors.toList());

    }

    

    public ConcursoDto findById(Long id) {

        Concurso concurso = concursoRepository.findById(id)

                .orElseThrow(() -> new RuntimeException("Concurso não encontrado com ID: " + id));

        return convertToDto(concurso);

    }

    

    public ConcursoDto save(ConcursoDto concursoDto) {

        if (concursoDto == null) {

            throw new IllegalArgumentException("ConcursoDto não pode ser nulo.");

        }

        Concurso concurso;
        if (concursoDto.getId() != null) {
            // Update existing concurso
            Concurso existingConcurso = concursoRepository.findById(concursoDto.getId())
                    .orElseThrow(() -> new RuntimeException("Concurso não encontrado com ID: " + concursoDto.getId()));

            // Update the existing entity with new values
            Instituicao instituicao = instituicaoRepository.findById(concursoDto.getInstituicaoId())
                    .orElseThrow(() -> new RuntimeException("Instituicao not found"));
            Banca banca = bancaRepository.findById(concursoDto.getBancaId())
                    .orElseThrow(() -> new RuntimeException("Banca not found"));

            existingConcurso.setInstituicao(instituicao);
            existingConcurso.setBanca(banca);
            existingConcurso.setAno(concursoDto.getAno());

            concurso = existingConcurso;
        } else {
            // Create new concurso
            concurso = convertToEntity(concursoDto);
        }

        Concurso savedConcurso = concursoRepository.save(concurso);

        return convertToDto(savedConcurso);

    }

    

    public void deleteById(Long id) {

        if (!concursoRepository.existsById(id)) {

            throw new RuntimeException("Concurso não encontrado com ID: " + id);

        }

        concursoRepository.deleteById(id);

    }

    

    private ConcursoDto convertToDto(Concurso concurso) {

        ConcursoDto dto = new ConcursoDto();

        dto.setId(concurso.getId());

        dto.setInstituicaoId(concurso.getInstituicao().getId());

        dto.setBancaId(concurso.getBanca().getId());

        dto.setAno(concurso.getAno());

        return dto;

    }

    

    private Concurso convertToEntity(ConcursoDto dto) {

        Instituicao instituicao = instituicaoRepository.findById(dto.getInstituicaoId())

                .orElseThrow(() -> new RuntimeException("Instituicao not found"));

        Banca banca = bancaRepository.findById(dto.getBancaId())

                .orElseThrow(() -> new RuntimeException("Banca not found"));

        

        Concurso concurso = new Concurso();

        concurso.setId(dto.getId());

        concurso.setInstituicao(instituicao);

        concurso.setBanca(banca);

        concurso.setAno(dto.getAno());

        

        return concurso;

    }

}
