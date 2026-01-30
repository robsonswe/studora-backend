package com.studora.service;

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

        Questao questao = convertToEntity(questaoDto);
        questao.setConcurso(concurso);

        if (questaoDto.getSubtemaIds() != null) {
            List<Subtema> subtemas = subtemaRepository.findAllById(
                questaoDto.getSubtemaIds()
            );
            questao.setSubtemas(subtemas);
        }

        Questao savedQuestao = questaoRepository.save(questao);

        // Handle QuestaoCargo associations
        for (Long ccId : questaoDto.getConcursoCargoIds()) {
            ConcursoCargo cc = concursoCargoRepository
                .findById(ccId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("ConcursoCargo", "ID", ccId)
                );
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

        existingQuestao.setEnunciado(questaoDto.getEnunciado());
        existingQuestao.setAnulada(questaoDto.getAnulada());

        if (questaoDto.getSubtemaIds() != null) {
            existingQuestao.setSubtemas(
                subtemaRepository.findAllById(questaoDto.getSubtemaIds())
            );
        }

        Questao updatedQuestao = questaoRepository.save(existingQuestao);

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
