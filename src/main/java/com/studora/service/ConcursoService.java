package com.studora.service;

import com.studora.dto.DificuldadeStatDto;
import com.studora.dto.MetricsLevel;
import com.studora.dto.StatSliceDto;
import com.studora.dto.concurso.ConcursoFilter;
import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.concurso.ConcursoCargoSummaryDto;

import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.entity.Banca;
import com.studora.entity.Cargo;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;

import com.studora.entity.Dificuldade;
import com.studora.entity.Instituicao;
import com.studora.entity.Resposta;
import com.studora.entity.Subtema;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.ConcursoMapper;
import com.studora.repository.*;
import com.studora.repository.specification.ConcursoSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ConcursoService {

    private final ConcursoRepository concursoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final BancaRepository bancaRepository;
    private final CargoRepository cargoRepository;
    private final ConcursoCargoRepository concursoCargoRepository;
    private final SubtemaRepository subtemaRepository;
    private final ConcursoMapper concursoMapper;
    private final jakarta.persistence.EntityManager entityManager;

    private final StatsAssembler statsAssembler;
    private final EstudoSubtemaRepository estudoSubtemaRepository;

    public ConcursoService(ConcursoRepository concursoRepository,
                           InstituicaoRepository instituicaoRepository,
                           BancaRepository bancaRepository,
                           CargoRepository cargoRepository,
                           ConcursoCargoRepository concursoCargoRepository,
                           SubtemaRepository subtemaRepository,
                           EstudoSubtemaRepository estudoSubtemaRepository,
                           ConcursoMapper concursoMapper,
                           StatsAssembler statsAssembler,
                           jakarta.persistence.EntityManager entityManager) {
        this.concursoRepository = concursoRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.bancaRepository = bancaRepository;
        this.cargoRepository = cargoRepository;
        this.concursoCargoRepository = concursoCargoRepository;
        this.subtemaRepository = subtemaRepository;
        this.estudoSubtemaRepository = estudoSubtemaRepository;
        this.concursoMapper = concursoMapper;
        this.statsAssembler = statsAssembler;
        this.entityManager = entityManager;
    }

    @Cacheable(value = "concurso-stats", key = "T(java.util.Objects).hash(#filter, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    @Transactional(readOnly = true)
    public Page<ConcursoSummaryDto> findAll(ConcursoFilter filter, Pageable pageable) {
        Specification<Concurso> spec = ConcursoSpecification.withFilter(filter);
        
        Page<Concurso> page = concursoRepository.findAll(spec, pageable);
        
        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> ids = page.getContent().stream().map(Concurso::getId).toList();
        List<Concurso> withDetails = concursoRepository.findAllByIdsWithDetails(ids);
        
        java.util.Map<Long, Concurso> detailsMap = withDetails.stream()
                .collect(java.util.stream.Collectors.toMap(Concurso::getId, c -> c));
        
        Page<ConcursoSummaryDto> result = page.map(c -> concursoMapper.toSummaryDto(detailsMap.getOrDefault(c.getId(), c)));
        return result;
    }

    @Transactional(readOnly = true)
    public ConcursoDetailDto getConcursoDetailById(Long id, MetricsLevel metrics) {
        Concurso concurso = concursoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", id));
        ConcursoDetailDto dto = concursoMapper.toDetailDto(concurso);

        if (metrics != null && dto.getCargos() != null) {
            for (ConcursoCargoSummaryDto cargoDto : dto.getCargos()) {
                List<com.studora.dto.concurso.ConcursoCargoSubtemaDto> topicos = cargoDto.getTopicos();
                if (topicos == null || topicos.isEmpty()) continue;

                List<Long> subtemaIds = topicos.stream()
                        .map(com.studora.dto.concurso.ConcursoCargoSubtemaDto::getId)
                        .collect(Collectors.toList());

                // Batch-fetch questoesConcursoCargo stats for all subtemas of this cargo
                Map<Long, StatSliceDto> ccStats = statsAssembler.buildBatchConcursoCargoStats(
                        cargoDto.getId(), subtemaIds, metrics);

                for (com.studora.dto.concurso.ConcursoCargoSubtemaDto topico : topicos) {
                    Long subId = topico.getId();

                    // totalEstudos is always present when metrics != null
                    topico.setTotalEstudos(estudoSubtemaRepository.countBySubtemaId(subId));

                    // questoesConcursoCargo: available for SUMMARY and FULL
                    topico.setQuestoesConcursoCargo(ccStats.get(subId));

                    // questaoStats: only for FULL
                    if (metrics == MetricsLevel.FULL) {
                        topico.setQuestaoStats(statsAssembler.buildStats(subId, "SUBTEMA", metrics));
                    }
                }
            }
        }

        return dto;
    }

    @CacheEvict(value = "concurso-stats", allEntries = true)
    public Long create(ConcursoCreateRequest request) {
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
        if (concurso.getEdital() != null && concurso.getEdital().isBlank()) {
            concurso.setEdital(null);
        }
        concurso.setInstituicao(instituicao);
        concurso.setBanca(banca);
        concurso.setDataProva(request.getDataProva());

        // Process Cargos
        List<Long> cargoIds = request.getCargos().stream().distinct().collect(Collectors.toList());
        for (Long cargoId : cargoIds) {
            Cargo cargo = cargoRepository.findById(cargoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", cargoId));
            ConcursoCargo cc = new ConcursoCargo();
            cc.setCargo(cargo);
            concurso.addConcursoCargo(cc);
        }

        // Process Provas (Nested Creation)
        if (request.getProvas() != null) {
            for (com.studora.dto.request.ProvaCreateRequest pReq : request.getProvas()) {
                com.studora.entity.Prova prova = new com.studora.entity.Prova();
                prova.setConcurso(concurso);
                prova.setNome(pReq.getNome());
                concurso.getProvas().add(prova);

                // Link Prova-Cargo
                if (pReq.getCargoIds() != null) {
                    Set<com.studora.entity.ConcursoCargo> cargos = concurso.getConcursoCargos().stream()
                            .filter(cc -> pReq.getCargoIds().contains(cc.getCargo().getId()))
                            .collect(Collectors.toSet());
                    prova.setCargos(cargos);
                }

                // Process Secoes
                if (pReq.getSecoes() != null) {
                    for (com.studora.dto.request.ProvaSecaoCreateRequest sReq : pReq.getSecoes()) {
                        com.studora.entity.ProvaSecao secao = new com.studora.entity.ProvaSecao();
                        secao.setProva(prova);
                        secao.setNome(sReq.getNome());
                        secao.setOrdem(sReq.getOrdem());
                        secao.setNumQuestoes(sReq.getNumQuestoes());
                        prova.getSecoes().add(secao);

                        // Link Subtemas
                        if (sReq.getSubtemaIds() != null) {
                            List<Subtema> subtemas = subtemaRepository.findAllById(sReq.getSubtemaIds());
                            secao.setSubtemas(new HashSet<>(subtemas));
                        }

                        // Process Pesos
                        if (sReq.getPesos() != null) {
                            for (com.studora.dto.request.ProvaSecaoPesoCreateRequest wReq : sReq.getPesos()) {
                                com.studora.entity.ProvaSecaoPeso peso = new com.studora.entity.ProvaSecaoPeso();
                                secao.addPeso(peso);
                                
                                if (wReq.getCargoId() != null) {
                                    final Long targetCargoId = wReq.getCargoId();
                                    ConcursoCargo targetCc = concurso.getConcursoCargos().stream()
                                            .filter(cc -> cc.getCargo().getId().equals(targetCargoId))
                                            .findFirst()
                                            .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "Cargo ID", targetCargoId));
                                    peso.setConcursoCargo(targetCc);
                                }
                                
                                peso.setPeso(wReq.getPeso());
                                peso.setNotaMinima(wReq.getNotaMinima());
                            }
                        }
                    }
                }
            }
        }

        return concursoRepository.save(concurso).getId();
    }

    @CacheEvict(value = "concurso-stats", allEntries = true)
    public void update(Long id, ConcursoUpdateRequest request) {
        log.info("Atualizando concurso ID: {}", id);
        
        Concurso concurso = concursoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", id));

        Long instId = request.getInstituicaoId() != null ? request.getInstituicaoId() : concurso.getInstituicao().getId();
        Long bancaId = request.getBancaId() != null ? request.getBancaId() : concurso.getBanca().getId();
        Integer ano = request.getAno() != null ? request.getAno() : concurso.getAno();
        Integer mes = request.getMes() != null ? request.getMes() : concurso.getMes();

        // Complex uniqueness check for update
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

        // Process Cargos
        List<Long> newCargoIds = request.getCargos().stream().distinct().collect(Collectors.toList());
        
        // Identify removals
        List<ConcursoCargo> toRemove = concurso.getConcursoCargos().stream()
                .filter(cc -> !newCargoIds.contains(cc.getCargo().getId()))
                .collect(Collectors.toList());

        for (ConcursoCargo cc : toRemove) {
            // Check if this cargo is used in any Prova of this concurso
            boolean isUsedInProva = concurso.getProvas().stream()
                    .anyMatch(p -> p.getCargos().contains(cc));
            
            if (isUsedInProva) {
                throw new ValidationException("O cargo " + cc.getCargo().getNome() + " não pode ser removido pois está associado a uma prova.");
            }

            concurso.getConcursoCargos().remove(cc);
            cc.setConcurso(null);
        }
        
        // Identify additions
        List<Long> existingCargoIds = concurso.getConcursoCargos().stream()
                .map(cc -> cc.getCargo().getId())
                .collect(Collectors.toList());
        
        List<Long> toAdd = newCargoIds.stream()
                .filter(idToAdd -> !existingCargoIds.contains(idToAdd))
                .collect(Collectors.toList());

        for (Long cargoId : toAdd) {
             Cargo cargo = cargoRepository.findById(cargoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", cargoId));
            ConcursoCargo cc = new ConcursoCargo();
            cc.setCargo(cargo);
            concurso.addConcursoCargo(cc);
        }

        concursoMapper.updateEntityFromDto(request, concurso);

        if (concurso.getEdital() != null && concurso.getEdital().isBlank()) {
            concurso.setEdital(null);
        }

        if (request.getDataProva() != null) {
            concurso.setDataProva(request.getDataProva());
        }

        if (request.getProvas() != null) {
            synchronizeProvas(concurso, request.getProvas());
        }

        concursoRepository.save(concurso);
    }

    private void synchronizeProvas(Concurso concurso, List<com.studora.dto.request.ProvaUpdateRequest> requests) {
        Map<Long, com.studora.entity.Prova> existingMap = concurso.getProvas().stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(com.studora.entity.Prova::getId, p -> p));

        Set<Long> idsToKeep = requests.stream()
                .map(com.studora.dto.request.ProvaUpdateRequest::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 1. Remove orphans
        concurso.getProvas().removeIf(p -> p.getId() != null && !idsToKeep.contains(p.getId()));

        // 2. Update or Create
        for (com.studora.dto.request.ProvaUpdateRequest pReq : requests) {
            com.studora.entity.Prova prova;
            if (pReq.getId() != null) {
                prova = existingMap.get(pReq.getId());
                if (prova == null) throw new ResourceNotFoundException("Prova", "ID", pReq.getId());
            } else {
                prova = new com.studora.entity.Prova();
                prova.setConcurso(concurso);
                concurso.getProvas().add(prova);
            }
            prova.setNome(pReq.getNome());

            // Sync Prova-Cargo (ManyToMany)
            if (pReq.getCargoIds() != null) {
                Set<com.studora.entity.ConcursoCargo> cargos = concurso.getConcursoCargos().stream()
                        .filter(cc -> pReq.getCargoIds().contains(cc.getCargo().getId()))
                        .collect(Collectors.toSet());
                prova.setCargos(cargos);
            }

            if (pReq.getSecoes() != null) {
                synchronizeSecoes(prova, pReq.getSecoes());
            }
        }
    }

    private void synchronizeSecoes(com.studora.entity.Prova prova, List<com.studora.dto.request.ProvaSecaoUpdateRequest> requests) {
        Map<Long, com.studora.entity.ProvaSecao> existingMap = prova.getSecoes().stream()
                .filter(ps -> ps.getId() != null)
                .collect(Collectors.toMap(com.studora.entity.ProvaSecao::getId, ps -> ps));

        Set<Long> idsToKeep = requests.stream()
                .map(com.studora.dto.request.ProvaSecaoUpdateRequest::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 1. Remove orphans
        prova.getSecoes().removeIf(ps -> ps.getId() != null && !idsToKeep.contains(ps.getId()));

        // 2. Temporary shift to avoid UNIQUE constraint violations during reordering
        for (com.studora.entity.ProvaSecao ps : prova.getSecoes()) {
            if (ps.getId() != null) {
                ps.setOrdem(-1 * ps.getId().intValue());
            }
        }
        entityManager.flush();

        // 3. Update or Create
        for (com.studora.dto.request.ProvaSecaoUpdateRequest sReq : requests) {
            com.studora.entity.ProvaSecao secao;
            if (sReq.getId() != null) {
                secao = existingMap.get(sReq.getId());
                if (secao == null) throw new ResourceNotFoundException("ProvaSecao", "ID", sReq.getId());
            } else {
                secao = new com.studora.entity.ProvaSecao();
                secao.setProva(prova);
                prova.getSecoes().add(secao);
            }
            secao.setNome(sReq.getNome());
            secao.setOrdem(sReq.getOrdem());
            secao.setNumQuestoes(sReq.getNumQuestoes());

            // Sync Subtemas
            if (sReq.getSubtemaIds() != null) {
                List<Subtema> subtemas = subtemaRepository.findAllById(sReq.getSubtemaIds());
                secao.setSubtemas(new HashSet<>(subtemas));
            }

            if (sReq.getPesos() != null) {
                synchronizePesos(secao, sReq.getPesos());
            }
        }

        // Validate: A subtema cannot be in more than one section per prova
        Map<Long, String> subtemaToSecaoMap = new java.util.HashMap<>();
        for (com.studora.entity.ProvaSecao s : prova.getSecoes()) {
            for (Subtema st : s.getSubtemas()) {
                if (subtemaToSecaoMap.containsKey(st.getId())) {
                    throw new com.studora.exception.ValidationException(
                        "O subtema '" + st.getNome() + "' está vinculado a múltiplas seções na mesma prova: '" + 
                        subtemaToSecaoMap.get(st.getId()) + "' e '" + s.getNome() + "'."
                    );
                }
                subtemaToSecaoMap.put(st.getId(), s.getNome());
            }
        }
    }

    private void synchronizePesos(com.studora.entity.ProvaSecao secao, List<com.studora.dto.request.ProvaSecaoPesoUpdateRequest> requests) {
        Map<Long, com.studora.entity.ProvaSecaoPeso> existingMap = secao.getPesos().stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(com.studora.entity.ProvaSecaoPeso::getId, p -> p));

        Set<Long> idsToKeep = requests.stream()
                .map(com.studora.dto.request.ProvaSecaoPesoUpdateRequest::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 1. Remove orphans
        secao.getPesos().removeIf(p -> p.getId() != null && !idsToKeep.contains(p.getId()));

        // 2. Update or Create
        for (com.studora.dto.request.ProvaSecaoPesoUpdateRequest wReq : requests) {
            com.studora.entity.ProvaSecaoPeso peso;
            if (wReq.getId() != null) {
                peso = existingMap.get(wReq.getId());
                if (peso == null) throw new ResourceNotFoundException("ProvaSecaoPeso", "ID", wReq.getId());
            } else {
                peso = new com.studora.entity.ProvaSecaoPeso();
                secao.addPeso(peso);
            }
            
            if (wReq.getCargoId() != null) {
                final Long targetCargoId = wReq.getCargoId();
                ConcursoCargo targetCc = secao.getProva().getConcurso().getConcursoCargos().stream()
                        .filter(cc -> cc.getCargo().getId().equals(targetCargoId))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "Cargo ID", targetCargoId));
                peso.setConcursoCargo(targetCc);
            }
            
            peso.setPeso(wReq.getPeso());
            peso.setNotaMinima(wReq.getNotaMinima());
        }
    }

    @CacheEvict(value = "concurso-stats", allEntries = true)
    public void delete(Long id) {
        log.info("Excluindo concurso ID: {}", id);
        if (!concursoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Concurso", "ID", id);
        }
        concursoRepository.deleteById(id);
    }

    @CacheEvict(value = "concurso-stats", allEntries = true)
    public void toggleInscricao(Long concursoCargoId) {
        log.info("Togglings inscrição para ConcursoCargo ID: {}", concursoCargoId);
        ConcursoCargo cc = concursoCargoRepository.findById(concursoCargoId)
                .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "ID", concursoCargoId));

        boolean newStatus = !cc.isInscrito();

        if (newStatus && concursoCargoRepository.existsByConcursoIdAndInscritoTrue(cc.getConcurso().getId())) {
            throw new ValidationException("Você já está inscrito em outro cargo para este concurso. Desinscreva-se primeiro.");
        }

        cc.setInscrito(newStatus);
        concursoCargoRepository.save(cc);
    }

    @CacheEvict(value = "concurso-stats", allEntries = true)
    public void toggleFinalizado(Long id) {
        log.info("Toggling status finalizado para Concurso ID: {}", id);
        Concurso concurso = concursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", id));
        
        concurso.setFinalizado(!concurso.isFinalizado());
        concursoRepository.save(concurso);
    }

    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).longValue()));
    }

    private Map<Long, LocalDateTime> toDateMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> ((Number) row[0]).longValue(),
                row -> parseDate(row[1])));
    }

    private LocalDateTime parseDate(Object val) {
        if (val instanceof LocalDateTime) return (LocalDateTime) val;
        if (val instanceof String) return LocalDateTime.parse((String) val, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return null;
    }
}