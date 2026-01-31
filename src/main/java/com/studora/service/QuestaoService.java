package com.studora.service;

import com.studora.dto.AlternativaDto;
import com.studora.dto.QuestaoCargoDto;
import com.studora.dto.QuestaoDto;
import com.studora.entity.*;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.repository.*;
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

    public QuestaoDto getQuestaoById(Long id) {
        Questao questao = questaoRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Questão", "ID", id)
            );
        return convertToDto(questao);
    }

    public List<QuestaoDto> getQuestoesByConcursoId(Long concursoId) {
        return questaoRepository
            .findByConcursoId(concursoId)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<QuestaoDto> getQuestoesBySubtemaId(Long subtemaId) {
        Subtema subtema = subtemaRepository
            .findById(subtemaId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Subtema", "ID", subtemaId)
            );

        return questaoRepository
            .findBySubtemasContaining(subtema)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<QuestaoDto> getQuestoesByTemaId(Long temaId) {
        Tema tema = temaRepository
            .findById(temaId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Tema", "ID", temaId)
            );

        return questaoRepository
            .findBySubtemasTemaId(temaId)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<QuestaoDto> getQuestoesByDisciplinaId(Long disciplinaId) {
        Disciplina disciplina = disciplinaRepository
            .findById(disciplinaId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Disciplina", "ID", disciplinaId)
            );

        return questaoRepository
            .findBySubtemasTemaDisciplinaId(disciplinaId)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<QuestaoDto> getQuestoesAnuladas() {
        return questaoRepository
            .findByAnuladaTrue()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<QuestaoDto> getQuestoesByCargoId(Long cargoId) {
        List<QuestaoCargo> questaoCargos = questaoCargoRepository.findByConcursoCargoId(cargoId);
        List<Long> questaoIds = questaoCargos.stream()
                .map(qc -> qc.getQuestao().getId())
                .collect(Collectors.toList());

        if (questaoIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Questao> questoes = questaoRepository.findAllById(questaoIds);
        return questoes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestaoDto createQuestao(QuestaoDto questaoDto) {
        Concurso concurso = concursoRepository
            .findById(questaoDto.getConcursoId())
            .orElseThrow(() -> new ResourceNotFoundException("Concurso", "ID", questaoDto.getConcursoId()));

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

        Questao updatedQuestao = questaoRepository.save(existingQuestao);

        // Handle Alternativas - delete existing and create new ones
        if (questaoDto.getAlternativas() != null) {
            // Delete existing alternatives (this will cascade delete related respostas)
            List<com.studora.entity.Alternativa> existingAlternativas = existingQuestao.getAlternativas();
            if (existingAlternativas != null) {
                alternativaRepository.deleteAll(existingAlternativas);
            }

            // Create new alternatives
            for (com.studora.dto.AlternativaDto altDto : questaoDto.getAlternativas()) {
                com.studora.entity.Alternativa alternativa = new com.studora.entity.Alternativa();
                alternativa.setQuestao(updatedQuestao);
                alternativa.setOrdem(altDto.getOrdem());
                alternativa.setTexto(altDto.getTexto());
                alternativa.setCorreta(altDto.getCorreta());
                alternativa.setJustificativa(altDto.getJustificativa());
                alternativaRepository.save(alternativa);
            }
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

    // Methods for managing alternatives
    @Transactional
    public AlternativaDto addAlternativaToQuestao(Long questaoId, AlternativaDto alternativaDto) {
        Questao questao = questaoRepository
            .findById(questaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", questaoId));

        // Delete all existing respostas for this question since alternatives are changing
        deleteRespostasByQuestaoId(questaoId);

        // Create new alternative
        com.studora.entity.Alternativa alternativa = new com.studora.entity.Alternativa();
        alternativa.setQuestao(questao);
        alternativa.setOrdem(alternativaDto.getOrdem());
        alternativa.setTexto(alternativaDto.getTexto());
        alternativa.setCorreta(alternativaDto.getCorreta());
        alternativa.setJustificativa(alternativaDto.getJustificativa());

        com.studora.entity.Alternativa savedAlternativa = alternativaRepository.save(alternativa);
        return convertAlternativaToDto(savedAlternativa);
    }

    @Transactional
    public AlternativaDto updateAlternativaFromQuestao(Long questaoId, Long alternativaId, AlternativaDto alternativaDto) {
        Questao questao = questaoRepository
            .findById(questaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", questaoId));

        com.studora.entity.Alternativa alternativa = alternativaRepository
            .findById(alternativaId)
            .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", alternativaId));

        // Verify that the alternative belongs to the specified question
        if (!alternativa.getQuestao().getId().equals(questaoId)) {
            throw new ValidationException("A alternativa não pertence à questão especificada");
        }

        // Delete all existing respostas for this question since alternatives are changing
        deleteRespostasByQuestaoId(questaoId);

        // Update alternative
        alternativa.setOrdem(alternativaDto.getOrdem());
        alternativa.setTexto(alternativaDto.getTexto());
        alternativa.setCorreta(alternativaDto.getCorreta());
        alternativa.setJustificativa(alternativaDto.getJustificativa());

        com.studora.entity.Alternativa updatedAlternativa = alternativaRepository.save(alternativa);
        return convertAlternativaToDto(updatedAlternativa);
    }

    @Transactional
    public void removeAlternativaFromQuestao(Long questaoId, Long alternativaId) {
        Questao questao = questaoRepository
            .findById(questaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", questaoId));

        com.studora.entity.Alternativa alternativa = alternativaRepository
            .findById(alternativaId)
            .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", alternativaId));

        // Verify that the alternative belongs to the specified question
        if (!alternativa.getQuestao().getId().equals(questaoId)) {
            throw new ValidationException("A alternativa não pertence à questão especificada");
        }

        // Delete all existing respostas for this question since alternatives are changing
        deleteRespostasByQuestaoId(questaoId);

        alternativaRepository.delete(alternativa);
    }

    private void deleteRespostasByQuestaoId(Long questaoId) {
        // Delete all respostas for the question since alternatives are changing
        respostaRepository.deleteByQuestaoId(questaoId);
    }

    private AlternativaDto convertAlternativaToDto(com.studora.entity.Alternativa alternativa) {
        AlternativaDto dto = new AlternativaDto();
        dto.setId(alternativa.getId());
        dto.setQuestaoId(alternativa.getQuestao().getId());
        dto.setOrdem(alternativa.getOrdem());
        dto.setTexto(alternativa.getTexto());
        // Don't set correta to hide it in responses
        dto.setJustificativa(alternativa.getJustificativa());
        return dto;
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

        if (questao.getAlternativas() != null) {
            List<com.studora.dto.AlternativaDto> alternativaDtos = questao
                .getAlternativas()
                .stream()
                .map(alt -> {
                    com.studora.dto.AlternativaDto altDto = new com.studora.dto.AlternativaDto();
                    altDto.setId(alt.getId());
                    altDto.setOrdem(alt.getOrdem());
                    altDto.setTexto(alt.getTexto());
                    altDto.setJustificativa(alt.getJustificativa());
                    // Do not set correta field to hide it from GET responses
                    return altDto;
                })
                .collect(Collectors.toList());
            dto.setAlternativas(alternativaDtos);
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

    private Questao convertToEntity(QuestaoDto dto) {
        Questao questao = new Questao();
        questao.setEnunciado(dto.getEnunciado());
        questao.setAnulada(dto.getAnulada() != null ? dto.getAnulada() : false);
        return questao;
    }
}
