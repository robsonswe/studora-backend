package com.studora.service;

import com.studora.dto.AlternativaDto;
import com.studora.entity.Alternativa;
import com.studora.entity.Questao;
import com.studora.exception.ConflictException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.QuestaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlternativaService {

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    public List<AlternativaDto> getAllAlternativas() {
        return alternativaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AlternativaDto getAlternativaById(Long id) {
        Alternativa alternativa = alternativaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", id));
        return convertToDto(alternativa);
    }

    public List<AlternativaDto> getAlternativasByQuestaoId(Long questaoId) {
        return alternativaRepository.findByQuestaoIdOrderByOrdemAsc(questaoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<AlternativaDto> getAlternativasCorretasByQuestaoId(Long questaoId) {
        return alternativaRepository.findByQuestaoIdAndCorretaTrue(questaoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AlternativaDto createAlternativa(AlternativaDto alternativaDto) {
        Questao questao = questaoRepository.findById(alternativaDto.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", alternativaDto.getQuestaoId()));

        // Check for duplicate order within the same question
        List<Alternativa> existingAlternativas = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(alternativaDto.getQuestaoId());
        for (Alternativa alt : existingAlternativas) {
            if (alt.getOrdem().equals(alternativaDto.getOrdem())) {
                throw new ConflictException("Já existe uma alternativa com a ordem " + alternativaDto.getOrdem() + " para a questão com ID: " + alternativaDto.getQuestaoId());
            }
        }

        Alternativa alternativa = convertToEntity(alternativaDto);
        alternativa.setQuestao(questao);

        Alternativa savedAlternativa = alternativaRepository.save(alternativa);
        return convertToDto(savedAlternativa);
    }

    public AlternativaDto updateAlternativa(Long id, AlternativaDto alternativaDto) {
        Alternativa existingAlternativa = alternativaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", id));

        Questao questao = questaoRepository.findById(alternativaDto.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", alternativaDto.getQuestaoId()));

        // Check for duplicate order within the same question (excluding current alternative)
        List<Alternativa> existingAlternativas = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(alternativaDto.getQuestaoId());
        for (Alternativa alt : existingAlternativas) {
            if (alt.getId() != id && alt.getOrdem().equals(alternativaDto.getOrdem())) {
                throw new ConflictException("Já existe uma alternativa com a ordem " + alternativaDto.getOrdem() + " para a questão com ID: " + alternativaDto.getQuestaoId());
            }
        }

        // Check business rule: only one alternative can be marked as correct per question
        if (Boolean.TRUE.equals(alternativaDto.getCorreta())) {
            List<Alternativa> correctAlternatives = alternativaRepository.findByQuestaoIdAndCorretaTrue(alternativaDto.getQuestaoId());
            for (Alternativa alt : correctAlternatives) {
                if (alt.getId() != id) {
                    throw new ValidationException("Já existe uma alternativa marcada como correta para a questão com ID: " + alternativaDto.getQuestaoId());
                }
            }
        }

        // Update fields
        existingAlternativa.setQuestao(questao);
        existingAlternativa.setOrdem(alternativaDto.getOrdem());
        existingAlternativa.setTexto(alternativaDto.getTexto());
        existingAlternativa.setCorreta(alternativaDto.getCorreta());
        existingAlternativa.setJustificativa(alternativaDto.getJustificativa());

        Alternativa updatedAlternativa = alternativaRepository.save(existingAlternativa);
        return convertToDto(updatedAlternativa);
    }

    public void deleteAlternativa(Long id) {
        if (!alternativaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Alternativa", "ID", id);
        }
        alternativaRepository.deleteById(id);
    }

    private AlternativaDto convertToDto(Alternativa alternativa) {
        AlternativaDto dto = new AlternativaDto();
        dto.setId(alternativa.getId());
        dto.setQuestaoId(alternativa.getQuestao().getId());
        dto.setOrdem(alternativa.getOrdem());
        dto.setTexto(alternativa.getTexto());
        dto.setCorreta(alternativa.getCorreta());
        dto.setJustificativa(alternativa.getJustificativa());
        return dto;
    }

    private Alternativa convertToEntity(AlternativaDto dto) {
        Alternativa alternativa = new Alternativa();
        alternativa.setOrdem(dto.getOrdem());
        alternativa.setTexto(dto.getTexto());
        alternativa.setCorreta(dto.getCorreta());
        alternativa.setJustificativa(dto.getJustificativa());
        return alternativa;
    }
}