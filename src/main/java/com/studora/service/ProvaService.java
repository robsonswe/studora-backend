package com.studora.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studora.dto.prova.ProvaDetailDto;
import com.studora.dto.prova.ProvaSummaryDto;
import com.studora.entity.Prova;
import com.studora.exception.ResourceNotFoundException;
import com.studora.mapper.ProvaMapper;
import com.studora.repository.ProvaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProvaService {

    private final ProvaRepository provaRepository;
    private final ProvaMapper provaMapper;

    @Transactional(readOnly = true)
    public List<ProvaSummaryDto> listarPorConcurso(Long concursoId) {
        return provaRepository.findByConcursoId(concursoId).stream()
                .map(provaMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProvaDetailDto detalharProva(Long concursoId, Long provaId) {
        Prova prova = provaRepository.findById(provaId)
                .filter(p -> p.getConcurso().getId().equals(concursoId))
                .orElseThrow(() -> new ResourceNotFoundException("Prova", "ID", provaId));
        return provaMapper.toDetailDto(prova);
    }
}
