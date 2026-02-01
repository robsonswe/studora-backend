package com.studora.service;

import com.studora.dto.AlternativaDto;
import com.studora.dto.QuestaoCargoDto;
import com.studora.dto.QuestaoDto;
import com.studora.dto.QuestaoFilter;
import com.studora.entity.*;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.AlternativaMapper;
import com.studora.mapper.QuestaoCargoMapper;
import com.studora.mapper.QuestaoMapper;
import com.studora.repository.*;
import com.studora.repository.specification.QuestaoSpecification;
import com.studora.util.QuestaoValidationConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestaoService {

    private final QuestaoRepository questaoRepository;
    private final ConcursoRepository concursoRepository;
    private final SubtemaRepository subtemaRepository;
    private final ConcursoCargoRepository concursoCargoRepository;
    private final QuestaoCargoRepository questaoCargoRepository;
    private final TemaRepository temaRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final AlternativaRepository alternativaRepository;
    private final RespostaRepository respostaRepository;
    private final QuestaoMapper questaoMapper;
    private final AlternativaMapper alternativaMapper;
    private final QuestaoCargoMapper questaoCargoMapper;

    public List<QuestaoDto> getAllQuestoes() {
        return questaoRepository
            .findAll()
            .stream()
            .map(questaoMapper::toDto)
            .collect(Collectors.toList());
    }

    public Page<QuestaoDto> search(QuestaoFilter filter, Pageable pageable) {
        return questaoRepository.findAll(QuestaoSpecification.withFilter(filter), pageable)
                .map(questaoMapper::toDto);
    }

    public QuestaoDto getQuestaoById(Long id) {
        Questao questao = questaoRepository
            .findByIdWithDetails(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Questão", "ID", id)
            );
        return questaoMapper.toDto(questao);
    }

    @Transactional
    public QuestaoDto createQuestao(QuestaoDto questaoDto) {
        Concurso concurso = concursoRepository
            .findById(questaoDto.getConcursoId())
            .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", questaoDto.getConcursoId()));

        // Normalize orders before validation and processing
        normalizeAlternativaOrders(questaoDto.getAlternativas());

        // Validate that the question has at least one cargo association
        if (questaoDto.getConcursoCargoIds() == null || questaoDto.getConcursoCargoIds().size() < QuestaoValidationConstants.MIN_CARGO_ASSOCIATIONS) {
            throw new ValidationException("Uma questão deve estar associada a pelo menos " + QuestaoValidationConstants.MIN_CARGO_ASSOCIATIONS + " cargo");
        }

        // Validate that the question has at least 2 alternatives
        if (questaoDto.getAlternativas() == null || questaoDto.getAlternativas().size() < QuestaoValidationConstants.MIN_ALTERNATIVAS) {
            throw new ValidationException("Uma questão deve ter pelo menos " + QuestaoValidationConstants.MIN_ALTERNATIVAS + " alternativas");
        }

        // Validate that exactly one alternative is correct, unless the question is annulled
        if (!Boolean.TRUE.equals(questaoDto.getAnulada())) {
            long correctCount = questaoDto.getAlternativas().stream()
                .map(com.studora.dto.AlternativaDto::getCorreta)
                .filter(Boolean.TRUE::equals)
                .count();
            if (correctCount != QuestaoValidationConstants.REQUIRED_CORRECT_ALTERNATIVAS) {
                throw new ValidationException("Uma questão deve ter exatamente " + QuestaoValidationConstants.REQUIRED_CORRECT_ALTERNATIVAS + " alternativa correta");
            }
        }

        // PRE-VALIDATE ConcursoCargo associations before saving Questao
        List<ConcursoCargo> validatedCargos = new ArrayList<>();
        for (Long ccId : questaoDto.getConcursoCargoIds()) {
            ConcursoCargo cc = concursoCargoRepository
                .findById(ccId)
                .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "ID", ccId));

            if (!cc.getConcurso().getId().equals(concurso.getId())) {
                throw new ValidationException("O concurso do cargo não corresponde ao concurso da questão");
            }
            validatedCargos.add(cc);
        }

        Questao questao = questaoMapper.toEntity(questaoDto);
        questao.setConcurso(concurso);

        if (questaoDto.getSubtemaIds() != null) {
            List<Subtema> subtemas = subtemaRepository.findAllById(
                questaoDto.getSubtemaIds()
            );
            questao.setSubtemas(new java.util.LinkedHashSet<>(subtemas));
        }

        Questao savedQuestao = questaoRepository.save(questao);

        // Handle Alternativas
        if (questaoDto.getAlternativas() != null) {
            for (com.studora.dto.AlternativaDto altDto : questaoDto.getAlternativas()) {
                com.studora.entity.Alternativa alternativa = alternativaMapper.toEntity(altDto);
                alternativa.setQuestao(savedQuestao);
                com.studora.entity.Alternativa savedAlt = alternativaRepository.save(alternativa);
                savedQuestao.getAlternativas().add(savedAlt);
            }
        }

        // Handle QuestaoCargo associations (using pre-validated list)
        for (ConcursoCargo cc : validatedCargos) {
            QuestaoCargo qc = new QuestaoCargo();
            qc.setQuestao(savedQuestao);
            qc.setConcursoCargo(cc);
            QuestaoCargo savedQc = questaoCargoRepository.save(qc);
            savedQuestao.getQuestaoCargos().add(savedQc);
        }

        return questaoMapper.toDto(savedQuestao);
    }

    @Transactional
    public QuestaoDto updateQuestao(Long id, QuestaoDto questaoDto) {
        Questao existingQuestao = questaoRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", id));

        // Normalize orders before validation and processing
        normalizeAlternativaOrders(questaoDto.getAlternativas());

        // Validate that the question has at least 2 alternatives
        if (questaoDto.getAlternativas() != null) {
            if (questaoDto.getAlternativas().size() < QuestaoValidationConstants.MIN_ALTERNATIVAS) {
                throw new ValidationException("Uma questão deve ter pelo menos " + QuestaoValidationConstants.MIN_ALTERNATIVAS + " alternativas");
            }

            if (!Boolean.TRUE.equals(questaoDto.getAnulada())) {
                long correctCount = questaoDto.getAlternativas().stream()
                    .map(com.studora.dto.AlternativaDto::getCorreta)
                    .filter(Boolean.TRUE::equals)
                    .count();
                if (correctCount != QuestaoValidationConstants.REQUIRED_CORRECT_ALTERNATIVAS) {
                    throw new ValidationException("Uma questão deve ter exatamente " + QuestaoValidationConstants.REQUIRED_CORRECT_ALTERNATIVAS + " alternativa correta");
                }
            }
        }

        questaoMapper.updateEntityFromDto(questaoDto, existingQuestao);

        if (questaoDto.getSubtemaIds() != null) {
            existingQuestao.setSubtemas(
                new java.util.LinkedHashSet<>(subtemaRepository.findAllById(questaoDto.getSubtemaIds()))
            );
        }

        // Explicitly clear response using repository for reliability (Critical Bug fix)
        respostaRepository.deleteByQuestaoId(id);

        Questao updatedQuestao = questaoRepository.save(existingQuestao);

        // Handle Alternativas - managed update strategy
        if (questaoDto.getAlternativas() != null) {
            List<com.studora.entity.Alternativa> currentAlts = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(id);
            java.util.Map<Long, com.studora.entity.Alternativa> existingMap = currentAlts.stream()
                .collect(java.util.stream.Collectors.toMap(com.studora.entity.Alternativa::getId, a -> a));

            java.util.Set<Long> idsToKeep = questaoDto.getAlternativas().stream()
                .map(com.studora.dto.AlternativaDto::getId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

            for (com.studora.entity.Alternativa alt : currentAlts) {
                if (!idsToKeep.contains(alt.getId())) {
                    updatedQuestao.getAlternativas().remove(alt);
                    alternativaRepository.delete(alt);
                }
            }
            alternativaRepository.flush();

            for (com.studora.dto.AlternativaDto altDto : questaoDto.getAlternativas()) {
                com.studora.entity.Alternativa existingAlt = null;
                if (altDto.getId() != null) {
                    existingAlt = existingMap.get(altDto.getId());
                }

                if (existingAlt != null) {
                    alternativaMapper.updateEntityFromDto(altDto, existingAlt);
                    alternativaRepository.save(existingAlt);
                    if (!updatedQuestao.getAlternativas().contains(existingAlt)) {
                        updatedQuestao.getAlternativas().add(existingAlt);
                    }
                } else {
                    com.studora.entity.Alternativa newAlt = alternativaMapper.toEntity(altDto);
                    newAlt.setQuestao(updatedQuestao);
                    com.studora.entity.Alternativa savedAlt = alternativaRepository.save(newAlt);
                    updatedQuestao.getAlternativas().add(savedAlt);
                }
            }
            alternativaRepository.flush();
        }

        // HANDLE ConcursoCargo UPDATES (Critical Bug fix)
        if (questaoDto.getConcursoCargoIds() != null) {
            // 1. Validate that all requested cargos belong to the same concurso
            List<ConcursoCargo> validatedCargos = new ArrayList<>();
            for (Long ccId : questaoDto.getConcursoCargoIds()) {
                ConcursoCargo cc = concursoCargoRepository.findById(ccId)
                    .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "ID", ccId));
                
                if (!cc.getConcurso().getId().equals(updatedQuestao.getConcurso().getId())) {
                    throw new ValidationException("O concurso do cargo não corresponde ao concurso da questão");
                }
                validatedCargos.add(cc);
            }

            // 2. Sync associations
            List<QuestaoCargo> currentAssociations = questaoCargoRepository.findByQuestaoId(id);
            java.util.Map<Long, QuestaoCargo> existingAssocMap = currentAssociations.stream()
                .collect(java.util.stream.Collectors.toMap(qc -> qc.getConcursoCargo().getId(), qc -> qc));

            java.util.Set<Long> idsToKeep = new java.util.HashSet<>(questaoDto.getConcursoCargoIds());

            // Remove associations not in the new list
            for (QuestaoCargo qc : currentAssociations) {
                if (!idsToKeep.contains(qc.getConcursoCargo().getId())) {
                    updatedQuestao.getQuestaoCargos().remove(qc);
                    questaoCargoRepository.delete(qc);
                }
            }

            // Add new associations
            for (ConcursoCargo cc : validatedCargos) {
                if (!existingAssocMap.containsKey(cc.getId())) {
                    QuestaoCargo qc = new QuestaoCargo();
                    qc.setQuestao(updatedQuestao);
                    qc.setConcursoCargo(cc);
                    QuestaoCargo savedQc = questaoCargoRepository.save(qc);
                    updatedQuestao.getQuestaoCargos().add(savedQc);
                }
            }
            questaoCargoRepository.flush();
        }

        // Final validation: ensure at least one cargo remains
        if (questaoCargoRepository.findByQuestaoId(id).size() < QuestaoValidationConstants.MIN_CARGO_ASSOCIATIONS) {
            throw new ValidationException("Uma questão deve estar associada a pelo menos " + QuestaoValidationConstants.MIN_CARGO_ASSOCIATIONS + " cargo");
        }

        return questaoMapper.toDto(updatedQuestao);
    }

    @Transactional
    public void deleteQuestao(Long id) {
        if (!questaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Questão", "ID", id);
        }

        // First, remove all cargo associations for this questao
        List<QuestaoCargo> questaoCargos = questaoCargoRepository.findByQuestaoId(id);
        questaoCargoRepository.deleteAll(questaoCargos);

        // Delete the question (this should cascade delete alternatives and related respostas)
        questaoRepository.deleteById(id);
    }


    // Methods for managing cargo associations
    public List<QuestaoCargoDto> getCargosByQuestaoId(Long questaoId) {
        List<QuestaoCargo> questaoCargos = questaoCargoRepository.findByQuestaoId(questaoId);
        return questaoCargos.stream()
                .map(questaoCargoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestaoCargoDto addCargoToQuestao(QuestaoCargoDto questaoCargoDto) {
        // Check if the association already exists
        List<QuestaoCargo> existingAssociations = questaoCargoRepository
                .findByQuestaoIdAndConcursoCargoId(questaoCargoDto.getQuestaoId(), questaoCargoDto.getConcursoCargoId());

        if (!existingAssociations.isEmpty()) {
            throw new ValidationException("Cargo já associado à questão");
        }

        Questao questao = questaoRepository.findById(questaoCargoDto.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", questaoCargoDto.getQuestaoId()));

        ConcursoCargo concursoCargo = concursoCargoRepository.findById(questaoCargoDto.getConcursoCargoId())
                .orElseThrow(() -> new ResourceNotFoundException("ConcursoCargo", "ID", questaoCargoDto.getConcursoCargoId()));

        // Validate that the concurso in the concursoCargo matches the concurso in the questao
        if (!concursoCargo.getConcurso().getId().equals(questao.getConcurso().getId())) {
            throw new ValidationException("O concurso do cargo não corresponde ao concurso da questão");
        }

        QuestaoCargo questaoCargo = new QuestaoCargo();
        questaoCargo.setQuestao(questao);
        questaoCargo.setConcursoCargo(concursoCargo);

        QuestaoCargo savedQuestaoCargo = questaoCargoRepository.save(questaoCargo);
        return questaoCargoMapper.toDto(savedQuestaoCargo);
    }

    @Transactional
    public void removeCargoFromQuestao(Long questaoId, Long concursoCargoId) {
        List<QuestaoCargo> questaoCargos = questaoCargoRepository
                .findByQuestaoIdAndConcursoCargoId(questaoId, concursoCargoId);

        if (questaoCargos.isEmpty()) {
            throw new ResourceNotFoundException("Associação entre questão e cargo não encontrada");
        }

        // Check if removing this association would leave the question with no cargo associations
        List<QuestaoCargo> currentAssociations = questaoCargoRepository.findByQuestaoId(questaoId);
        if (currentAssociations.size() <= 1) {
            throw new ValidationException("Uma questão deve estar associada a pelo menos um cargo");
        }

        // Delete the association
        questaoCargoRepository.deleteAll(questaoCargos);
    }

    private void normalizeAlternativaOrders(List<com.studora.dto.AlternativaDto> alternativas) {
        if (alternativas == null || alternativas.isEmpty()) {
            return;
        }

        // Sort by current order (nulls treated as 0) and reassign from 1 to N
        alternativas.sort(java.util.Comparator.comparing(a -> a.getOrdem() == null ? 0 : a.getOrdem()));
        for (int i = 0; i < alternativas.size(); i++) {
            alternativas.get(i).setOrdem(i + 1);
        }
    }
}
