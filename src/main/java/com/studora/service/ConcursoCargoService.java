package com.studora.service;

import com.studora.dto.ConcursoCargoDto;
import com.studora.entity.Cargo;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.repository.CargoRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConcursoCargoService {

    @Autowired
    private ConcursoCargoRepository concursoCargoRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private CargoRepository cargoRepository;

    public List<ConcursoCargoDto> findAll() {
        return concursoCargoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ConcursoCargoDto findById(Long id) {
        return concursoCargoRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    public ConcursoCargoDto save(ConcursoCargoDto concursoCargoDto) {
        ConcursoCargo concursoCargo = convertToEntity(concursoCargoDto);
        return convertToDto(concursoCargoRepository.save(concursoCargo));
    }

    public void deleteById(Long id) {
        concursoCargoRepository.deleteById(id);
    }

    private ConcursoCargoDto convertToDto(ConcursoCargo concursoCargo) {
        ConcursoCargoDto concursoCargoDto = new ConcursoCargoDto();
        concursoCargoDto.setId(concursoCargo.getId());
        concursoCargoDto.setConcursoId(concursoCargo.getConcurso().getId());
        concursoCargoDto.setCargoId(concursoCargo.getCargo().getId());
        return concursoCargoDto;
    }

    private ConcursoCargo convertToEntity(ConcursoCargoDto concursoCargoDto) {
        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(concursoCargoDto.getId());

        if (concursoCargoDto.getConcursoId() != null) {
            Concurso concurso = concursoRepository.findById(concursoCargoDto.getConcursoId())
                    .orElseThrow(() -> new RuntimeException("Concurso not found"));
            concursoCargo.setConcurso(concurso);
        }

        if (concursoCargoDto.getCargoId() != null) {
            Cargo cargo = cargoRepository.findById(concursoCargoDto.getCargoId())
                    .orElseThrow(() -> new RuntimeException("Cargo not found"));
            concursoCargo.setCargo(cargo);
        }

        return concursoCargo;
    }
}
