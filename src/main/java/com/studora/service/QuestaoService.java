package com.studora.service;

import com.studora.dto.PageResponse;
import com.studora.dto.questao.QuestaoCargoDto;
import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoFilter;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.request.QuestaoCargoCreateRequest;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.entity.*;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.mapper.QuestaoCargoMapper;
import com.studora.mapper.QuestaoMapper;
import com.studora.repository.*;
import com.studora.repository.specification.QuestaoSpecification;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuestaoService {

    private final QuestaoRepository questaoRepository;
    private final ConcursoRepository concursoRepository;
    private final SubtemaRepository subtemaRepository;
    private final ConcursoCargoRepository concursoCargoRepository;
    private final QuestaoCargoRepository questaoCargoRepository;
    private final RespostaRepository respostaRepository;
    private final AlternativaRepository alternativaRepository;
    private final QuestaoMapper questaoMapper;
    private final QuestaoCargoMapper questaoCargoMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<QuestaoSummaryDto> findAll(QuestaoFilter filter, Pageable pageable) {
        Specification<Questao> spec = QuestaoSpecification.withFilter(filter);
        return questaoRepository.findAll(spec, pageable)
                .map(questaoMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public QuestaoDetailDto getQuestaoDetailById(Long id) {
        Questao questao = questaoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", id));
        return questaoMapper.toDetailDto(questao);
    }

    public QuestaoDetailDto create(QuestaoCreateRequest request) {
        log.info("Criando nova questão para o concurso ID: {}", request.getConcursoId());
        
        Concurso concurso = concursoRepository.findById(request.getConcursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", request.getConcursoId()));

        validateQuestaoBusinessRules(request.getAlternativas(), request.getAnulada(), request.getConcursoCargoIds(), request.getConcursoId());

        Questao questao = questaoMapper.toEntity(request);
        questao.setConcurso(concurso);

        if (request.getSubtemaIds() != null && !request.getSubtemaIds().isEmpty()) {
            List<Subtema> subtemas = subtemaRepository.findAllById(request.getSubtemaIds());
            questao.setSubtemas(new HashSet<>(subtemas));
        }

        if (request.getAlternativas() != null) {
            request.getAlternativas().forEach(altReq -> {
                Alternativa alt = new Alternativa();
                alt.setQuestao(questao);
                alt.setTexto(altReq.getTexto());
                alt.setCorreta(altReq.getCorreta());
                alt.setOrdem(altReq.getOrdem());
                alt.setJustificativa(altReq.getJustificativa());
                questao.getAlternativas().add(alt);
            });
        }

        normalizeAlternativaOrders(questao);
        Questao savedQuestao = questaoRepository.save(questao);

        if (request.getConcursoCargoIds() != null && !request.getConcursoCargoIds().isEmpty()) {
            for (Long ccId : request.getConcursoCargoIds()) {
                ConcursoCargo cc = concursoCargoRepository.findById(ccId)
                        .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "ID", ccId));
                
                QuestaoCargo qc = new QuestaoCargo();
                qc.setQuestao(savedQuestao);
                qc.setConcursoCargo(cc);
                questaoCargoRepository.save(qc);
            }
        }

        entityManager.flush();
        return questaoMapper.toDetailDto(questaoRepository.findByIdWithDetails(savedQuestao.getId()).get());
    }

    public QuestaoDetailDto update(Long id, QuestaoUpdateRequest request) {
        log.info("Atualizando questão ID: {}", id);
        
        Questao questao = questaoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", id));

        validateQuestaoBusinessRules(request.getAlternativas(), request.getAnulada(), request.getConcursoCargoIds(), request.getConcursoId());

        boolean contentChanged = hasContentChanged(questao, request);
        if (contentChanged) {
            log.info("Mudança de conteúdo detectada na questão {}. Excluindo histórico de respostas.", id);
            respostaRepository.deleteByQuestaoId(id);
        }

        if (request.getConcursoId() != null) {
            Concurso concurso = concursoRepository.findById(request.getConcursoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", request.getConcursoId()));
            questao.setConcurso(concurso);
        }

        if (request.getSubtemaIds() != null) {
            List<Subtema> subtemas = subtemaRepository.findAllById(request.getSubtemaIds());
            questao.setSubtemas(new HashSet<>(subtemas));
        }

        questaoMapper.updateEntityFromDto(request, questao);
        
        if (request.getAlternativas() != null) {
            List<Alternativa> currentAlts = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(id);
            java.util.Map<Long, Alternativa> existingMap = currentAlts.stream()
                .collect(Collectors.toMap(Alternativa::getId, a -> a));

            Set<Long> idsToKeep = request.getAlternativas().stream()
                .map(com.studora.dto.request.AlternativaUpdateRequest::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

            // 1. Remove orphans
            for (Alternativa alt : currentAlts) {
                if (!idsToKeep.contains(alt.getId())) {
                    questao.getAlternativas().remove(alt);
                    alternativaRepository.delete(alt);
                }
            }
            alternativaRepository.flush();

            // 2. Update or Create
            for (com.studora.dto.request.AlternativaUpdateRequest altReq : request.getAlternativas()) {
                Alternativa alt = null;
                if (altReq.getId() != null) {
                    alt = existingMap.get(altReq.getId());
                }

                if (alt != null) {
                    // Update existing
                    alt.setTexto(altReq.getTexto());
                    alt.setCorreta(altReq.getCorreta());
                    alt.setOrdem(altReq.getOrdem());
                    alt.setJustificativa(altReq.getJustificativa());
                    alternativaRepository.save(alt);
                    if (!questao.getAlternativas().contains(alt)) {
                        questao.getAlternativas().add(alt);
                    }
                } else {
                    // Create new
                    alt = new Alternativa();
                    alt.setQuestao(questao);
                    alt.setTexto(altReq.getTexto());
                    alt.setCorreta(altReq.getCorreta());
                    alt.setOrdem(altReq.getOrdem());
                    alt.setJustificativa(altReq.getJustificativa());
                    alternativaRepository.save(alt);
                    questao.getAlternativas().add(alt);
                }
            }
            alternativaRepository.flush();
        }

        if (request.getConcursoCargoIds() != null) {
            synchronizeCargos(questao, request.getConcursoCargoIds());
        }

        Questao saved = questaoRepository.save(questao);
        normalizeAlternativaOrders(saved);
        saved = questaoRepository.save(saved);
        entityManager.flush();
        return questaoMapper.toDetailDto(questaoRepository.findByIdWithDetails(saved.getId()).get());
    }

    private void normalizeAlternativaOrders(Questao questao) {
        if (questao.getAlternativas() == null || questao.getAlternativas().isEmpty()) return;
        
        List<Alternativa> sorted = questao.getAlternativas().stream()
                .sorted(java.util.Comparator.comparing(Alternativa::getOrdem))
                .collect(Collectors.toList());
        
        int order = 1;
        for (Alternativa alt : sorted) {
            alt.setOrdem(order++);
        }
    }

    private void validateQuestaoBusinessRules(List<? extends com.studora.dto.request.AlternativaBaseRequest> alternativas, 
                                            Boolean anulada, List<Long> concursoCargoIds, Long concursoId) {
        if (alternativas == null || alternativas.size() < com.studora.common.constants.AppConstants.MIN_ALTERNATIVAS) {
            throw new com.studora.exception.ValidationException("A questão deve ter pelo menos " + com.studora.common.constants.AppConstants.MIN_ALTERNATIVAS + " alternativas");
        }

        if (Boolean.FALSE.equals(anulada)) {
            long correctCount = alternativas.stream().filter(com.studora.dto.request.AlternativaBaseRequest::getCorreta).count();
            if (correctCount != com.studora.common.constants.AppConstants.REQUIRED_CORRECT_ALTERNATIVAS) {
                throw new com.studora.exception.ValidationException("Uma questão não anulada deve ter exatamente uma alternativa correta");
            }
        }

        if (concursoCargoIds == null || concursoCargoIds.isEmpty()) {
            throw new com.studora.exception.ValidationException("A questão deve estar associada a pelo menos um cargo");
        }

        for (Long ccId : concursoCargoIds) {
            ConcursoCargo cc = concursoCargoRepository.findById(ccId)
                    .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "ID", ccId));
            if (!cc.getConcurso().getId().equals(concursoId)) {
                throw new com.studora.exception.ValidationException("O cargo ID " + ccId + " não pertence ao concurso ID " + concursoId);
            }
        }
    }

    private boolean hasContentChanged(Questao questao, QuestaoUpdateRequest request) {
        if (!request.getEnunciado().equals(questao.getEnunciado())) return true;
        if (!request.getAnulada().equals(questao.getAnulada())) return true;
        
        if (request.getAlternativas().size() != questao.getAlternativas().size()) return true;
        
        java.util.Map<Long, Alternativa> currentMap = questao.getAlternativas().stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(Alternativa::getId, a -> a));

        for (com.studora.dto.request.AlternativaUpdateRequest altReq : request.getAlternativas()) {
            if (altReq.getId() == null) return true; // New alternative
            Alternativa current = currentMap.get(altReq.getId());
            if (current == null) return true; // Alternative not found (shouldn't happen with valid IDs)
            if (!altReq.getTexto().equals(current.getTexto())) return true;
            if (!altReq.getCorreta().equals(current.getCorreta())) return true;
            
            String reqJust = altReq.getJustificativa() != null ? altReq.getJustificativa() : "";
            String curJust = current.getJustificativa() != null ? current.getJustificativa() : "";
            if (!reqJust.equals(curJust)) return true;
        }
        
        return false;
    }

    private void synchronizeCargos(Questao questao, List<Long> concursoCargoIds) {
        java.util.Map<Long, QuestaoCargo> currentMap = questao.getQuestaoCargos().stream()
                .collect(Collectors.toMap(qc -> qc.getConcursoCargo().getId(), qc -> qc));

        Set<Long> idsToKeep = new HashSet<>(concursoCargoIds);

        // 1. Remove orphans
        List<QuestaoCargo> toRemove = new java.util.ArrayList<>();
        for (QuestaoCargo qc : questao.getQuestaoCargos()) {
            if (!idsToKeep.contains(qc.getConcursoCargo().getId())) {
                toRemove.add(qc);
            }
        }
        
        for (QuestaoCargo qc : toRemove) {
            questao.getQuestaoCargos().remove(qc);
            questaoCargoRepository.delete(qc);
        }
        questaoCargoRepository.flush();

        // 2. Add new
        for (Long ccId : concursoCargoIds) {
            if (!currentMap.containsKey(ccId)) {
                ConcursoCargo cc = concursoCargoRepository.findById(ccId)
                        .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "ID", ccId));
                QuestaoCargo qc = new QuestaoCargo();
                qc.setQuestao(questao);
                qc.setConcursoCargo(cc);
                questaoCargoRepository.save(qc);
                questao.getQuestaoCargos().add(qc);
            }
        }
        questaoCargoRepository.flush();
    }

    public void delete(Long id) {
        log.info("Excluindo questão ID: {}", id);
        if (!questaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Questão", "ID", id);
        }
        questaoRepository.deleteById(id);
    }

    public void toggleDesatualizada(Long id) {
        Questao questao = questaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", id));
        questao.setDesatualizada(!questao.getDesatualizada());
        questaoRepository.save(questao);
    }

    @Transactional(readOnly = true)
    public List<QuestaoCargoDto> getCargosByQuestaoId(Long questaoId) {
        return questaoCargoRepository.findByQuestaoId(questaoId).stream()
                .map(questaoCargoMapper::toDto)
                .collect(Collectors.toList());
    }

    public QuestaoCargoDto addCargoToQuestao(Long questaoId, QuestaoCargoCreateRequest request) {
        if (!questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(questaoId, request.getConcursoCargoId()).isEmpty()) {
            throw new ConflictException("Esta questão já está associada a este cargo de concurso.");
        }

        Questao questao = questaoRepository.findById(questaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", questaoId));
        
        ConcursoCargo cc = concursoCargoRepository.findById(request.getConcursoCargoId())
                .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "ID", request.getConcursoCargoId()));

        if (!cc.getConcurso().getId().equals(questao.getConcurso().getId())) {
            throw new com.studora.exception.ValidationException("O cargo ID " + request.getConcursoCargoId() + " não pertence ao concurso da questão");
        }

        QuestaoCargo qc = new QuestaoCargo();
        qc.setConcursoCargo(cc);
        questao.addQuestaoCargo(qc);

        return questaoCargoMapper.toDto(questaoCargoRepository.save(qc));
    }

    public void removeCargoFromQuestao(Long questaoId, Long concursoCargoId) {
        List<QuestaoCargo> qcs = questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(questaoId, concursoCargoId);
        if (qcs.isEmpty()) {
            throw new ResourceNotFoundException("Associação Questão-Cargo", "IDs", questaoId + "-" + concursoCargoId);
        }
        
        if (questaoCargoRepository.countByQuestaoId(questaoId) <= 1) {
            throw new com.studora.exception.ValidationException("Uma questão deve estar associada a pelo menos um cargo");
        }

        questaoCargoRepository.delete(qcs.get(0));
    }
}
