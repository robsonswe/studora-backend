package com.studora.service;

import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoFilter;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.entity.*;
import com.studora.exception.ResourceNotFoundException;
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
    private final RespostaRepository respostaRepository;
    private final AlternativaRepository alternativaRepository;
    private final QuestaoMapper questaoMapper;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<QuestaoSummaryDto> findAll(QuestaoFilter filter, Pageable pageable) {
        Specification<Questao> spec = QuestaoSpecification.withFilter(filter);
        
        // 1. Fetch the page of questions (initially without full details to keep count/pagination simple)
        Page<Questao> page = questaoRepository.findAll(spec, pageable);
        
        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. Extract IDs and fetch full details in a single query
        List<Long> ids = page.getContent().stream().map(Questao::getId).toList();
        List<Questao> withDetails = questaoRepository.findByIdsWithDetails(ids);
        
        // 3. Map to DTOs while maintaining the original page order
        java.util.Map<Long, Questao> detailsMap = withDetails.stream()
                .collect(java.util.stream.Collectors.toMap(Questao::getId, q -> q));
        
        return page.map(q -> questaoMapper.toSummaryDto(detailsMap.getOrDefault(q.getId(), q)));
    }

    @Transactional(readOnly = true)
    public QuestaoDetailDto getQuestaoDetailById(Long id) {
        Questao questao = questaoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", id));
        return questaoMapper.toDetailDto(questao);
    }

    @Transactional(readOnly = true)
    public QuestaoDetailDto getRandomQuestao(com.studora.dto.questao.QuestaoRandomFilter randomFilter) {
        QuestaoFilter filter = new QuestaoFilter();
        filter.setBancaId(randomFilter.getBancaId());
        filter.setInstituicaoId(randomFilter.getInstituicaoId());
        filter.setConcursoId(randomFilter.getConcursoId());
        filter.setCargoId(randomFilter.getCargoId());
        filter.setDisciplinaId(randomFilter.getDisciplinaId());
        filter.setTemaId(randomFilter.getTemaId());
        filter.setSubtemaId(randomFilter.getSubtemaId());
        
        // 1. Force desatualizada to false (not an option for random endpoint)
        filter.setDesatualizada(false);
        
        // 2. Default anulada to false if not provided
        filter.setAnulada(java.util.Objects.requireNonNullElse(randomFilter.getAnulada(), false));

        Specification<Questao> spec = QuestaoSpecification.withFilter(filter)
                .and(QuestaoSpecification.notAnsweredRecently(java.time.LocalDateTime.now().minusMonths(1)));

        long count = questaoRepository.count(spec);

        if (count == 0) {
            throw new ResourceNotFoundException("Não foi possível encontrar nenhuma questão com os filtros fornecidos.");
        }

        int randomIndex = (int) (Math.random() * count);
        Page<Questao> randomPage = questaoRepository.findAll(spec, org.springframework.data.domain.PageRequest.of(randomIndex, 1));
        
        if (randomPage.hasContent()) {
            // We need details, and findAll with pageable won't fetch everything efficiently/correctly with details in a single query
            // So we fetch the full details by ID of the randomly picked question
            return getQuestaoDetailById(randomPage.getContent().get(0).getId());
        }

        throw new ResourceNotFoundException("Não foi possível encontrar nenhuma questão com os filtros fornecidos.");
    }

    public QuestaoDetailDto create(QuestaoCreateRequest request) {
        log.info("Criando nova questão para o concurso ID: {}", request.getConcursoId());
        
        Concurso concurso = concursoRepository.findById(request.getConcursoId())
                .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", request.getConcursoId()));

        validateQuestaoBusinessRules(request.getAlternativas(), request.getAnulada(), request.getCargos(), request.getConcursoId());

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

        if (request.getCargos() != null && !request.getCargos().isEmpty()) {
            for (Long cargoId : request.getCargos()) {
                ConcursoCargo cc = concursoCargoRepository.findByConcursoIdAndCargoId(request.getConcursoId(), cargoId)
                        .stream().findFirst()
                        .orElseThrow(() -> new com.studora.exception.ValidationException("O cargo ID " + cargoId + " não pertence ao concurso ID " + request.getConcursoId()));
                
                QuestaoCargo qc = new QuestaoCargo();
                qc.setConcursoCargo(cc);
                questao.addQuestaoCargo(qc);
            }
        }

        normalizeAlternativaOrders(questao);
        Questao savedQuestao = questaoRepository.save(questao);

        entityManager.flush();
        return questaoMapper.toDetailDto(questaoRepository.findByIdWithDetails(savedQuestao.getId()).get());
    }

    public QuestaoDetailDto update(Long id, QuestaoUpdateRequest request) {
        log.info("Atualizando questão ID: {}", id);
        
        Questao questao = questaoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", id));

        validateQuestaoBusinessRules(request.getAlternativas(), request.getAnulada(), request.getCargos(), request.getConcursoId());

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
            log.debug("Processando atualização de alternativas para questão ID {}. Qtd: {}", id, request.getAlternativas().size());
            List<Alternativa> currentAlts = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(id);
            java.util.Map<Long, Alternativa> existingMap = currentAlts.stream()
                .collect(Collectors.toMap(Alternativa::getId, a -> a));

            Set<Long> idsToKeep = request.getAlternativas().stream()
                .map(com.studora.dto.request.AlternativaUpdateRequest::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

            // 1. Remove orphans
            questao.getAlternativas().removeIf(alt -> !idsToKeep.contains(alt.getId()));

            // 1.5. Temporary Shift: Set existing items to temporary unique negative orders
            // This prevents UNIQUE constraint violations (e.g. swapping 1 and 2) by clearing the positive number space.
            log.trace("Realizando shift temporário para ordens negativas para evitar conflitos.");
            for (Alternativa alt : questao.getAlternativas()) {
                if (alt.getId() != null) {
                    alt.setOrdem(-1 * alt.getId().intValue());
                }
            }
            // Flush only once here to clear the positive sequence in DB
            entityManager.flush();

            // 2. Update or Create
            for (com.studora.dto.request.AlternativaUpdateRequest altReq : request.getAlternativas()) {
                Alternativa alt;
                if (altReq.getId() != null) {
                    alt = existingMap.get(altReq.getId());
                    if (alt != null) {
                        alt.setTexto(altReq.getTexto());
                        alt.setCorreta(altReq.getCorreta());
                        alt.setOrdem(altReq.getOrdem());
                        alt.setJustificativa(altReq.getJustificativa());
                    }
                } else {
                    alt = new Alternativa();
                    alt.setQuestao(questao);
                    alt.setTexto(altReq.getTexto());
                    alt.setCorreta(altReq.getCorreta());
                    alt.setOrdem(altReq.getOrdem());
                    alt.setJustificativa(altReq.getJustificativa());
                    questao.getAlternativas().add(alt);
                }
            }
        }

        if (request.getCargos() != null) {
            synchronizeCargos(questao, request.getCargos(), request.getConcursoId());
        }

        normalizeAlternativaOrders(questao);
        Questao saved = questaoRepository.save(questao);
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
                                            Boolean anulada, List<Long> cargoIds, Long concursoId) {
        if (alternativas == null || alternativas.size() < com.studora.common.constants.AppConstants.MIN_ALTERNATIVAS) {
            throw new com.studora.exception.ValidationException("A questão deve ter pelo menos " + com.studora.common.constants.AppConstants.MIN_ALTERNATIVAS + " alternativas");
        }

        if (Boolean.FALSE.equals(anulada)) {
            long correctCount = alternativas.stream().filter(com.studora.dto.request.AlternativaBaseRequest::getCorreta).count();
            if (correctCount != com.studora.common.constants.AppConstants.REQUIRED_CORRECT_ALTERNATIVAS) {
                throw new com.studora.exception.ValidationException("Uma questão não anulada deve ter exatamente uma alternativa correta");
            }
        }

        if (cargoIds == null || cargoIds.isEmpty()) {
            throw new com.studora.exception.ValidationException("A questão deve estar associada a pelo menos um cargo");
        }

        for (Long cargoId : cargoIds) {
            if (!concursoCargoRepository.existsByConcursoIdAndCargoId(concursoId, cargoId)) {
                throw new com.studora.exception.ValidationException("O cargo ID " + cargoId + " não pertence ao concurso ID " + concursoId);
            }
        }
    }

    private boolean hasContentChanged(Questao questao, QuestaoUpdateRequest request) {
        if (!request.getEnunciado().equals(questao.getEnunciado())) return true;
        if (!request.getAnulada().equals(questao.getAnulada())) return true;
        
        // Check cargos change
        if (request.getCargos() != null) {
            Set<Long> currentCargoIds = questao.getQuestaoCargos().stream()
                    .map(qc -> qc.getConcursoCargo().getCargo().getId())
                    .collect(Collectors.toSet());
            Set<Long> newCargoIds = new HashSet<>(request.getCargos());
            if (!currentCargoIds.equals(newCargoIds)) return true;
        }

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

    private void synchronizeCargos(Questao questao, List<Long> cargoIds, Long concursoId) {
        java.util.Map<Long, QuestaoCargo> currentMap = questao.getQuestaoCargos().stream()
                .collect(Collectors.toMap(qc -> qc.getConcursoCargo().getCargo().getId(), qc -> qc));

        Set<Long> idsToKeep = new HashSet<>(cargoIds);

        // 1. Remove orphans - leveraging orphanRemoval = true
        questao.getQuestaoCargos().removeIf(qc -> !idsToKeep.contains(qc.getConcursoCargo().getCargo().getId()));

        // 2. Add new
        for (Long cargoId : cargoIds) {
            if (!currentMap.containsKey(cargoId)) {
                ConcursoCargo cc = concursoCargoRepository.findByConcursoIdAndCargoId(concursoId, cargoId)
                        .stream().findFirst()
                        .orElseThrow(() -> new com.studora.exception.ValidationException("O cargo ID " + cargoId + " não pertence ao concurso ID " + concursoId));
                
                QuestaoCargo qc = new QuestaoCargo();
                qc.setConcursoCargo(cc);
                questao.addQuestaoCargo(qc);
            }
        }
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
}