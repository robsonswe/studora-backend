package com.studora.service;

import com.studora.dto.DificuldadeStatDto;
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
import com.studora.entity.ConcursoCargoSubtema;
import com.studora.entity.Dificuldade;
import com.studora.entity.Instituicao;
import com.studora.entity.Resposta;
import com.studora.entity.Subtema;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.ConcursoMapper;
import com.studora.repository.*;
import com.studora.repository.specification.ConcursoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final EstudoSubtemaRepository estudoSubtemaRepository;
    private final SubtemaRepository subtemaRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaRepository respostaRepository;
    private final ConcursoCargoSubtemaRepository concursoCargoSubtemaRepository;
    private final ConcursoMapper concursoMapper;

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
        enrichTopicos(result.getContent().stream()
                .filter(d -> d.getCargos() != null)
                .flatMap(d -> d.getCargos().stream())
                .collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public ConcursoDetailDto getConcursoDetailById(Long id) {
        Concurso concurso = concursoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", id));
        ConcursoDetailDto dto = concursoMapper.toDetailDto(concurso);
        enrichTopicos(dto.getCargos() != null ? dto.getCargos() : java.util.List.of());
        return dto;
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

        // Process Cargos
        List<Long> cargoIds = request.getCargos().stream().distinct().collect(Collectors.toList());
        for (Long cargoId : cargoIds) {
            Cargo cargo = cargoRepository.findById(cargoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cargo", "ID", cargoId));
            ConcursoCargo cc = new ConcursoCargo();
            cc.setCargo(cargo);
            concurso.addConcursoCargo(cc);
        }

        // Process Topicos
        if (request.getTopicos() != null && !request.getTopicos().isEmpty()) {
            processTopicos(request.getTopicos(), concurso, cargoIds);
        }

        return concursoMapper.toDetailDto(concursoRepository.save(concurso));
    }

    public ConcursoDetailDto update(Long id, ConcursoUpdateRequest request) {
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
            if (!questaoCargoRepository.findByConcursoCargoId(cc.getId()).isEmpty()) {
                throw new ValidationException("Não é possível remover o cargo ID " + cc.getCargo().getId() + " pois existem questões associadas a ele neste concurso.");
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

        // Process Topicos (diff-based)
        if (request.getTopicos() != null) {
            processTopicosUpdate(request.getTopicos(), concurso, newCargoIds);
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

    public ConcursoCargoSummaryDto toggleInscricao(Long concursoCargoId) {
        log.info("Togglings inscrição para ConcursoCargo ID: {}", concursoCargoId);
        ConcursoCargo cc = concursoCargoRepository.findById(concursoCargoId)
                .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "ID", concursoCargoId));
        
        boolean newStatus = !cc.isInscrito();
        
        if (newStatus && concursoCargoRepository.existsByConcursoIdAndInscritoTrue(cc.getConcurso().getId())) {
            throw new ValidationException("Você já está inscrito em outro cargo para este concurso. Desinscreva-se primeiro.");
        }

        cc.setInscrito(newStatus);
        cc = concursoCargoRepository.save(cc);
        
        ConcursoCargoSummaryDto dto = new ConcursoCargoSummaryDto();
        dto.setId(cc.getId());
        dto.setCargoId(cc.getCargo().getId());
        dto.setCargoNome(cc.getCargo().getNome());
        dto.setNivel(cc.getCargo().getNivel());
        dto.setArea(cc.getCargo().getArea());
        dto.setInscrito(cc.isInscrito());
        return dto;
    }

    private void processTopicos(Map<Long, List<Long>> topicosRequest, Concurso concurso, List<Long> validCargoIds) {
        // Build cargoId -> ConcursoCargo map from the concurso
        Map<Long, ConcursoCargo> cargoMap = concurso.getConcursoCargos().stream()
                .collect(Collectors.toMap(cc -> cc.getCargo().getId(), cc -> cc));

        for (Map.Entry<Long, List<Long>> entry : topicosRequest.entrySet()) {
            Long subtemaId = entry.getKey();
            List<Long> cargoIds = entry.getValue();

            // Validate subtema has at least 1 cargo
            if (cargoIds == null || cargoIds.isEmpty()) {
                throw new ValidationException("O subtema ID " + subtemaId + " deve estar associado a pelo menos 1 cargo.");
            }

            // Validate subtema exists
            subtemaRepository.findById(subtemaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", subtemaId));

            // Deduplicate cargo IDs
            Set<Long> uniqueCargoIds = new HashSet<>(cargoIds);

            for (Long cargoId : uniqueCargoIds) {
                // Validate cargo exists in this concurso
                if (!validCargoIds.contains(cargoId)) {
                    throw new ValidationException("O cargo ID " + cargoId + " não está associado a este concurso.");
                }

                ConcursoCargo cc = cargoMap.get(cargoId);
                ConcursoCargoSubtema ccs = new ConcursoCargoSubtema();
                ccs.setSubtema(subtemaRepository.getReferenceById(subtemaId));
                cc.addConcursoCargoSubtema(ccs);
            }
        }
    }

    private void processTopicosUpdate(Map<Long, List<Long>> topicosRequest, Concurso concurso, List<Long> validCargoIds) {
        // Build cargoId -> ConcursoCargo map
        Map<Long, ConcursoCargo> cargoMap = concurso.getConcursoCargos().stream()
                .collect(Collectors.toMap(cc -> cc.getCargo().getId(), cc -> cc));

        // Build existing pairs: Set of "concursoCargoId:subtemaId"
        Set<String> existingPairs = new HashSet<>();
        for (ConcursoCargo cc : concurso.getConcursoCargos()) {
            for (ConcursoCargoSubtema ccs : cc.getConcursoCargoSubtemas()) {
                existingPairs.add(cc.getId() + ":" + ccs.getSubtema().getId());
            }
        }

        // Build requested pairs from the request (and validate)
        Set<String> requestedPairs = new HashSet<>();
        for (Map.Entry<Long, List<Long>> entry : topicosRequest.entrySet()) {
            Long subtemaId = entry.getKey();
            List<Long> cargoIds = entry.getValue();

            // Validate subtema has at least 1 cargo
            if (cargoIds == null || cargoIds.isEmpty()) {
                throw new ValidationException("O subtema ID " + subtemaId + " deve estar associado a pelo menos 1 cargo.");
            }

            // Validate subtema exists
            subtemaRepository.findById(subtemaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", subtemaId));

            // Deduplicate cargo IDs
            Set<Long> uniqueCargoIds = new HashSet<>(cargoIds);

            for (Long cargoId : uniqueCargoIds) {
                if (!validCargoIds.contains(cargoId)) {
                    throw new ValidationException("O cargo ID " + cargoId + " não está associado a este concurso.");
                }
                ConcursoCargo cc = cargoMap.get(cargoId);
                requestedPairs.add(cc.getId() + ":" + subtemaId);
            }
        }

        // Remove pairs that exist but are not in request
        for (ConcursoCargo cc : concurso.getConcursoCargos()) {
            Set<ConcursoCargoSubtema> toRemove = cc.getConcursoCargoSubtemas().stream()
                    .filter(ccs -> !requestedPairs.contains(cc.getId() + ":" + ccs.getSubtema().getId()))
                    .collect(Collectors.toSet());
            for (ConcursoCargoSubtema ccs : toRemove) {
                cc.getConcursoCargoSubtemas().remove(ccs);
            }
        }

        // Add pairs that are in request but don't exist
        for (Map.Entry<Long, List<Long>> entry : topicosRequest.entrySet()) {
            Long subtemaId = entry.getKey();
            Set<Long> uniqueCargoIds = new HashSet<>(entry.getValue());

            for (Long cargoId : uniqueCargoIds) {
                ConcursoCargo cc = cargoMap.get(cargoId);
                String pairKey = cc.getId() + ":" + subtemaId;
                if (!existingPairs.contains(pairKey)) {
                    ConcursoCargoSubtema ccs = new ConcursoCargoSubtema();
                    ccs.setSubtema(subtemaRepository.getReferenceById(subtemaId));
                    cc.addConcursoCargoSubtema(ccs);
                }
            }
        }
    }

    private void enrichTopicos(java.util.List<ConcursoCargoSummaryDto> cargos) {
        List<Long> subtemaIds = cargos.stream()
                .filter(c -> c.getTopicos() != null)
                .flatMap(c -> c.getTopicos().stream())
                .map(SubtemaSummaryDto::getId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        if (subtemaIds.isEmpty()) {
            return;
        }

        Map<Long, Long> counts = estudoSubtemaRepository.countBySubtemaIds(subtemaIds).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()));

        Map<Long, LocalDateTime> dates = estudoSubtemaRepository.findLatestStudyDatesBySubtemaIds(subtemaIds).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> {
                            Object val = row[1];
                            if (val instanceof LocalDateTime) return (LocalDateTime) val;
                            if (val instanceof String) return LocalDateTime.parse((String) val, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            return null;
                        }));

        // Questao stats
        Map<Long, Long> totalQuestoesMap = questaoRepository.countQuestoesBySubtemaIds(subtemaIds).stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[1]).longValue()));
        Map<Long, Long> respondidasMap = respostaRepository.countRespondidasBySubtemaIds(subtemaIds).stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[1]).longValue()));
        Map<Long, Long> acertadasMap = respostaRepository.countAcertadasBySubtemaIds(subtemaIds).stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[1]).longValue()));
        Map<Long, Double> avgTempoMap = respostaRepository.avgTempoBySubtemaIds(subtemaIds).stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[1]).doubleValue()));

        // Difficulty stats
        List<Resposta> allRespostas = respostaRepository.findAllBySubtemaIdsWithDetails(subtemaIds);
        Map<Long, Map<String, DificuldadeStatDto>> dificuldadeMap = computeDificuldadeStatsBatch(allRespostas,
                r -> r.getQuestao().getSubtemas().stream().map(Subtema::getId).toList());

        for (ConcursoCargoSummaryDto cargo : cargos) {
            if (cargo.getTopicos() == null) continue;
            for (SubtemaSummaryDto topico : cargo.getTopicos()) {
                Long topId = topico.getId();
                topico.setTotalEstudos(counts.getOrDefault(topId, 0L));
                topico.setUltimoEstudo(dates.get(topId));
                topico.setTotalQuestoes(totalQuestoesMap.getOrDefault(topId, 0L));
                topico.setQuestoesRespondidas(respondidasMap.getOrDefault(topId, 0L));
                topico.setQuestoesAcertadas(acertadasMap.getOrDefault(topId, 0L));
                topico.setMediaTempoResposta(avgTempoMap.containsKey(topId) ? avgTempoMap.get(topId).intValue() : null);
                topico.setDificuldadeRespostas(dificuldadeMap.getOrDefault(topId, Collections.emptyMap()));
            }
        }
    }

    private Map<String, DificuldadeStatDto> computeDificuldadeStatsFromResponses(List<Resposta> responses) {
        Map<String, DificuldadeStatDto> result = new HashMap<>();
        for (Dificuldade d : Dificuldade.values()) {
            result.put(d.name(), new DificuldadeStatDto(0, 0));
        }

        Map<Long, Resposta> latestByQuestao = new HashMap<>();
        for (Resposta r : responses) {
            latestByQuestao.merge(r.getQuestao().getId(), r, (existing, candidate) ->
                    candidate.getCreatedAt().isAfter(existing.getCreatedAt()) ? candidate : existing);
        }

        for (Resposta r : latestByQuestao.values()) {
            String diff = r.getDificuldade() != null ? r.getDificuldade().name() : Dificuldade.MEDIA.name();
            DificuldadeStatDto stat = result.get(diff);
            stat.setTotal(stat.getTotal() + 1);
            if (isCorrect(r)) {
                stat.setCorretas(stat.getCorretas() + 1);
            }
        }
        return result;
    }

    private Map<Long, Map<String, DificuldadeStatDto>> computeDificuldadeStatsBatch(
            List<Resposta> responses, java.util.function.Function<Resposta, List<Long>> scopeIdsExtractor) {
        Map<Long, List<Resposta>> byScope = new HashMap<>();
        for (Resposta r : responses) {
            for (Long scopeId : scopeIdsExtractor.apply(r)) {
                byScope.computeIfAbsent(scopeId, k -> new ArrayList<>()).add(r);
            }
        }
        Map<Long, Map<String, DificuldadeStatDto>> result = new HashMap<>();
        for (var entry : byScope.entrySet()) {
            result.put(entry.getKey(), computeDificuldadeStatsFromResponses(entry.getValue()));
        }
        return result;
    }

    private boolean isCorrect(Resposta r) {
        if (r.getAlternativaEscolhida() == null) return false;
        return Boolean.TRUE.equals(r.getAlternativaEscolhida().getCorreta());
    }
}