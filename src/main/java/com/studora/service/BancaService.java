package com.studora.service;

import com.studora.dto.BancaDto;
import com.studora.entity.Banca;
import com.studora.repository.BancaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BancaService {

    @Autowired
    private BancaRepository bancaRepository;

    public List<BancaDto> findAll() {
        return bancaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BancaDto findById(Long id) {
        return bancaRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    public BancaDto save(BancaDto bancaDto) {
        Banca banca = convertToEntity(bancaDto);
        return convertToDto(bancaRepository.save(banca));
    }

    public void deleteById(Long id) {
        bancaRepository.deleteById(id);
    }

    private BancaDto convertToDto(Banca banca) {
        BancaDto bancaDto = new BancaDto();
        bancaDto.setId(banca.getId());
        bancaDto.setNome(banca.getNome());
        return bancaDto;
    }

    private Banca convertToEntity(BancaDto bancaDto) {
        Banca banca = new Banca();
        banca.setId(bancaDto.getId());
        banca.setNome(bancaDto.getNome());
        return banca;
    }
}
