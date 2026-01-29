package com.studora.service;

import com.studora.dto.QuestaoDto;
import com.studora.entity.Concurso;
import com.studora.entity.Questao;
import com.studora.entity.Subtema;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.SubtemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestaoService {
    
    @Autowired
    private QuestaoRepository questaoRepository;
    
    @Autowired
    private ConcursoRepository concursoRepository;
    
    @Autowired
    private SubtemaRepository subtemaRepository;
    
    public List<QuestaoDto> getAllQuestoes() {
        return questaoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public QuestaoDto getQuestaoById(Long id) {
        Questao questao = questaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada com ID: " + id));
        return convertToDto(questao);
    }
    
    public List<QuestaoDto> getQuestoesByConcursoId(Long concursoId) {
        return questaoRepository.findByConcursoId(concursoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<QuestaoDto> getQuestoesBySubtemaId(Long subtemaId) {
        Subtema subtema = subtemaRepository.findById(subtemaId)
                .orElseThrow(() -> new RuntimeException("Subtema não encontrado com ID: " + subtemaId));
        
        return questaoRepository.findBySubtemasContaining(subtema).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<QuestaoDto> getQuestoesNaoAnuladas() {
        return questaoRepository.findByAnuladaFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public QuestaoDto createQuestao(QuestaoDto questaoDto) {
        Concurso concurso = concursoRepository.findById(questaoDto.getConcursoId())
                .orElseThrow(() -> new RuntimeException("Concurso não encontrado com ID: " + questaoDto.getConcursoId()));
        
        Questao questao = convertToEntity(questaoDto);
        questao.setConcurso(concurso);
        
        // Associate subtemas if provided
        if (questaoDto.getSubtemaIds() != null && !questaoDto.getSubtemaIds().isEmpty()) {
            List<Subtema> subtemas = new ArrayList<>();
            for (Long subtemaId : questaoDto.getSubtemaIds()) {
                Subtema subtema = subtemaRepository.findById(subtemaId)
                        .orElseThrow(() -> new RuntimeException("Subtema não encontrado com ID: " + subtemaId));
                subtemas.add(subtema);
            }
            questao.setSubtemas(subtemas);
        }
        
        Questao savedQuestao = questaoRepository.save(questao);
        return convertToDto(savedQuestao);
    }
    
    public QuestaoDto updateQuestao(Long id, QuestaoDto questaoDto) {
        Questao existingQuestao = questaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Questão não encontrada com ID: " + id));
        
        Concurso concurso = concursoRepository.findById(questaoDto.getConcursoId())
                .orElseThrow(() -> new RuntimeException("Concurso não encontrado com ID: " + questaoDto.getConcursoId()));
        
        // Update fields
        existingQuestao.setConcurso(concurso);
        existingQuestao.setEnunciado(questaoDto.getEnunciado());
        existingQuestao.setAnulada(questaoDto.getAnulada());
        
        // Update subtemas association
        if (questaoDto.getSubtemaIds() != null) {
            List<Subtema> subtemas = new ArrayList<>();
            for (Long subtemaId : questaoDto.getSubtemaIds()) {
                Subtema subtema = subtemaRepository.findById(subtemaId)
                        .orElseThrow(() -> new RuntimeException("Subtema não encontrado com ID: " + subtemaId));
                subtemas.add(subtema);
            }
            existingQuestao.setSubtemas(subtemas);
        }
        
        Questao updatedQuestao = questaoRepository.save(existingQuestao);
        return convertToDto(updatedQuestao);
    }
    
    public void deleteQuestao(Long id) {
        if (!questaoRepository.existsById(id)) {
            throw new RuntimeException("Questão não encontrada com ID: " + id);
        }
        questaoRepository.deleteById(id);
    }
    
    private QuestaoDto convertToDto(Questao questao) {
        QuestaoDto dto = new QuestaoDto();
        dto.setId(questao.getId());
        dto.setConcursoId(questao.getConcurso().getId());
        dto.setEnunciado(questao.getEnunciado());
        dto.setAnulada(questao.getAnulada());
        
        // Map subtema IDs
        if (questao.getSubtemas() != null) {
            List<Long> subtemaIds = questao.getSubtemas().stream()
                    .map(Subtema::getId)
                    .collect(Collectors.toList());
            dto.setSubtemaIds(subtemaIds);
        }
        
        return dto;
    }
    
    private Questao convertToEntity(QuestaoDto dto) {
        Questao questao = new Questao();
        questao.setEnunciado(dto.getEnunciado());
        questao.setAnulada(dto.getAnulada());
        return questao;
    }
}