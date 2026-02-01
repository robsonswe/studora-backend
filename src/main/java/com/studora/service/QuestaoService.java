package com.studora.service;

import com.studora.dto.AlternativaDto;
import com.studora.dto.QuestaoCargoDto;
import com.studora.dto.QuestaoDto;
import com.studora.dto.QuestaoFilter;
import com.studora.entity.*;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.repository.*;
import com.studora.repository.specification.QuestaoSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestaoService {

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private SubtemaRepository subtemaRepository;

    @Autowired
    private ConcursoCargoRepository concursoCargoRepository;

    @Autowired
    private QuestaoCargoRepository questaoCargoRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private RespostaRepository respostaRepository;

    public List<QuestaoDto> getAllQuestoes() {
        return questaoRepository
            .findAll()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<QuestaoDto> search(QuestaoFilter filter) {
        return questaoRepository.findAll(QuestaoSpecification.withFilter(filter))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public QuestaoDto getQuestaoById(Long id) {
        Questao questao = questaoRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Questão", "ID", id)
            );
        return convertToDto(questao);
    }

    @Transactional
    public QuestaoDto createQuestao(QuestaoDto questaoDto) {
        Concurso concurso = concursoRepository
            .findById(questaoDto.getConcursoId())
            .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", questaoDto.getConcursoId()));

        // Normalize orders before validation and processing
        normalizeAlternativaOrders(questaoDto.getAlternativas());

        // Validate that the question has at least one cargo association
        if (questaoDto.getConcursoCargoIds() == null || questaoDto.getConcursoCargoIds().isEmpty()) {
            throw new ValidationException("Uma questão deve estar associada a pelo menos um cargo");
        }

        // Validate that the question has at least 2 alternatives
        if (questaoDto.getAlternativas() == null || questaoDto.getAlternativas().size() < 2) {
            throw new ValidationException("Uma questão deve ter pelo menos 2 alternativas");
        }

        // Validate that exactly one alternative is correct, unless the question is annulled
        if (!Boolean.TRUE.equals(questaoDto.getAnulada())) {
            long correctCount = questaoDto.getAlternativas().stream()
                .map(com.studora.dto.AlternativaDto::getCorreta)
                .filter(Boolean.TRUE::equals)
                .count();
            if (correctCount != 1) {
                throw new ValidationException("Uma questão deve ter exatamente uma alternativa correta");
            }
        }

        Questao questao = convertToEntity(questaoDto);
        questao.setConcurso(concurso);

        if (questaoDto.getSubtemaIds() != null) {
            List<Subtema> subtemas = subtemaRepository.findAllById(
                questaoDto.getSubtemaIds()
            );
            questao.setSubtemas(subtemas);
        }

        Questao savedQuestao = questaoRepository.save(questao);

        // Handle Alternativas
        if (questaoDto.getAlternativas() != null) {
            for (com.studora.dto.AlternativaDto altDto : questaoDto.getAlternativas()) {
                com.studora.entity.Alternativa alternativa = new com.studora.entity.Alternativa();
                alternativa.setQuestao(savedQuestao);
                alternativa.setOrdem(altDto.getOrdem());
                alternativa.setTexto(altDto.getTexto());
                alternativa.setCorreta(altDto.getCorreta());
                alternativa.setJustificativa(altDto.getJustificativa());
                // Save each alternative
                alternativaRepository.save(alternativa);
            }
        }

        // Handle QuestaoCargo associations
        for (Long ccId : questaoDto.getConcursoCargoIds()) {
            ConcursoCargo cc = concursoCargoRepository
                .findById(ccId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("ConcursoCargo", "ID", ccId)
                );

            // Validate that the concurso in the concursoCargo matches the concurso in the questao
            if (!cc.getConcurso().getId().equals(questao.getConcurso().getId())) {
                throw new ValidationException("O concurso do cargo não corresponde ao concurso da questão");
            }

            QuestaoCargo qc = new QuestaoCargo();
            qc.setQuestao(savedQuestao);
            qc.setConcursoCargo(cc);
            questaoCargoRepository.save(qc);
        }

        return convertToDto(savedQuestao);
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
            if (questaoDto.getAlternativas().size() < 2) {
                throw new ValidationException("Uma questão deve ter pelo menos 2 alternativas");
            }

            // Validate that exactly one alternative is correct, unless the question is annulled
            if (!Boolean.TRUE.equals(questaoDto.getAnulada())) {
                long correctCount = questaoDto.getAlternativas().stream()
                    .map(com.studora.dto.AlternativaDto::getCorreta)
                    .filter(Boolean.TRUE::equals)
                    .count();
                if (correctCount != 1) {
                    throw new ValidationException("Uma questão deve ter exatamente uma alternativa correta");
                }
            }
        }

        existingQuestao.setEnunciado(questaoDto.getEnunciado());
        existingQuestao.setAnulada(questaoDto.getAnulada());

        if (questaoDto.getSubtemaIds() != null) {
            existingQuestao.setSubtemas(
                subtemaRepository.findAllById(questaoDto.getSubtemaIds())
            );
        }

        // Clear responses on ANY update, as per the philosophy that any change makes responses outdated
        if (existingQuestao.getRespostas() != null) {
            existingQuestao.getRespostas().clear();
        }

        Questao updatedQuestao = questaoRepository.save(existingQuestao);

        // Handle Alternativas - managed update strategy
        if (questaoDto.getAlternativas() != null) {
            // Fetch current alternatives from DB to be absolutely sure we have the latest state
            List<com.studora.entity.Alternativa> currentAlts = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(id);
            java.util.Map<Integer, com.studora.entity.Alternativa> existingMap = currentAlts.stream()
                .collect(java.util.stream.Collectors.toMap(com.studora.entity.Alternativa::getOrdem, a -> a));

            java.util.Set<Integer> newOrders = questaoDto.getAlternativas().stream()
                .map(com.studora.dto.AlternativaDto::getOrdem)
                .collect(java.util.stream.Collectors.toSet());

            // 1. Remove alternatives that are not in the new list
            for (com.studora.entity.Alternativa alt : currentAlts) {
                if (!newOrders.contains(alt.getOrdem())) {
                    alternativaRepository.delete(alt);
                }
            }
            alternativaRepository.flush();

            // 2. Update existing or add new ones
            for (com.studora.dto.AlternativaDto altDto : questaoDto.getAlternativas()) {
                com.studora.entity.Alternativa existingAlt = existingMap.get(altDto.getOrdem());
                if (existingAlt != null) {
                    // Update existing alternative (same order, so no conflict)
                    existingAlt.setTexto(altDto.getTexto());
                    existingAlt.setCorreta(altDto.getCorreta());
                    existingAlt.setJustificativa(altDto.getJustificativa());
                    alternativaRepository.save(existingAlt);
                } else {
                    // Create new alternative
                    com.studora.entity.Alternativa newAlt = new com.studora.entity.Alternativa();
                    newAlt.setQuestao(updatedQuestao);
                    newAlt.setOrdem(altDto.getOrdem());
                    newAlt.setTexto(altDto.getTexto());
                    newAlt.setCorreta(altDto.getCorreta());
                    newAlt.setJustificativa(altDto.getJustificativa());
                    alternativaRepository.save(newAlt);
                }
            }
            alternativaRepository.flush();
        }

        // Validate that the question still has at least one cargo association after update
        List<QuestaoCargo> currentAssociations = questaoCargoRepository.findByQuestaoId(id);
        if (currentAssociations.isEmpty()) {
            throw new ValidationException("Uma questão deve estar associada a pelo menos um cargo");
        }

        return convertToDto(updatedQuestao);
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
                .map(this::convertQuestaoCargoToDto)
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
        return convertQuestaoCargoToDto(savedQuestaoCargo);
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

    private QuestaoDto convertToDto(Questao questao) {
        QuestaoDto dto = new QuestaoDto();
        dto.setId(questao.getId());
        dto.setConcursoId(questao.getConcurso().getId());
        dto.setEnunciado(questao.getEnunciado());
        dto.setAnulada(questao.getAnulada());

        if (questao.getSubtemas() != null) {
            dto.setSubtemaIds(
                questao
                    .getSubtemas()
                    .stream()
                    .map(Subtema::getId)
                    .collect(Collectors.toList())
            );
        }

        if (questao.getQuestaoCargos() != null) {
            dto.setConcursoCargoIds(
                questao
                    .getQuestaoCargos()
                    .stream()
                    .map(qc -> qc.getConcursoCargo().getId())
                    .collect(Collectors.toList())
            );
        }

        if (questao.getAlternativas() != null && !questao.getAlternativas().isEmpty()) {
            List<com.studora.dto.AlternativaDto> alternativaDtos = questao
                .getAlternativas()
                .stream()
                .map(alt -> {
                    com.studora.dto.AlternativaDto altDto = new com.studora.dto.AlternativaDto();
                    altDto.setId(alt.getId());
                    altDto.setOrdem(alt.getOrdem());
                    altDto.setTexto(alt.getTexto());
                    altDto.setJustificativa(alt.getJustificativa());
                    altDto.setCorreta(alt.getCorreta());
                    return altDto;
                })
                .collect(Collectors.toList());
            dto.setAlternativas(alternativaDtos);
        } else if (questao.getId() != null) {
            // Try to load from repository if list is null or empty (useful in some test scenarios)
            List<com.studora.entity.Alternativa> alts = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(questao.getId());
            if (!alts.isEmpty()) {
                List<com.studora.dto.AlternativaDto> alternativaDtos = alts.stream()
                    .map(alt -> {
                        com.studora.dto.AlternativaDto altDto = new com.studora.dto.AlternativaDto();
                        altDto.setId(alt.getId());
                        altDto.setOrdem(alt.getOrdem());
                        altDto.setTexto(alt.getTexto());
                        altDto.setJustificativa(alt.getJustificativa());
                        altDto.setCorreta(alt.getCorreta());
                        return altDto;
                    })
                    .collect(Collectors.toList());
                dto.setAlternativas(alternativaDtos);
            } else {
                dto.setAlternativas(new ArrayList<>());
            }
        } else {
            dto.setAlternativas(new ArrayList<>());
        }

        return dto;
    }

    private QuestaoCargoDto convertQuestaoCargoToDto(QuestaoCargo questaoCargo) {
        QuestaoCargoDto dto = new QuestaoCargoDto();
        dto.setId(questaoCargo.getId());
        dto.setQuestaoId(questaoCargo.getQuestao().getId());
        dto.setConcursoCargoId(questaoCargo.getConcursoCargo().getId());
        return dto;
    }

    private void normalizeAlternativaOrders(List<AlternativaDto> alternativas) {
        if (alternativas == null || alternativas.isEmpty()) {
            return;
        }

        // Sort by current order (nulls treated as 0) and reassign from 1 to N
        alternativas.sort(java.util.Comparator.comparing(a -> a.getOrdem() == null ? 0 : a.getOrdem()));
        for (int i = 0; i < alternativas.size(); i++) {
            alternativas.get(i).setOrdem(i + 1);
        }
    }

    private Questao convertToEntity(QuestaoDto dto) {
        Questao questao = new Questao();
        questao.setEnunciado(dto.getEnunciado());
        questao.setAnulada(dto.getAnulada() != null ? dto.getAnulada() : false);
        return questao;
    }
}
