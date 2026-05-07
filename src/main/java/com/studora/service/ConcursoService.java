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
    private final SecaoCargoRepository secaoCargoRepository;
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
                           SecaoCargoRepository secaoCargoRepository,
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
        this.secaoCargoRepository = secaoCargoRepository;
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
                if (cargoDto.getTopicos() == null || cargoDto.getTopicos().isEmpty()) continue;

                // Flatten all subtemas from all sections (topicos)
                List<com.studora.dto.concurso.ConcursoCargoSubtemaDto> allSubtemas = cargoDto.getTopicos().stream()
                        .flatMap(secao -> secao.getAssuntos().stream())
                        .collect(Collectors.toList());

                List<Long> subtemaIds = allSubtemas.stream()
                        .map(com.studora.dto.concurso.ConcursoCargoSubtemaDto::getId)
                        .collect(Collectors.toList());

                // Batch-fetch questoesConcursoCargo stats for all subtemas of this cargo
                Map<Long, StatSliceDto> ccStats = statsAssembler.buildBatchConcursoCargoStats(
                        cargoDto.getId(), subtemaIds, metrics);

                for (com.studora.dto.concurso.ConcursoCargoSubtemaDto topico : allSubtemas) {
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

                // Link Prova-Cargo (1:1)
                final Long targetCargoId = pReq.getCargoId();
if (targetCargoId != null) {
                ConcursoCargo targetCc = concurso.getConcursoCargos().stream()
                        .filter(cc -> cc.getCargo().getId().equals(targetCargoId))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "Cargo ID", targetCargoId));
