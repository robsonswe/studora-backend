package com.studora.service;

import com.studora.dto.concurso.ConcursoCargoDto;
import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.request.ConcursoCargoCreateRequest;
import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.entity.Banca;
import com.studora.entity.Cargo;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Instituicao;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.ConcursoCargoMapper;
import com.studora.mapper.ConcursoMapper;
import com.studora.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConcursoService {

    private final ConcursoRepository concursoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final BancaRepository bancaRepository;
    private final CargoRepository cargoRepository;
    private final ConcursoCargoRepository concursoCargoRepository;
    private final QuestaoCargoRepository questaoCargoRepository;
    private final ConcursoMapper concursoMapper;
    private final ConcursoCargoMapper concursoCargoMapper;

    @Transactional(readOnly = true)
    public Page<ConcursoSummaryDto> findAll(Pageable pageable) {
        return concursoRepository.findAll(pageable)
                .map(concursoMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public ConcursoDetailDto getConcursoDetailById(Long id) {
        Concurso concurso = concursoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", id));
        return concursoMapper.toDetailDto(concurso);
    }

    public ConcursoDetailDto create(ConcursoCreateRequest request) {
        log.info("Criando novo concurso: Inst {}, Banca {}, Ano {}", 
                request.getInstituicaoId(), request.getBancaId(), request.getAno());
        
        if (concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(
                request.getInstituicaoId(), request.getBancaId(), request.getAno(), request.getMes())) {
            throw new com.studora.exception.ConflictException("Já existe um concurso cadastrado para esta instituição, banca, ano e mês.");
        }

        Instituicao instituicao = instituicaoRepository.findById(request.getInstituicaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", request.getInstituicaoId()));
        
        Banca banca = bancaRepository.findById(request.getBancaId())
                .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", request.getBancaId()));

        Concurso concurso = concursoMapper.toEntity(request);
        concurso.setInstituicao(instituicao);
        concurso.setBanca(banca);

        return concursoMapper.toDetailDto(concursoRepository.save(concurso));
    }

    public ConcursoDetailDto update(Long id, ConcursoUpdateRequest request) {
        log.info("Atualizando concurso ID: {}", id);
        
        Concurso concurso = concursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", id));

        Long instId = request.getInstituicaoId() != null ? request.getInstituicaoId() : concurso.getInstituicao().getId();
        Long bancaId = request.getBancaId() != null ? request.getBancaId() : concurso.getBanca().getId();
        Integer ano = request.getAno() != null ? request.getAno() : concurso.getAno();
        Integer mes = request.getMes() != null ? request.getMes() : concurso.getMes();

        // Complex uniqueness check for update
        concursoRepository.findByIdWithDetails(id); // Ensure loaded
        if (! (instId.equals(concurso.getInstituicao().getId()) && 
               bancaId.equals(concurso.getBanca().getId()) && 
               ano.equals(concurso.getAno()) && 
               mes.equals(concurso.getMes()))) {
            
            if (concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(instId, bancaId, ano, mes)) {
                throw new com.studora.exception.ConflictException("Já existe um concurso cadastrado para esta instituição, banca, ano e mês.");
            }
        }

        if (request.getInstituicaoId() != null) {
            Instituicao inst = instituicaoRepository.findById(request.getInstituicaoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instituição", "ID", request.getInstituicaoId()));
            concurso.setInstituicao(inst);
        }

        if (request.getBancaId() != null) {
            Banca banca = bancaRepository.findById(request.getBancaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Banca", "ID", request.getBancaId()));
            concurso.setBanca(banca);
        }

        concursoMapper.updateEntityFromDto(request, concurso);
        return concursoMapper.toDetailDto(concursoRepository.save(concurso));
    }

    public void delete(Long id) {
        log.info("Excluindo concurso ID: {}", id);
        if (!concursoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Concurso", "ID", id);
        }
        concursoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ConcursoCargoDto> getCargosByConcursoId(Long concursoId) {
        return concursoCargoRepository.findByConcursoId(concursoId).stream()
                .map(concursoCargoMapper::toDto)
                .collect(Collectors.toList());
    }

    public ConcursoCargoDto addCargoToConcurso(Long concursoId, ConcursoCargoCreateRequest request) {
        if (concursoCargoRepository.existsByConcursoIdAndCargoId(concursoId, request.getCargoId())) {
            throw new com.studora.exception.ConflictException("Este cargo já está associado a este concurso.");
        }

        Concurso concurso = concursoRepository.findById(concursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", concursoId));
        
        Cargo cargo = cargoRepository.findById(request.getCargoId())
                .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", request.getCargoId()));

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo);

        return concursoCargoMapper.toDto(concursoCargoRepository.save(cc));
    }

    public void removeCargoFromConcurso(Long concursoId, Long cargoId) {
        List<ConcursoCargo> ccs = concursoCargoRepository.findByConcursoIdAndCargoId(concursoId, cargoId);
        if (ccs.isEmpty()) {
            throw new ResourceNotFoundException("Associação Concurso-Cargo", "IDs", concursoId + "-" + cargoId);
        }
        ConcursoCargo cc = ccs.get(0);
        
        if (!questaoCargoRepository.findByConcursoCargoId(cc.getId()).isEmpty()) {
            throw new ValidationException("Não é possível remover o cargo pois existem questões associadas a ele neste concurso.");
        }

        if (concursoCargoRepository.countByConcursoId(concursoId) <= 1) {
            throw new ValidationException("Um concurso deve estar associado a pelo menos um cargo");
        }

        concursoCargoRepository.delete(cc);
    }
}
