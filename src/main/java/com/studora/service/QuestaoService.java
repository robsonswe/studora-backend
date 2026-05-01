package com.studora.service;

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
        filter.setCargoId(randomFilter.getCargoId());
        filter.setDisciplinaId(randomFilter.getDisciplinaId());
        filter.setTemaId(randomFilter.getTemaId());
        filter.setSubtemaId(randomFilter.getSubtemaId());
        filter.setInstituicaoArea(randomFilter.getInstituicaoArea());
        filter.setCargoArea(randomFilter.getCargoArea());
        filter.setCargoNivel(randomFilter.getCargoNivel());

        // 1. Force desatualizada to false (not an option for random endpoint)
        filter.setDesatualizada(false);

        // 2. Default anulada to false if not provided
        filter.setAnulada(java.util.Objects.requireNonNullElse(randomFilter.getAnulada(), false));

        // 3. Handle autoral filter: default to standard-only, include both when includeAutoral=true
        if (Boolean.TRUE.equals(randomFilter.getIncludeAutoral())) {
            // Don't set the filter — both types eligible
        } else {
            filter.setAutoral(false); // standard only
        }

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

    public Long create(QuestaoCreateRequest request) {
        log.info("Criando nova questão");

        boolean isAutoral = Boolean.TRUE.equals(request.getAutoral());
        validateQuestaoBusinessRules(request.getAlternativas(), request.getAnulada(), request.getSecoesIds(), isAutoral, request.getSubtemaIds());

        Questao questao = questaoMapper.toEntity(request);


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

        if (!isAutoral) {
            synchronizeSecoes(questao, request.getSecoesIds());
        }

        normalizeAlternativaOrders(questao);
        Questao savedQuestao = questaoRepository.save(questao);

        entityManager.flush();
        return savedQuestao.getId();
    }

    public QuestaoDetailDto update(Long id, QuestaoUpdateRequest request) {
        log.info("Atualizando questão ID: {}", id);

        Questao questao = questaoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", id));

        // Guard: type cannot be changed after creation
        if (request.getAutoral() != null && !request.getAutoral().equals(questao.getAutoral())) {
            throw new com.studora.exception.ValidationException("O tipo da questão (autoral/concurso) não pode ser alterado após a criação.");
        }

        boolean isAutoral = Boolean.TRUE.equals(questao.getAutoral());
        validateQuestaoBusinessRules(request.getAlternativas(), request.getAnulada(), request.getSecoesIds(), isAutoral, request.getSubtemaIds());

        boolean contentChanged = hasContentChanged(questao, request, isAutoral);
        if (contentChanged) {
            log.info("Mudança de conteúdo detectada na questão {}. Excluindo histórico de respostas.", id);
            respostaRepository.deleteByQuestaoId(id);
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

        if (!isAutoral && request.getSecoesIds() != null) {
            synchronizeSecoes(questao, request.getSecoesIds());
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
                                            Boolean anulada, List<Long> secoesIds, boolean autoral,
                                            List<Long> subtemaIds) {
        if (alternativas == null || alternativas.size() < com.studora.common.constants.AppConstants.MIN_ALTERNATIVAS) {
            throw new com.studora.exception.ValidationException("A questão deve ter pelo menos " + com.studora.common.constants.AppConstants.MIN_ALTERNATIVAS + " alternativas");
        }

        if (subtemaIds == null || subtemaIds.isEmpty()) {
            throw new com.studora.exception.ValidationException("A questão deve estar associada a pelo menos um subtema");
        }

        if (Boolean.FALSE.equals(anulada)) {
            long correctCount = alternativas.stream().filter(com.studora.dto.request.AlternativaBaseRequest::getCorreta).count();
            if (correctCount != com.studora.common.constants.AppConstants.REQUIRED_CORRECT_ALTERNATIVAS) {
                throw new com.studora.exception.ValidationException("Uma questão não anulada deve ter exatamente uma alternativa correta");
            }
        }

        // Only enforced for standard (non-autoral) questions
        if (!autoral) {
            if (secoesIds == null || secoesIds.isEmpty()) {
                throw new com.studora.exception.ValidationException("Uma questão de concurso deve estar associada a pelo menos uma seção de prova");
            }
        }
    }

    private boolean hasContentChanged(Questao questao, QuestaoUpdateRequest request, boolean isAutoral) {
        if (!request.getEnunciado().equals(questao.getEnunciado())) return true;
        if (!request.getAnulada().equals(questao.getAnulada())) return true;

        if (request.getSecoesIds() != null && !isAutoral) {
            Set<Long> currentSecoesIds = questao.getSecoes().stream()
                    .map(qs -> qs.getProvaSecao().getId())
                    .collect(Collectors.toSet());
            Set<Long> newSecoesIds = new HashSet<>(request.getSecoesIds());
            if (!currentSecoesIds.equals(newSecoesIds)) return true;
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

    private void synchronizeSecoes(Questao questao, List<Long> secoesIds) {
        if (secoesIds == null || secoesIds.isEmpty()) {
            questao.getSecoes().clear();
            return;
        }

        // Validate: One section per prova per questao
        Map<Long, String> provaToSecaoMap = new java.util.HashMap<>();
        for (Long secaoId : secoesIds) {
            ProvaSecao ps = provaSecaoRepository.findById(secaoId)
                    .orElseThrow(() -> new ResourceNotFoundException("ProvaSecao", "ID", secaoId));
            String secaoNome = ps.getNome() != null ? ps.getNome() : "Sem nome";
            if (ps.getProva() == null) {
                throw new com.studora.exception.ValidationException("A seção '" + secaoNome + "' (ID: " + secaoId + ") não está vinculada a nenhuma prova.");
            }
            Long provaId = ps.getProva().getId();
            if (provaToSecaoMap.containsKey(provaId)) {
                throw new com.studora.exception.ValidationException(
                    "A questão já está vinculada à seção '" + provaToSecaoMap.get(provaId) + 
                    "' desta prova. Não pode ser vinculada à seção '" + secaoNome + "'."
                );
            }
            provaToSecaoMap.put(provaId, secaoNome);
        }

        java.util.Map<Long, QuestaoProvaSecao> currentMap = questao.getSecoes().stream()
                .collect(Collectors.toMap(qs -> qs.getProvaSecao().getId(), qs -> qs));

        Set<Long> idsToKeep = new HashSet<>(secoesIds);

        // 1. Remove orphans - leveraging orphanRemoval = true
        questao.getSecoes().removeIf(qs -> !idsToKeep.contains(qs.getProvaSecao().getId()));

        // 2. Add new
        for (Long secaoId : secoesIds) {
            if (!currentMap.containsKey(secaoId)) {
                ProvaSecao ps = provaSecaoRepository.findById(secaoId)
                        .orElseThrow(() -> new ResourceNotFoundException("ProvaSecao", "ID", secaoId));
                
                QuestaoProvaSecao qps = new QuestaoProvaSecao();
                qps.setProvaSecao(ps);
                questao.addSecao(qps);
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