prova.setConcursoCargo(targetCc);
                entityManager.flush();
            }

            if (pReq.getSecoes() != null) {
                    for (com.studora.dto.request.ProvaSecaoCreateRequest sReq : pReq.getSecoes()) {
                        com.studora.entity.ProvaSecao secao = new com.studora.entity.ProvaSecao();
                        secao.setProva(prova);
                        secao.setNome(sReq.getNome());
                        secao.setOrdem(sReq.getOrdem());
                        secao.setNumQuestoes(sReq.getNumQuestoes() != null && sReq.getNumQuestoes() > 0 ? sReq.getNumQuestoes() : 1);
                        prova.getSecoes().add(secao);

                        // Find or Create SecaoCargo (Inheritance Definition)
                        ConcursoCargo cc = prova.getConcursoCargo();
                        if (cc != null) {
                            final String sName = sReq.getNome();
                            com.studora.entity.SecaoCargo sc = cc.getSecaoCargos().stream()
                                    .filter(existing -> existing.getNome().equalsIgnoreCase(sName))
                                    .findFirst()
                                    .orElseGet(() -> {
                                        com.studora.entity.SecaoCargo newSc = new com.studora.entity.SecaoCargo();
                                        newSc.setConcursoCargo(cc);
                                        newSc.setNome(sName);
                                        cc.getSecaoCargos().add(newSc);
                                        return newSc;
                                    });
                            
                            // Update definition fields (Inherit these from the request)
                            // If multiple provas define the same section differently, the last one wins in this simple logic.
                            sc.setPeso(sReq.getPeso() != null ? sReq.getPeso() : 1.0);
                            sc.setNotaMinima(sReq.getNotaMinima() != null ? sReq.getNotaMinima() : 0.0);
                            sc.setOrdem(sReq.getOrdem());
                            sc.setNumQuestoes(sReq.getNumQuestoes() != null && sReq.getNumQuestoes() > 0 ? sReq.getNumQuestoes() : 1);
                            
                            if (sReq.getSubtemaIds() != null) {
                                List<Subtema> subtemas = subtemaRepository.findAllById(sReq.getSubtemaIds());
                                sc.setSubtemas(new HashSet<>(subtemas));
                            }
                            secao.setSecaoCargo(sc);
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
                    .anyMatch(p -> p.getConcursoCargo() != null && p.getConcursoCargo().equals(cc));
            
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

            // Sync Prova-Cargo (1:1)
            final Long targetCargoId = pReq.getCargoId();
if (targetCargoId != null) {
                ConcursoCargo targetCc = concurso.getConcursoCargos().stream()
                        .filter(cc -> cc.getCargo().getId().equals(targetCargoId))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "Cargo ID", targetCargoId));
                prova.setConcursoCargo(targetCc);
                if (pReq.getId() == null) {
                    entityManager.flush();
                }
            }

            if (pReq.getSecoes() != null) {
                synchronizeSecoes(prova, pReq.getSecoes());
            }
        }
    }

    private void synchronizeSecoes(com.studora.entity.Prova prova, List<com.studora.dto.request.ProvaSecaoUpdateRequest> requests) {
        log.info("synchronizeSecoes called: prova.id={}, prova.concursoCargo={}, requests={}", prova.getId(), prova.getConcursoCargo(), requests);
        if (requests == null) {
            log.warn("synchronizeSecoes: NULL requests for prova.id={}", prova.getId());
            return;
        }
        if (requests.isEmpty()) {
            log.warn("synchronizeSecoes: EMPTY list for prova.id={}", prova.getId());
            return;
        }
        log.info("synchronizeSecoes: processing {} secoes for prova.id={}", requests.size(), prova.getId());
        for (int i = 0; i < requests.size(); i++) {
            log.info("  secao[{}]: id={}, nome={}", i, requests.get(i).getId(), requests.get(i).getNome());
        }
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
            ConcursoCargo cc = prova.getConcursoCargo();
            log.info("ProvaSecao sync: prova.id={}, prova.concursoCargo={}, secao.id={}, sReq.nome={}", prova.getId(), cc != null ? cc.getId() : "null", sReq.getId(), sReq.getNome());

            // First, find or create SecaoCargo (before adding ProvaSecao to collection)
            com.studora.entity.SecaoCargo sc = null;
            if (cc != null) {
                final String sName = sReq.getNome();
                log.info("Looking up SecaoCargo: cc.getId()={}, sName={}", cc.getId(), sName);
                sc = secaoCargoRepository
                        .findByConcursoCargoIdAndNomeIgnoreCase(cc.getId(), sName)
                        .orElseGet(() -> {
                            log.info("Creating new SecaoCargo for cc.getId()={}, sName={}", cc.getId(), sName);
                            com.studora.entity.SecaoCargo newSc = new com.studora.entity.SecaoCargo();
                            newSc.setConcursoCargo(cc);
                            newSc.setNome(sName);
                            return secaoCargoRepository.save(newSc);
                        });
                log.info("Found/created SecaoCargo sc.id={} for nome={}", sc.getId(), sName);

                // Update definition fields
                sc.setPeso(sReq.getPeso() != null ? sReq.getPeso() : 1.0);
                sc.setNotaMinima(sReq.getNotaMinima() != null ? sReq.getNotaMinima() : 0.0);
                sc.setOrdem(sReq.getOrdem());
                sc.setNumQuestoes(sReq.getNumQuestoes() != null && sReq.getNumQuestoes() > 0 ? sReq.getNumQuestoes() : 1);
                secaoCargoRepository.save(sc);

                if (sReq.getSubtemaIds() != null) {
                    List<Subtema> subtemas = subtemaRepository.findAllById(sReq.getSubtemaIds());
                    sc.setSubtemas(new HashSet<>(subtemas));
                }
            } else {
                log.warn("cc is null! Cannot set secaoCargo for secao.id={}", sReq.getId());
            }

// Now create or update ProvaSecao
            if (sReq.getId() != null) {
                secao = existingMap.get(sReq.getId());
                if (secao == null) throw new ResourceNotFoundException("ProvaSecao", "ID", sReq.getId());
                secao.setNome(sReq.getNome());
                secao.setOrdem(sReq.getOrdem());
                secao.setNumQuestoes(sReq.getNumQuestoes() != null && sReq.getNumQuestoes() > 0 ? sReq.getNumQuestoes() : 1);
                if (sc != null) {
                    secao.setSecaoCargo(sc);
                }
            } else {
                secao = new com.studora.entity.ProvaSecao();
                secao.setProva(prova);
                secao.setNome(sReq.getNome());
                secao.setOrdem(sReq.getOrdem());
                secao.setNumQuestoes(sReq.getNumQuestoes() != null && sReq.getNumQuestoes() > 0 ? sReq.getNumQuestoes() : 1);
                if (sc != null) {
                    secao.setSecaoCargo(sc);
                }
                prova.getSecoes().add(secao);
                entityManager.flush();
            }
            if (sc != null) {
                log.info("Set secaoCargo.sc.id={} for secao.id={}", sc.getId(), secao.getId());
            }
        }

        // Validate: A subtema cannot be in more than one section definition per cargo
        // (This check is now implicitly across all provas of the cargo, but here we check the cargo context)
        ConcursoCargo cc = prova.getConcursoCargo();
        if (cc != null) {
            Map<Long, String> subtemaToSecaoDefMap = new java.util.HashMap<>();
            List<com.studora.entity.SecaoCargo> secoesCargos = secaoCargoRepository.findAllByConcursoCargoId(cc.getId());
            for (com.studora.entity.SecaoCargo sc : secoesCargos) {
                for (Subtema st : sc.getSubtemas()) {
                    if (subtemaToSecaoDefMap.containsKey(st.getId())) {
                        throw new com.studora.exception.ValidationException(
                            "O subtema '" + st.getNome() + "' está vinculado a múltiplas seções temáticas para o cargo '" + 
                            cc.getCargo().getNome() + "': '" + subtemaToSecaoDefMap.get(st.getId()) + "' e '" + sc.getNome() + "'."
                        );
                    }
                    subtemaToSecaoDefMap.put(st.getId(), sc.getNome());
                }
            }
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