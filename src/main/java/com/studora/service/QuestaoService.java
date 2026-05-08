package com.studora.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoFilter;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.dto.request.SecaoQuestaoRequest;
import com.studora.entity.Alternativa;
import com.studora.entity.ProvaSecao;
import com.studora.entity.Questao;
import com.studora.entity.QuestaoProvaSecao;
import com.studora.entity.Subtema;
import com.studora.exception.ResourceNotFoundException;
import com.studora.mapper.QuestaoMapper;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.ProvaSecaoRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.specification.QuestaoSpecification;
import com.studora.entity.SecaoDisciplina;
import com.studora.repository.SecaoDisciplinaRepository;
import com.studora.dto.request.SecaoDisciplinaRequest;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final ProvaSecaoRepository provaSecaoRepository;
    private final SecaoDisciplinaRepository secaoDisciplinaRepository;

    // =========================================================================
    // READ
    // =========================================================================

    @Transactional(readOnly = true)
    public Page<QuestaoSummaryDto> findAll(QuestaoFilter filter, Pageable pageable) {
        Specification<Questao> spec = QuestaoSpecification.withFilter(filter);
        Page<Questao> page = questaoRepository.findAll(spec, pageable);

        if (page.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> ids = page.getContent().stream().map(Questao::getId).toList();
        List<Questao> withDetails = questaoRepository.findByIdsWithDetails(ids);

        Map<Long, Questao> detailsMap = withDetails.stream()
                .collect(Collectors.toMap(Questao::getId, q -> q));

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
        filter.setCargoId(randomFilter.getCargoId());
        filter.setDisciplinaId(randomFilter.getDisciplinaId());
        filter.setTemaId(randomFilter.getTemaId());
        filter.setSubtemaId(randomFilter.getSubtemaId());
        filter.setInstituicaoArea(randomFilter.getInstituicaoArea());
        filter.setCargoArea(randomFilter.getCargoArea());
        filter.setCargoNivel(randomFilter.getCargoNivel());
        filter.setDesatualizada(false);
        filter.setAnulada(java.util.Objects.requireNonNullElse(randomFilter.getAnulada(), false));

        if (!Boolean.TRUE.equals(randomFilter.getIncludeAutoral())) {
            filter.setAutoral(false);
        }

        Specification<Questao> spec = QuestaoSpecification.withFilter(filter)
                .and(QuestaoSpecification.notAnsweredRecently(java.time.LocalDateTime.now().minusMonths(1)));

        long count = questaoRepository.count(spec);
        if (count == 0) {
            throw new ResourceNotFoundException("Não foi possível encontrar nenhuma questão com os filtros fornecidos.");
        }

        int randomIndex = (int) (Math.random() * count);
        Page<Questao> randomPage = questaoRepository.findAll(spec,
                org.springframework.data.domain.PageRequest.of(randomIndex, 1));

        if (randomPage.hasContent()) {
            return getQuestaoDetailById(randomPage.getContent().get(0).getId());
        }

        throw new ResourceNotFoundException("Não foi possível encontrar nenhuma questão com os filtros fornecidos.");
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    public Long create(QuestaoCreateRequest request) {
        log.info("Criando nova questão");

        boolean isAutoral = Boolean.TRUE.equals(request.getAutoral());
        
        List<Long> subtemaIds = request.getSubtemaIds() != null ? new java.util.ArrayList<>(request.getSubtemaIds()) : new java.util.ArrayList<>();
        if (request.getPrincipalSubtemaId() != null && !subtemaIds.contains(request.getPrincipalSubtemaId())) {
            subtemaIds.add(request.getPrincipalSubtemaId());
        }
        request.setSubtemaIds(subtemaIds);

        validateQuestaoBusinessRules(request.getAlternativas(), request.getAnulada(),
                request.getSecoes(), isAutoral, subtemaIds, request.getPrincipalSubtemaId());

        Questao questao = questaoMapper.toEntity(request);

        if (!subtemaIds.isEmpty()) {
            synchronizeSubtemas(questao, subtemaIds, request.getPrincipalSubtemaId());
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

        if (!isAutoral) {
            synchronizeSecoes(questao, request.getSecoes());
        }

        normalizeAlternativaOrders(questao);

        // Persist first so all QPS rows have real DB IDs before normalization
        Questao saved = questaoRepository.save(questao);
        entityManager.flush();

        // Now renumber every question in the affected prova(s)
        if (!isAutoral && !saved.getSecoes().isEmpty()) {
            collectProvaIds(saved).forEach(this::normalizeQuestaoNumbersForProva);
        }

        return saved.getId();
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    public QuestaoDetailDto update(Long id, QuestaoUpdateRequest request) {
        log.info("Atualizando questão ID: {}", id);

        Questao questao = questaoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", id));

        if (request.getAutoral() != null && !request.getAutoral().equals(questao.getAutoral())) {
            throw new com.studora.exception.ValidationException(
                    "O tipo da questão (autoral/concurso) não pode ser alterado após a criação.");
        }

        boolean isAutoral = Boolean.TRUE.equals(questao.getAutoral());
        
        List<Long> subtemaIds = request.getSubtemaIds() != null ? new java.util.ArrayList<>(request.getSubtemaIds()) : new java.util.ArrayList<>();
        if (request.getPrincipalSubtemaId() != null && !subtemaIds.contains(request.getPrincipalSubtemaId())) {
            subtemaIds.add(request.getPrincipalSubtemaId());
        }
        request.setSubtemaIds(subtemaIds);

        validateQuestaoBusinessRules(request.getAlternativas(), request.getAnulada(),
                request.getSecoes(), isAutoral, subtemaIds, request.getPrincipalSubtemaId());

        boolean contentChanged = hasContentChanged(questao, request, isAutoral);
        if (contentChanged) {
            respostaRepository.deleteByQuestaoId(id);
        }

        questao.setEnunciado(request.getEnunciado());
        questao.setAnulada(request.getAnulada());
        if (request.getDesatualizada() != null) {
            questao.setDesatualizada(request.getDesatualizada());
        }
        questao.setImageUrl(request.getImageUrl());

        if (!subtemaIds.isEmpty()) {
            synchronizeSubtemas(questao, subtemaIds, request.getPrincipalSubtemaId());
        }

        if (!isAutoral) {
            synchronizeSecoes(questao, request.getSecoes());
        }

        // Update alternativas with negative-shift to avoid UNIQUE(questao_id, ordem)
        // conflicts when reordering
        if (request.getAlternativas() != null) {
            List<Alternativa> existing = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(id);

            // Phase 1: negative shift to vacate all ordre slots atomically
            existing.forEach(alt -> alt.setOrdem(-alt.getId().intValue()));
            entityManager.flush();

            Map<Long, Alternativa> existingById = existing.stream()
                    .filter(a -> a.getId() != null)
                    .collect(Collectors.toMap(Alternativa::getId, a -> a));

            Set<Long> requestIds = request.getAlternativas().stream()
                    .filter(r -> ((com.studora.dto.request.AlternativaUpdateRequest) r).getId() != null)
                    .map(r -> ((com.studora.dto.request.AlternativaUpdateRequest) r).getId())
                    .collect(Collectors.toSet());

            // Remove alternativas absent from the request
            questao.getAlternativas().removeIf(
                    alt -> alt.getId() != null && !requestIds.contains(alt.getId()));

            // Phase 2: apply requested ordens
            for (com.studora.dto.request.AlternativaUpdateRequest altReq : request.getAlternativas()) {
                if (altReq.getId() != null && existingById.containsKey(altReq.getId())) {
                    Alternativa alt = existingById.get(altReq.getId());
                    alt.setTexto(altReq.getTexto());
                    alt.setCorreta(altReq.getCorreta());
                    alt.setOrdem(altReq.getOrdem());
                    alt.setJustificativa(altReq.getJustificativa());
                } else {
                    Alternativa newAlt = new Alternativa();
                    newAlt.setQuestao(questao);
                    newAlt.setTexto(altReq.getTexto());
                    newAlt.setCorreta(altReq.getCorreta());
                    newAlt.setOrdem(altReq.getOrdem());
                    newAlt.setJustificativa(altReq.getJustificativa());
                    questao.getAlternativas().add(newAlt);
                }
            }
        }

        normalizeAlternativaOrders(questao);

        Questao saved = questaoRepository.save(questao);
        entityManager.flush();

        // Renumber every question in the affected prova(s)
        if (!isAutoral && !saved.getSecoes().isEmpty()) {
            collectProvaIds(saved).forEach(this::normalizeQuestaoNumbersForProva);
        }

        return questaoMapper.toDetailDto(questao);
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    public void delete(Long id) {
        log.info("Excluindo questão ID: {}", id);

        // Load before deleting so we can capture the affected prova IDs
        Questao questao = questaoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", id));

        Set<Long> provaIds = collectProvaIds(questao);

        questaoRepository.deleteById(id);
        entityManager.flush(); // cascade-deletes QPS rows first

        // Renumber the survivors in each affected prova
        provaIds.forEach(this::normalizeQuestaoNumbersForProva);
    }

    public void toggleDesatualizada(Long id) {
        Questao questao = questaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", id));
        questao.setDesatualizada(!questao.getDesatualizada());
        questaoRepository.save(questao);
    }

    // =========================================================================
    // NORMALIZATION — "Shift Algorithm" (section-ordered, global numbering)
    // =========================================================================

    /**
     * Re-numbers every QuestaoProvaSecao that belongs to the given Prova.
     *
     * The algorithm guarantees that:
     *  • Numbers are strictly sequential across the whole prova, starting at 1.
     *  • The sequence respects ProvaSecao.ordem: all questions of section A
     *    (ordem=1) come before all questions of section B (ordem=2), etc.
     *  • Within a section, the existing relative ordering of questions is
     *    preserved (questions with lower current numeroQuestao come first;
     *    questions with null numeroQuestao are appended at the end of their
     *    section).
     *  • Empty sections simply contribute nothing to the counter — they do not
     *    create gaps.
     *
     * The negative-shift phase prevents UNIQUE constraint violations that would
     * arise if two questions temporarily hold the same numero during the
     * reassignment (e.g. swapping section orders).
     *
     * IMPORTANT: must be called AFTER the triggering entity has been flushed
     * to the database so that all QPS rows have real primary-key IDs.
     */
    private void normalizeQuestaoNumbersForProva(Long provaId) {
        // Load all sections (sorted by ordem ASC) together with their QPS rows
        List<ProvaSecao> sections = provaSecaoRepository.findByProvaIdWithQuestoes(provaId);

        // Build the globally-ordered list: section by section, preserving
        // intra-section order (sort by current numeroQuestao, nulls last)
        List<QuestaoProvaSecao> orderedList = new ArrayList<>();
        for (ProvaSecao section : sections) {
            List<QuestaoProvaSecao> sectionQs = new ArrayList<>(section.getQuestoes());
            sectionQs.sort(Comparator.comparingInt(qps ->
                    qps.getNumeroQuestao() != null ? qps.getNumeroQuestao() : Integer.MAX_VALUE));
            orderedList.addAll(sectionQs);
        }

        if (orderedList.isEmpty()) return;

        // Phase 1 — Negative shift: ensures no two rows share the same positive
        // value while we are mid-reassignment (guards future UNIQUE constraints)
        for (QuestaoProvaSecao qps : orderedList) {
            qps.setNumeroQuestao(-qps.getId().intValue());
        }
        entityManager.flush();

        // Phase 2 — Sequential global assignment, section-ordered
        int globalCounter = 1;
        for (QuestaoProvaSecao qps : orderedList) {
            qps.setNumeroQuestao(globalCounter++);
        }
        // No explicit flush here — the enclosing transaction will commit the values
    }

    /**
     * Collects the IDs of every Prova that contains at least one of the
     * questão's section associations. A questão can theoretically span multiple
     * provas, so we return a Set to handle that edge case.
     */
    private Set<Long> collectProvaIds(Questao questao) {
        return questao.getSecoes().stream()
                .map(qps -> qps.getProvaSecao().getProva().getId())
                .collect(Collectors.toSet());
    }

    // =========================================================================
    // SECTION SYNCHRONIZATION
    // =========================================================================

    /**
     * Adds/removes QuestaoProvaSecao links so that the questão's section
     * memberships match {@code secoesReq}.
     *
     * Deliberately does NOT set numeroQuestao here — that is the exclusive
     * responsibility of {@link #normalizeQuestaoNumbersForProva}.
     */
    private void synchronizeSecoes(Questao questao, List<SecaoQuestaoRequest> secoesReq) {
        if (secoesReq == null || secoesReq.isEmpty()) {
            questao.getSecoes().clear();
            return;
        }

        // Guard: within the same concursoCargo a questão may only belong to one
        // SecaoCargo definition (it can appear in multiple Provas for that cargo,
        // but always in the same thematic section)
        Map<Long, Long> cargoToSecaoDefMap = new java.util.HashMap<>();
        for (SecaoQuestaoRequest req : secoesReq) {
            Long secaoId = req.getSecaoId();
            ProvaSecao ps = provaSecaoRepository.findById(secaoId)
                    .orElseThrow(() -> new ResourceNotFoundException("ProvaSecao", "ID", secaoId));

            if (ps.getProva() == null || ps.getProva().getConcursoCargo() == null
                    || ps.getSecaoCargo() == null) {
                throw new com.studora.exception.ValidationException(
                        "A seção ID " + secaoId
                                + " deve estar vinculada a uma prova com cargo e definição de seção.");
            }

            Long concursoCargoId = ps.getProva().getConcursoCargo().getId();
            Long secaoDefId = ps.getSecaoCargo().getId();
            String cargoNome = ps.getProva().getConcursoCargo().getCargo().getNome();
            String secaoNome = ps.getSecaoCargo().getNome();

            if (cargoToSecaoDefMap.containsKey(concursoCargoId)
                    && !cargoToSecaoDefMap.get(concursoCargoId).equals(secaoDefId)) {
                com.studora.entity.SecaoCargo prevDef = entityManager
                        .find(com.studora.entity.SecaoCargo.class, cargoToSecaoDefMap.get(concursoCargoId));
                String prevName = prevDef != null ? prevDef.getNome() : "outra seção";
                throw new com.studora.exception.ValidationException(
                        "A questão já está vinculada à seção temática '" + prevName
                                + "' para o cargo '" + cargoNome
                                + "'. Não pode ser vinculada a uma segunda seção temática ('"
                                + secaoNome + "') para o mesmo cargo.");
            }
            cargoToSecaoDefMap.put(concursoCargoId, secaoDefId);
        }

        Map<Long, QuestaoProvaSecao> currentMap = questao.getSecoes().stream()
                .collect(Collectors.toMap(qs -> qs.getProvaSecao().getId(), qs -> qs));

        Set<Long> idsToKeep = secoesReq.stream()
                .map(SecaoQuestaoRequest::getSecaoId)
                .collect(Collectors.toSet());

        // Remove associations that are no longer requested
        questao.getSecoes().removeIf(qs -> !idsToKeep.contains(qs.getProvaSecao().getId()));

        // Add new associations or update existing ones
        for (SecaoQuestaoRequest req : secoesReq) {
            QuestaoProvaSecao qps;
            if (!currentMap.containsKey(req.getSecaoId())) {
                ProvaSecao ps = provaSecaoRepository.findById(req.getSecaoId())
                        .orElseThrow(() -> new ResourceNotFoundException("ProvaSecao", "ID", req.getSecaoId()));
                qps = new QuestaoProvaSecao();
                qps.setProvaSecao(ps);
                questao.addSecao(qps);
            } else {
                qps = currentMap.get(req.getSecaoId());
            }

            if (req.getDisciplinaEditalId() != null) {
                com.studora.entity.SecaoDisciplina sd = secaoDisciplinaRepository.findById(req.getDisciplinaEditalId())
                        .orElseThrow(() -> new ResourceNotFoundException("SecaoDisciplina", "ID", req.getDisciplinaEditalId()));
                
                // Validate that the discipline belongs to the section's definition
                if (!sd.getSecaoCargo().getId().equals(qps.getProvaSecao().getSecaoCargo().getId())) {
                    throw new com.studora.exception.ValidationException("A disciplina informada ('" + sd.getNome() + "') não pertence à definição da seção ('" + qps.getProvaSecao().getSecaoCargo().getNome() + "').");
                }
                qps.setSecaoDisciplina(sd);
            } else {
                qps.setSecaoDisciplina(null);
            }
        }
    }

    // =========================================================================
    // SUBTEMA SYNCHRONIZATION
    // =========================================================================

    private void synchronizeSubtemas(Questao questao, List<Long> subtemaIds, Long principalSubtemaId) {
        if (subtemaIds == null) return;

        Map<Long, com.studora.entity.QuestaoSubtema> currentMap = questao.getQuestaoSubtemas().stream()
                .collect(Collectors.toMap(qs -> qs.getSubtema().getId(), qs -> qs));

        Set<Long> idsToKeep = new HashSet<>(subtemaIds);

        // Remove old ones
        boolean principalRemoved = currentMap.containsKey(principalSubtemaId) && !idsToKeep.contains(principalSubtemaId);
        questao.getQuestaoSubtemas().removeIf(qs -> !idsToKeep.contains(qs.getSubtema().getId()));

        // Add/Update
        for (Long subId : subtemaIds) {
            boolean isPrincipal = subId.equals(principalSubtemaId);
            if (!currentMap.containsKey(subId)) {
                Subtema subtema = subtemaRepository.findById(subId)
                        .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", subId));
                questao.addSubtema(subtema, isPrincipal);
            } else {
                currentMap.get(subId).setPrincipal(isPrincipal);
            }
        }

        // Handle cascading principal switch if the original was removed
        if (principalRemoved) {
            List<com.studora.entity.QuestaoSubtema> remaining = questao.getQuestaoSubtemas().stream()
                    .filter(qs -> idsToKeep.contains(qs.getSubtema().getId()))
                    .collect(Collectors.toList());
            
            if (remaining.isEmpty()) {
                throw new com.studora.exception.ValidationException("Não é possível remover todos os subtemas pois a questão deve ter um subtema principal.");
            }

            // Find a valid replacement - in this case, pick the first one remaining.
            // Note: Logic could be expanded to pick best match based on concurso associations if needed.
            remaining.get(0).setPrincipal(true);
        }
    }

    // =========================================================================
    // ALTERNATIVA HELPERS
    // =========================================================================

    /**
     * Sorts alternativas by their current {@code ordem} and re-assigns
     * sequential values 1, 2, 3 … in that order.
     *
     * This does NOT do a negative-shift flush — callers in {@code update()}
     * are expected to have already performed that step before setting the new
     * ordens.  In {@code create()}, alternativas are brand-new (no UNIQUE
     * conflict possible), so no flush is needed.
     */
    private void normalizeAlternativaOrders(Questao questao) {
        if (questao.getAlternativas() == null || questao.getAlternativas().isEmpty()) return;

        List<Alternativa> sorted = new ArrayList<>(questao.getAlternativas());
        sorted.sort(Comparator.comparingInt(
                a -> a.getOrdem() != null ? a.getOrdem() : Integer.MAX_VALUE));

        int counter = 1;
        for (Alternativa alt : sorted) {
            alt.setOrdem(counter++);
        }
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    private void validateQuestaoBusinessRules(
            List<? extends com.studora.dto.request.AlternativaBaseRequest> alternativas,
            Boolean anulada, List<SecaoQuestaoRequest> secoes,
            boolean autoral, List<Long> subtemaIds, Long principalSubtemaId) {

        if (alternativas == null
                || alternativas.size() < com.studora.common.constants.AppConstants.MIN_ALTERNATIVAS) {
            throw new com.studora.exception.ValidationException(
                    "A questão deve ter pelo menos "
                            + com.studora.common.constants.AppConstants.MIN_ALTERNATIVAS
                            + " alternativas");
        }

        if (subtemaIds == null || subtemaIds.isEmpty()) {
            throw new com.studora.exception.ValidationException(
                    "A questão deve estar associada a pelo menos um subtema");
        }

        if (principalSubtemaId == null) {
            throw new com.studora.exception.ValidationException(
                    "A questão deve ter um subtema principal definido");
        }

        if (!subtemaIds.contains(principalSubtemaId)) {
            throw new com.studora.exception.ValidationException(
                    "O subtema principal deve estar entre os subtemas associados à questão");
        }

        if (!autoral) {
            // Association already validated by the main block below
        }

        if (Boolean.FALSE.equals(anulada)) {
            long correctCount = alternativas.stream()
                    .filter(com.studora.dto.request.AlternativaBaseRequest::getCorreta).count();
            if (correctCount != com.studora.common.constants.AppConstants.REQUIRED_CORRECT_ALTERNATIVAS) {
                throw new com.studora.exception.ValidationException(
                        "Uma questão não anulada deve ter exatamente uma alternativa correta");
            }
        }

        if (!autoral && (secoes == null || secoes.isEmpty())) {
            throw new com.studora.exception.ValidationException(
                    "Uma questão de concurso deve estar associada a pelo menos uma seção de prova");
        }

        // Rule: principal subtema must be in the edital of all linked sections/disciplines
        if (!autoral && secoes != null) {
            Subtema principal = subtemaRepository.findById(principalSubtemaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subtema", "ID", principalSubtemaId));

            for (SecaoQuestaoRequest sReq : secoes) {
                ProvaSecao ps = provaSecaoRepository.findById(sReq.getSecaoId())
                        .orElseThrow(() -> new ResourceNotFoundException("ProvaSecao", "ID", sReq.getSecaoId()));

                if (sReq.getDisciplinaEditalId() != null) {
                    com.studora.entity.SecaoDisciplina sd = secaoDisciplinaRepository.findById(sReq.getDisciplinaEditalId())
                            .orElseThrow(() -> new ResourceNotFoundException("SecaoDisciplina", "ID", sReq.getDisciplinaEditalId()));
                    
                    if (!sd.getSubtemas().contains(principal)) {
                        throw new com.studora.exception.ValidationException(
                            "O subtema principal '" + principal.getNome() + 
                            "' não pertence à disciplina '" + sd.getNome() + 
                            "' no edital desta seção.");
                    }
                } else {
                    // Check if it belongs to ANY discipline of the section's definition
                    com.studora.entity.SecaoCargo definition = ps.getSecaoCargo();
                    boolean foundInDefinition = definition.getDisciplinas().stream()
                            .anyMatch(sd -> sd.getSubtemas().contains(principal));
                    
                    if (!foundInDefinition) {
                        throw new com.studora.exception.ValidationException(
                            "O subtema principal '" + principal.getNome() + 
                            "' não está previsto no edital para a seção '" + definition.getNome() + "'.");
                    }
                }
            }
        }
    }

    private boolean hasContentChanged(Questao questao, QuestaoUpdateRequest request, boolean isAutoral) {
        if (!request.getEnunciado().equals(questao.getEnunciado())) return true;
        if (!request.getAnulada().equals(questao.getAnulada())) return true;

        if (request.getSecoes() != null && !isAutoral) {
            Set<Long> currentSecoesIds = questao.getSecoes().stream()
                    .map(qs -> qs.getProvaSecao().getId())
                    .collect(Collectors.toSet());
            Set<Long> newSecoesIds = request.getSecoes().stream()
                    .map(SecaoQuestaoRequest::getSecaoId)
                    .collect(Collectors.toSet());
            if (!currentSecoesIds.equals(newSecoesIds)) return true;
        }

        if (request.getAlternativas().size() != questao.getAlternativas().size()) return true;

        Map<Long, Alternativa> currentMap = questao.getAlternativas().stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(Alternativa::getId, a -> a));

        for (com.studora.dto.request.AlternativaUpdateRequest altReq : request.getAlternativas()) {
            if (altReq.getId() == null) return true;
            Alternativa current = currentMap.get(altReq.getId());
            if (current == null) return true;
            if (!altReq.getTexto().equals(current.getTexto())) return true;
            if (!altReq.getCorreta().equals(current.getCorreta())) return true;

            String reqJust = altReq.getJustificativa() != null ? altReq.getJustificativa() : "";
            String curJust = current.getJustificativa() != null ? current.getJustificativa() : "";
            if (!reqJust.equals(curJust)) return true;
        }

        return false;
    }
}