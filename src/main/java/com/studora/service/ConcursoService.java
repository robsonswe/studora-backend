package com.studora.service;

import com.studora.dto.ConcursoCargoDto;
import com.studora.dto.ConcursoDto;
import com.studora.entity.Banca;
import com.studora.entity.Cargo;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Instituicao;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.BancaRepository;
import com.studora.repository.CargoRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.InstituicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private ConcursoCargoRepository concursoCargoRepository;

    public List<ConcursoDto> findAll() {
        return concursoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ConcursoDto findById(Long id) {
        Concurso concurso = concursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", id));
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
                    .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", concursoDto.getId()));

            // Update the existing entity with new values
            Instituicao instituicao = instituicaoRepository.findById(concursoDto.getInstituicaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", concursoDto.getInstituicaoId()));
            Banca banca = bancaRepository.findById(concursoDto.getBancaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", concursoDto.getBancaId()));

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

    @Transactional
    public void deleteById(Long id) {
        if (!concursoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Concurso", "ID", id);
        }

        // First, remove all cargo associations for this concurso
        List<ConcursoCargo> concursoCargos = concursoCargoRepository.findByConcursoId(id);
        concursoCargoRepository.deleteAll(concursoCargos);

        concursoRepository.deleteById(id);
    }

    // Methods for managing cargo associations
    public List<ConcursoCargoDto> getCargosByConcursoId(Long concursoId) {
        List<ConcursoCargo> concursoCargos = concursoCargoRepository.findByConcursoId(concursoId);
        return concursoCargos.stream()
                .map(this::convertConcursoCargoToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConcursoCargoDto addCargoToConcurso(ConcursoCargoDto concursoCargoDto) {
        // Check if the association already exists
        List<ConcursoCargo> existingAssociations = concursoCargoRepository
                .findByConcursoIdAndCargoId(concursoCargoDto.getConcursoId(), concursoCargoDto.getCargoId());

        if (!existingAssociations.isEmpty()) {
            throw new RuntimeException("Cargo já associado ao concurso");
        }

        Concurso concurso = concursoRepository.findById(concursoCargoDto.getConcursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", concursoCargoDto.getConcursoId()));

        Cargo cargo = cargoRepository.findById(concursoCargoDto.getCargoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", concursoCargoDto.getCargoId()));

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setConcurso(concurso);
        concursoCargo.setCargo(cargo);

        ConcursoCargo savedConcursoCargo = concursoCargoRepository.save(concursoCargo);
        return convertConcursoCargoToDto(savedConcursoCargo);
    }

    @Transactional
    public void removeCargoFromConcurso(Long concursoId, Long cargoId) {
        List<ConcursoCargo> concursoCargos = concursoCargoRepository
                .findByConcursoIdAndCargoId(concursoId, cargoId);

        if (concursoCargos.isEmpty()) {
            throw new ResourceNotFoundException("Associação entre concurso e cargo não encontrada");
        }

        // Check if removing this association would leave the concurso with no cargo associations
        List<ConcursoCargo> currentAssociations = concursoCargoRepository.findByConcursoId(concursoId);
        if (currentAssociations.size() <= 1) {
            throw new RuntimeException("Um concurso deve estar associado a pelo menos um cargo");
        }

        // Delete the association
        concursoCargoRepository.deleteAll(concursoCargos);
    }

    private ConcursoDto convertToDto(Concurso concurso) {
        ConcursoDto dto = new ConcursoDto();
        dto.setId(concurso.getId());
        dto.setInstituicaoId(concurso.getInstituicao().getId());
        dto.setBancaId(concurso.getBanca().getId());
        dto.setAno(concurso.getAno());
        return dto;
    }

    private ConcursoCargoDto convertConcursoCargoToDto(ConcursoCargo concursoCargo) {
        ConcursoCargoDto dto = new ConcursoCargoDto();
        dto.setId(concursoCargo.getId());
        dto.setConcursoId(concursoCargo.getConcurso().getId());
        dto.setCargoId(concursoCargo.getCargo().getId());
        return dto;
    }

    private Concurso convertToEntity(ConcursoDto dto) {
        Instituicao instituicao = instituicaoRepository.findById(dto.getInstituicaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", dto.getInstituicaoId()));

        Banca banca = bancaRepository.findById(dto.getBancaId())
                .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", dto.getBancaId()));

        Concurso concurso = new Concurso();
        concurso.setId(dto.getId());
        concurso.setInstituicao(instituicao);
        concurso.setBanca(banca);
        concurso.setAno(dto.getAno());

        return concurso;
    }
}
