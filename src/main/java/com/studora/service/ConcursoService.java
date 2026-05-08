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
import com.studora.entity.SecaoDisciplina;
import com.studora.dto.request.SecaoDisciplinaRequest;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
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
    private final SecaoDisciplinaRepository secaoDisciplinaRepository;
    private final EstudoSubtemaRepository estudoSubtemaRepository;
    private final QuestaoRepository questaoRepository;
    private final StatsAssembler statsAssembler;
    private final ConcursoMapper concursoMapper;
    private final jakarta.persistence.EntityManager entityManager;

    public ConcursoService(ConcursoRepository concursoRepository,
            InstituicaoRepository instituicaoRepository,
            BancaRepository bancaRepository,
            CargoRepository cargoRepository,
            ConcursoCargoRepository concursoCargoRepository,
            SecaoCargoRepository secaoCargoRepository,
            SubtemaRepository subtemaRepository,
            EstudoSubtemaRepository estudoSubtemaRepository,
            QuestaoRepository questaoRepository,
            ConcursoMapper concursoMapper,
            StatsAssembler statsAssembler,
            jakarta.persistence.EntityManager entityManager,
            SecaoDisciplinaRepository secaoDisciplinaRepository) {
        this.concursoRepository = concursoRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.bancaRepository = bancaRepository;
        this.cargoRepository = cargoRepository;
        this.concursoCargoRepository = concursoCargoRepository;
        this.secaoCargoRepository = secaoCargoRepository;
        this.subtemaRepository = subtemaRepository;
        this.estudoSubtemaRepository = estudoSubtemaRepository;
        this.questaoRepository = questaoRepository;
        this.concursoMapper = concursoMapper;
        this.statsAssembler = statsAssembler;
        this.entityManager = entityManager;
        this.secaoDisciplinaRepository = secaoDisciplinaRepository;
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

        Page<ConcursoSummaryDto> result = page
                .map(c -> concursoMapper.toSummaryDto(detailsMap.getOrDefault(c.getId(), c)));
        return result;
    }

    @Transactional(readOnly = true)
    public ConcursoDetailDto getConcursoDetailById(Long id, MetricsLevel metrics) {
        Concurso concurso = concursoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", id));
        ConcursoDetailDto dto = concursoMapper.toDetailDto(concurso);

        if (metrics != null && dto.getCargos() != null) {
            for (ConcursoCargoSummaryDto cargoDto : dto.getCargos()) {
                if (cargoDto.getTopicos() == null || cargoDto.getTopicos().isEmpty())
                    continue;

                // Flatten all subtemas from all sections -> all disciplines
                List<com.studora.dto.concurso.ConcursoCargoSubtemaDto> allSubtemas = cargoDto.getTopicos().stream()
                        .flatMap(secao -> secao.getDisciplinas().stream())
                        .flatMap(disciplina -> disciplina.getAssuntos().stream())
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

                // Aggregate stats for Disciplines and Sections
                for (com.studora.dto.concurso.ConcursoSecaoDto secao : cargoDto.getTopicos()) {
                    List<com.studora.dto.StatSliceDto> secaoSlices = new ArrayList<>();
                    long secaoEstudos = 0;

                    for (com.studora.dto.concurso.ConcursoSecaoDisciplinaDto disc : secao.getDisciplinas()) {
                        List<com.studora.dto.StatSliceDto> discSlices = disc.getAssuntos().stream()
                                .map(com.studora.dto.concurso.ConcursoCargoSubtemaDto::getQuestoesConcursoCargo)
                                .filter(Objects::nonNull)
                                .toList();

                        disc.setQuestoesConcursoCargo(aggregateStatSlices(discSlices));
                        disc.setTotalEstudos(disc.getAssuntos().stream()
                                .mapToLong(a -> a.getTotalEstudos() != null ? a.getTotalEstudos() : 0L)
                                .sum());

                        if (disc.getQuestoesConcursoCargo() != null) {
                            secaoSlices.add(disc.getQuestoesConcursoCargo());
                        }
                        secaoEstudos += disc.getTotalEstudos();
                    }

                    secao.setQuestoesConcursoCargo(aggregateStatSlices(secaoSlices));
                    secao.setTotalEstudos(secaoEstudos);
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
            throw new com.studora.exception.ConflictException(
                    "Já existe um concurso cadastrado para esta instituição, banca, ano e mês.");
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
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("ConcursoCargo", "Cargo ID", targetCargoId));
                    prova.setConcursoCargo(targetCc);

                }

                if (pReq.getSecoes() != null) {
                    for (com.studora.dto.request.ProvaSecaoCreateRequest sReq : pReq.getSecoes()) {
                        com.studora.entity.ProvaSecao secao = new com.studora.entity.ProvaSecao();
                        secao.setProva(prova);
                        secao.setNome(sReq.getNome());
                        int currentOrdem = sReq.getOrdem() != null ? sReq.getOrdem() : (prova.getSecoes().size() + 1);
                        secao.setOrdem(currentOrdem);
                        secao.setNumQuestoes(
                                sReq.getNumQuestoes() != null && sReq.getNumQuestoes() > 0 ? sReq.getNumQuestoes() : 1);
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

                            applyMetricsLogic(sc, secao, sReq);
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

        Long instId = request.getInstituicaoId() != null ? request.getInstituicaoId()
                : concurso.getInstituicao().getId();
        Long bancaId = request.getBancaId() != null ? request.getBancaId() : concurso.getBanca().getId();
        Integer ano = request.getAno() != null ? request.getAno() : concurso.getAno();
        Integer mes = request.getMes() != null ? request.getMes() : concurso.getMes();

        // Complex uniqueness check for update
        if (!(instId.equals(concurso.getInstituicao().getId()) &&
                bancaId.equals(concurso.getBanca().getId()) &&
                ano.equals(concurso.getAno()) &&
                mes.equals(concurso.getMes()))) {

            if (concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(instId, bancaId, ano, mes)) {
                throw new com.studora.exception.ConflictException(
                        "Já existe um concurso cadastrado para esta instituição, banca, ano e mês.");
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
                throw new ValidationException("O cargo " + cc.getCargo().getNome()
                        + " não pode ser removido pois está associado a uma prova.");
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
                if (prova == null)
                    throw new ResourceNotFoundException("Prova", "ID", pReq.getId());
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

                }
            }

            if (pReq.getSecoes() != null) {
                synchronizeSecoes(prova, pReq.getSecoes());
            }
        }
    }

    private void synchronizeSecoes(com.studora.entity.Prova prova,
            List<com.studora.dto.request.ProvaSecaoUpdateRequest> requests) {
        log.info("synchronizeSecoes called: prova.id={}, prova.concursoCargo={}, requests={}", prova.getId(),
                prova.getConcursoCargo(), requests);
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

        // 3. Update or Create
        for (com.studora.dto.request.ProvaSecaoUpdateRequest sReq : requests) {
            com.studora.entity.ProvaSecao secao;
            ConcursoCargo cc = prova.getConcursoCargo();
            log.info("ProvaSecao sync: prova.id={}, prova.concursoCargo={}, secao.id={}, sReq.nome={}", prova.getId(),
                    cc != null ? cc.getId() : "null", sReq.getId(), sReq.getNome());

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
            } else {
                log.warn("cc is null! Cannot set secaoCargo for secao.id={}", sReq.getId());
            }

            // Update or Create ProvaSecao
            if (sReq.getId() != null) {
                secao = existingMap.get(sReq.getId());
                if (secao == null)
                    throw new ResourceNotFoundException("ProvaSecao", "ID", sReq.getId());
                secao.setNome(sReq.getNome());
                secao.setOrdem(sReq.getOrdem());
                if (sc != null) {
                    secao.setSecaoCargo(sc);
                }
            } else {
                secao = new com.studora.entity.ProvaSecao();
                secao.setProva(prova);
                secao.setNome(sReq.getNome());
                secao.setOrdem(sReq.getOrdem());
                if (sc != null) {
                    secao.setSecaoCargo(sc);
                }
                prova.getSecoes().add(secao);

            }

            if (sc != null) {
                applyMetricsLogic(sc, secao, sReq);
                log.info("Applied metrics logic and set secaoCargo.sc.id={} for secao.id={}", sc.getId(),
                        secao.getId());
            }
        }

        // Validate: A subtema cannot be in more than one section definition per cargo
        // (This check is now implicitly across all provas of the cargo, but here we
        // check the cargo context)
        ConcursoCargo cc = prova.getConcursoCargo();
        if (cc != null) {
            Map<Long, String> subtemaToSecaoDefMap = new java.util.HashMap<>();
            List<com.studora.entity.SecaoCargo> secoesCargos = secaoCargoRepository
                    .findAllByConcursoCargoId(cc.getId());
            for (com.studora.entity.SecaoCargo sc : secoesCargos) {
                for (com.studora.entity.SecaoDisciplina sd : sc.getDisciplinas()) {
                    for (Subtema st : sd.getSubtemas()) {
                        if (subtemaToSecaoDefMap.containsKey(st.getId())) {
                            throw new com.studora.exception.ValidationException(
                                    "O subtema '" + st.getNome()
                                            + "' está vinculado a múltiplas seções temáticas para o cargo '" +
                                            cc.getCargo().getNome() + "': '" + subtemaToSecaoDefMap.get(st.getId())
                                            + "' e '" + sc.getNome() + "'.");
                        }
                        subtemaToSecaoDefMap.put(st.getId(), sc.getNome());
                    }
                }
            }
        }
    }

    private void applyMetricsLogic(com.studora.entity.SecaoCargo sc, com.studora.entity.ProvaSecao ps,
            com.studora.dto.request.ProvaSecaoRequest sReq) {
        if (sReq.getDisciplinas() != null) {
            if (sReq instanceof com.studora.dto.request.ProvaSecaoUpdateRequest) {
                synchronizeDisciplinas(sc, sReq.getDisciplinas());
            } else {
                // For creation (SecaoDisciplinaRequest in ProvaSecaoCreateRequest)
                sc.getDisciplinas().clear();
                for (com.studora.dto.request.SecaoDisciplinaRequest dReq : sReq.getDisciplinas()) {
                    com.studora.entity.SecaoDisciplina sd = new com.studora.entity.SecaoDisciplina();
                    sd.setSecaoCargo(sc);
                    sd.setNome(dReq.getNome());
                    sd.setPeso(dReq.getPeso());
                    sd.setNumQuestoes(dReq.getNumQuestoes());
                    sd.setNotaMinima(dReq.getNotaMinima());
                    if (dReq.getSubtemaIds() != null) {
                        List<Subtema> subtemas = subtemaRepository.findAllById(dReq.getSubtemaIds());
                        sd.setSubtemas(new HashSet<>(subtemas));
                    }
                    sc.getDisciplinas().add(sd);
                }
            }
        }

        boolean hasComputedMetrics = sc.getDisciplinas().stream()
                .anyMatch(d -> d.getNumQuestoes() != null || d.getPeso() != null || d.getNotaMinima() != null);

        if (hasComputedMetrics) {
            // Validate: all or none
            long filledCount = sc.getDisciplinas().stream()
                    .filter(d -> d.getNumQuestoes() != null || d.getPeso() != null || d.getNotaMinima() != null)
                    .count();
            if (filledCount < sc.getDisciplinas().size()) {
                throw new ValidationException("Inconsistência na seção '" + sc.getNome()
                        + "': Todas as disciplinas devem ter métricas definidas ou nenhuma deve ter.");
            }

            int totalQuestoes = sc.getDisciplinas().stream()
                    .mapToInt(d -> d.getNumQuestoes() != null ? d.getNumQuestoes() : 0).sum();
            double totalPeso = sc.getDisciplinas().stream().mapToDouble(d -> d.getPeso() != null ? d.getPeso() : 0.0)
                    .sum();
            double totalMinima = sc.getDisciplinas().stream()
                    .mapToDouble(d -> d.getNotaMinima() != null ? d.getNotaMinima() : 0.0).sum();

            sc.setNumQuestoes(totalQuestoes);
            sc.setPeso(totalPeso);
            sc.setNotaMinima(totalMinima);
        } else {
            sc.setNumQuestoes(sReq.getNumQuestoes() != null && sReq.getNumQuestoes() > 0 ? sReq.getNumQuestoes() : 1);
            sc.setPeso(sReq.getPeso() != null ? sReq.getPeso() : 1.0);
            sc.setNotaMinima(sReq.getNotaMinima() != null ? sReq.getNotaMinima() : 0.0);
        }

        int finalOrdem = sReq.getOrdem() != null ? sReq.getOrdem() : (sc.getOrdem() != null ? sc.getOrdem() : 1);
        sc.setOrdem(finalOrdem);

        if (ps != null) {
            ps.setNumQuestoes(sc.getNumQuestoes());
            ps.setOrdem(sc.getOrdem());
            ps.setSecaoCargo(sc);
        }
    }

    private void synchronizeDisciplinas(com.studora.entity.SecaoCargo sc,
            List<com.studora.dto.request.SecaoDisciplinaRequest> requests) {
        Map<Long, com.studora.entity.SecaoDisciplina> existingMap = sc.getDisciplinas().stream()
                .filter(sd -> sd.getId() != null)
                .collect(Collectors.toMap(com.studora.entity.SecaoDisciplina::getId, sd -> sd));

        Set<Long> idsToKeep = requests.stream()
                .map(com.studora.dto.request.SecaoDisciplinaRequest::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 1. Remove orphans
        for (com.studora.entity.SecaoDisciplina sd : new java.util.ArrayList<>(sc.getDisciplinas())) {
            if (sd.getId() != null && !idsToKeep.contains(sd.getId())) {
                if (questaoRepository.existsBySecaoDisciplinaId(sd.getId())) {
                    throw new com.studora.exception.ValidationException(
                            "Não é possível remover a disciplina '" + sd.getNome()
                                    + "' pois existem questões vinculadas a ela.");
                }
                sc.getDisciplinas().remove(sd);
            }
        }

        // 2. Update or Create
        for (com.studora.dto.request.SecaoDisciplinaRequest dReq : requests) {
            com.studora.entity.SecaoDisciplina sd;
            if (dReq.getId() != null) {
                sd = existingMap.get(dReq.getId());
                if (sd == null)
                    throw new ResourceNotFoundException("SecaoDisciplina", "ID", dReq.getId());

                // Check for principal subtema removal
                if (dReq.getSubtemaIds() != null) {
                    List<Long> invalidQuestaoIds = questaoRepository
                            .findQuestionIdsWithInvalidPrincipalSubtema(dReq.getId(), dReq.getSubtemaIds());
                    if (!invalidQuestaoIds.isEmpty()) {
                        throw new com.studora.exception.ValidationException(
                                "Não é possível remover subtemas da disciplina '" + dReq.getNome() +
                                        "' pois existem questões vinculadas que os utilizam como subtema principal (IDs: "
                                        +
                                        invalidQuestaoIds.stream().map(String::valueOf)
                                                .collect(Collectors.joining(", "))
                                        + ").");
                    }
                }
            } else {
                sd = new com.studora.entity.SecaoDisciplina();
                sd.setSecaoCargo(sc);
                sc.getDisciplinas().add(sd);
            }
            sd.setNome(dReq.getNome());
            // Validate field filling (all or none, and minimums)
            boolean anyFilled = dReq.getNumQuestoes() != null || dReq.getPeso() != null || dReq.getNotaMinima() != null;
            boolean allFilled = dReq.getNumQuestoes() != null && dReq.getPeso() != null && dReq.getNotaMinima() != null;

            if (anyFilled) {
                if (!allFilled) {
                    throw new com.studora.exception.ValidationException(
                            "Inconsistência na disciplina '" + dReq.getNome()
                                    + "': Se definir uma métrica, deve definir todas (numQuestoes, peso, notaMinima).");
                }
                if (dReq.getNumQuestoes() < 1 || dReq.getPeso() < 1.0 || dReq.getNotaMinima() < 0) {
                    throw new com.studora.exception.ValidationException(
                            "Inconsistência na disciplina '" + dReq.getNome()
                                    + "': Valores devem respeitar os mínimos (numQuestoes >= 1, peso >= 1.0, notaMinima >= 0).");
                }
            }

            sd.setPeso(dReq.getPeso());
            sd.setNumQuestoes(dReq.getNumQuestoes());
            sd.setNotaMinima(dReq.getNotaMinima());

            // Validate subtema count
            if (dReq.getSubtemaIds() == null || dReq.getSubtemaIds().isEmpty()) {
                throw new com.studora.exception.ValidationException(
                        "A disciplina '" + dReq.getNome() + "' deve ter pelo menos um subtema associado.");
            }

            // Process subtemas (ignore duplicates)
            Set<Long> uniqueSubtemaIds = new HashSet<>(dReq.getSubtemaIds());
            
            // Validate subtema unique association within the same Cargo
            for (Long subId : uniqueSubtemaIds) {
                for (com.studora.entity.SecaoDisciplina existingSd : sc.getDisciplinas()) {
                    if (existingSd != sd && existingSd.getSubtemas().stream().anyMatch(s -> s.getId().equals(subId))) {
                        throw new com.studora.exception.ValidationException(
                                "O subtema '" + subtemaRepository.findById(subId).map(Subtema::getNome).orElse("ID: " + subId) 
                                + "' já está associado a outra disciplina desta seção do cargo.");
                    }
                }
            }

            List<Subtema> subtemas = subtemaRepository.findAllById(new ArrayList<>(uniqueSubtemaIds));
            sd.setSubtemas(new HashSet<>(subtemas));
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
            throw new ValidationException(
                    "Você já está inscrito em outro cargo para este concurso. Desinscreva-se primeiro.");
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
        if (val instanceof LocalDateTime)
            return (LocalDateTime) val;
        if (val instanceof String)
            return LocalDateTime.parse((String) val,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return null;
    }

    private com.studora.dto.StatSliceDto aggregateStatSlices(List<com.studora.dto.StatSliceDto> slices) {
        if (slices == null || slices.isEmpty())
            return null;
        com.studora.dto.StatSliceDto result = new com.studora.dto.StatSliceDto();
        result.setRespondidas(0L);
        result.setAcertadas(0L);
        result.setTotalQuestoes(0L);
        result.setDificuldade(new java.util.HashMap<>());

        for (com.studora.dto.StatSliceDto s : slices) {
            if (s == null)
                continue;
            result.setRespondidas(result.getRespondidas() + (s.getRespondidas() != null ? s.getRespondidas() : 0L));
            result.setAcertadas(result.getAcertadas() + (s.getAcertadas() != null ? s.getAcertadas() : 0L));
            result.setTotalQuestoes(
                    result.getTotalQuestoes() + (s.getTotalQuestoes() != null ? s.getTotalQuestoes() : 0L));

            if (s.getDificuldade() != null) {
                s.getDificuldade().forEach((k, v) -> {
                    if (v == null)
                        return;
                    com.studora.dto.DificuldadeStatDto existing = result.getDificuldade().get(k);
                    if (existing == null) {
                        existing = new com.studora.dto.DificuldadeStatDto();
                        existing.setTotal(0L);
                        existing.setCorretas(0L);
                        result.getDificuldade().put(k, existing);
                    }
                    existing.setTotal(existing.getTotal() + v.getTotal());
                    existing.setCorretas(existing.getCorretas() + v.getCorretas());
                });
            }
        }
        return result;
    }
}
