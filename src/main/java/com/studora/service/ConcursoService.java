package com.studora.service;

import com.studora.dto.ConcursoCargoDto;
import com.studora.dto.ConcursoDto;
import com.studora.entity.Banca;
import com.studora.entity.Cargo;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Instituicao;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.ConcursoCargoMapper;
import com.studora.mapper.ConcursoMapper;
import com.studora.repository.BancaRepository;
import com.studora.repository.CargoRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.InstituicaoRepository;
import com.studora.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConcursoService {

    private final ConcursoRepository concursoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final BancaRepository bancaRepository;
    private final CargoRepository cargoRepository;
    private final ConcursoCargoRepository concursoCargoRepository;
    private final com.studora.repository.QuestaoCargoRepository questaoCargoRepository;
    private final ConcursoMapper concursoMapper;
    private final ConcursoCargoMapper concursoCargoMapper;

    public Page<ConcursoDto> findAll(Pageable pageable) {
        return concursoRepository.findAll(pageable)
                .map(concursoMapper::toDto);
    }

    public ConcursoDto findById(Long id) {
        Concurso concurso = concursoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", id));
        return concursoMapper.toDto(concurso);
    }

    public ConcursoDto save(ConcursoDto concursoDto) {
        if (concursoDto == null) {
            throw new IllegalArgumentException("ConcursoDto não pode ser nulo.");
        }

        // Check for duplicate combination (excluding the record being updated)
        if (concursoDto.getId() == null) {
            // New record: simple exists check
            if (concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(
                    concursoDto.getInstituicaoId(), concursoDto.getBancaId(), concursoDto.getAno(), concursoDto.getMes())) {
                throw new com.studora.exception.ConflictException("Já existe um concurso cadastrado para esta instituição, banca, ano e mês.");
            }
        } else {
            // Update: check if changes create a conflict with ANOTHER record
            Concurso existing = concursoRepository.findById(concursoDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", concursoDto.getId()));
            
            // If any of the identifying fields changed, check if the new combination already exists elsewhere
            boolean changed = !existing.getInstituicao().getId().equals(concursoDto.getInstituicaoId()) ||
                              !existing.getBanca().getId().equals(concursoDto.getBancaId()) ||
                              !existing.getAno().equals(concursoDto.getAno()) ||
                              !existing.getMes().equals(concursoDto.getMes());
            
            if (changed && concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(
                    concursoDto.getInstituicaoId(), concursoDto.getBancaId(), concursoDto.getAno(), concursoDto.getMes())) {
                throw new com.studora.exception.ConflictException("As alterações entram em conflito com outro concurso já cadastrado para esta mesma instituição, banca, ano e mês.");
            }
        }

        Concurso concurso;
        if (concursoDto.getId() != null) {
            // Update existing concurso
            concurso = concursoRepository.findById(concursoDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", concursoDto.getId()));

            // Update the existing entity with new values
            Instituicao instituicao = instituicaoRepository.findById(concursoDto.getInstituicaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", concursoDto.getInstituicaoId()));
            Banca banca = bancaRepository.findById(concursoDto.getBancaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", concursoDto.getBancaId()));

            concursoMapper.updateEntityFromDto(concursoDto, concurso);
            concurso.setInstituicao(instituicao);
            concurso.setBanca(banca);
        } else {
            // Create new concurso
            Instituicao instituicao = instituicaoRepository.findById(concursoDto.getInstituicaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", concursoDto.getInstituicaoId()));

            Banca banca = bancaRepository.findById(concursoDto.getBancaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", concursoDto.getBancaId()));

            concurso = concursoMapper.toEntity(concursoDto);
            concurso.setInstituicao(instituicao);
            concurso.setBanca(banca);
        }

        Concurso savedConcurso = concursoRepository.save(concurso);

        return concursoMapper.toDto(savedConcurso);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!concursoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Concurso", "ID", id);
        }

        concursoRepository.deleteById(id);
        concursoRepository.flush();
    }

    // Methods for managing cargo associations
    public List<ConcursoCargoDto> getCargosByConcursoId(Long concursoId) {
        List<ConcursoCargo> concursoCargos = concursoCargoRepository.findByConcursoId(concursoId);
        return concursoCargos.stream()
                .map(concursoCargoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConcursoCargoDto addCargoToConcurso(ConcursoCargoDto concursoCargoDto) {
        // Check if the association already exists
        List<ConcursoCargo> existingAssociations = concursoCargoRepository
                .findByConcursoIdAndCargoId(concursoCargoDto.getConcursoId(), concursoCargoDto.getCargoId());

        if (!existingAssociations.isEmpty()) {
            throw new ValidationException("Cargo já associado ao concurso");
        }

        Concurso concurso = concursoRepository.findById(concursoCargoDto.getConcursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", concursoCargoDto.getConcursoId()));

        Cargo cargo = cargoRepository.findById(concursoCargoDto.getCargoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", concursoCargoDto.getCargoId()));

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concurso.addConcursoCargo(concursoCargo);
        concursoCargo.setCargo(cargo);

        ConcursoCargo savedConcursoCargo = concursoCargoRepository.save(concursoCargo);
        return concursoCargoMapper.toDto(savedConcursoCargo);
    }

    @Transactional
    public void removeCargoFromConcurso(Long concursoId, Long cargoId) {
        List<ConcursoCargo> concursoCargos = concursoCargoRepository
                .findByConcursoIdAndCargoId(concursoId, cargoId);

        if (concursoCargos.isEmpty()) {
            throw new ResourceNotFoundException("Associação entre concurso e cargo não encontrada");
        }

        // Validate if any question depends solely on this ConcursoCargo
        for (ConcursoCargo cc : concursoCargos) {
            List<com.studora.entity.QuestaoCargo> questaoCargos = questaoCargoRepository.findByConcursoCargoId(cc.getId());
            for (com.studora.entity.QuestaoCargo qc : questaoCargos) {
                long count = questaoCargoRepository.countByQuestaoId(qc.getQuestao().getId());
                if (count <= 1) {
                    throw new ValidationException("Não é possível remover o cargo do concurso pois ele é a única associação para a questão ID: " + qc.getQuestao().getId());
                }
            }
        }

        // Check if removing this association would leave the concurso with no cargo associations
        List<ConcursoCargo> currentAssociations = concursoCargoRepository.findByConcursoId(concursoId);
        if (currentAssociations.size() <= concursoCargos.size()) {
             throw new ValidationException("Um concurso deve estar associado a pelo menos um cargo");
        }

        // Delete the association
        concursoCargoRepository.deleteAll(concursoCargos);
    }
}
