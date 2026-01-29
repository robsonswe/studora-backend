package com.studora.service;

import com.studora.dto.QuestaoDto;
import com.studora.entity.*;
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
                new RuntimeException("Questão não encontrada com ID: " + id)
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
                new RuntimeException(
                    "Subtema não encontrado com ID: " + subtemaId
                )
            );

        return questaoRepository
            .findBySubtemasContaining(subtema)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    public List<QuestaoDto> getQuestoesNaoAnuladas() {
        return questaoRepository
            .findByAnuladaFalse()
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public QuestaoDto createQuestao(QuestaoDto questaoDto) {
        Concurso concurso = concursoRepository
            .findById(questaoDto.getConcursoId())
            .orElseThrow(() -> new RuntimeException("Concurso não encontrado"));

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
        if (questaoDto.getConcursoCargoIds() != null) {
            for (Long ccId : questaoDto.getConcursoCargoIds()) {
                ConcursoCargo cc = concursoCargoRepository
                    .findById(ccId)
                    .orElseThrow(() ->
                        new RuntimeException("ConcursoCargo not found: " + ccId)
                    );
                QuestaoCargo qc = new QuestaoCargo();
                qc.setQuestao(savedQuestao);
                qc.setConcursoCargo(cc);
                questaoCargoRepository.save(qc);
            }
        }

        return convertToDto(savedQuestao);
    }

    @Transactional
    public QuestaoDto updateQuestao(Long id, QuestaoDto questaoDto) {
        Questao existingQuestao = questaoRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Questão não encontrada"));

        existingQuestao.setEnunciado(questaoDto.getEnunciado());
        existingQuestao.setAnulada(questaoDto.getAnulada());

        if (questaoDto.getSubtemaIds() != null) {
            existingQuestao.setSubtemas(
                subtemaRepository.findAllById(questaoDto.getSubtemaIds())
            );
        }

        Questao updatedQuestao = questaoRepository.save(existingQuestao);
        return convertToDto(updatedQuestao);
    }

    public void deleteQuestao(Long id) {
        questaoRepository.deleteById(id);
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

    private Questao convertToEntity(QuestaoDto dto) {
        Questao questao = new Questao();
        questao.setEnunciado(dto.getEnunciado());
        questao.setAnulada(dto.getAnulada() != null ? dto.getAnulada() : false);
        return questao;
    }
}
