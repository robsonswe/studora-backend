package com.studora.service;

import com.studora.dto.RespostaDto;
import com.studora.entity.Alternativa;
import com.studora.entity.Questao;
import com.studora.entity.Resposta;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RespostaService {
    
    @Autowired
    private RespostaRepository respostaRepository;
    
    @Autowired
    private QuestaoRepository questaoRepository;
    
    @Autowired
    private AlternativaRepository alternativaRepository;
    
    public List<RespostaDto> getAllRespostas() {
        return respostaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public RespostaDto getRespostaById(Long id) {
        Resposta resposta = respostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resposta", "ID", id));
        return convertToDto(resposta);
    }
    
    public List<RespostaDto> getRespostasByQuestaoId(Long questaoId) {
        return respostaRepository.findByQuestaoId(questaoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<RespostaDto> getRespostasByAlternativaId(Long alternativaId) {
        return respostaRepository.findByAlternativaEscolhidaId(alternativaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public RespostaDto createResposta(RespostaDto respostaDto) {
        Questao questao = questaoRepository.findById(respostaDto.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", respostaDto.getQuestaoId()));

        // Check if the question is annulled
        if (questao.getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        Alternativa alternativa = alternativaRepository.findById(respostaDto.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", respostaDto.getAlternativaId()));

        Resposta resposta = convertToEntity(respostaDto);
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);

        Resposta savedResposta = respostaRepository.save(resposta);
        return convertToDto(savedResposta);
    }
    
    public RespostaDto updateResposta(Long id, RespostaDto respostaDto) {
        Resposta existingResposta = respostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resposta", "ID", id));

        Questao questao = questaoRepository.findById(respostaDto.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", respostaDto.getQuestaoId()));

        // Check if the question is annulled
        if (questao.getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        Alternativa alternativa = alternativaRepository.findById(respostaDto.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", respostaDto.getAlternativaId()));

        // Update fields
        existingResposta.setQuestao(questao);
        existingResposta.setAlternativaEscolhida(alternativa);
        existingResposta.setRespondidaEm(respostaDto.getRespondidaEm() != null ?
                respostaDto.getRespondidaEm() : LocalDateTime.now());

        Resposta updatedResposta = respostaRepository.save(existingResposta);
        return convertToDto(updatedResposta);
    }
    
    public void deleteResposta(Long id) {
        if (!respostaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resposta", "ID", id);
        }
        respostaRepository.deleteById(id);
    }
    
    private RespostaDto convertToDto(Resposta resposta) {
        RespostaDto dto = new RespostaDto();
        dto.setId(resposta.getId());
        dto.setQuestaoId(resposta.getQuestao().getId());
        dto.setAlternativaId(resposta.getAlternativaEscolhida().getId());
        dto.setRespondidaEm(resposta.getRespondidaEm());
        return dto;
    }
    
    private Resposta convertToEntity(RespostaDto dto) {
        Resposta resposta = new Resposta();
        resposta.setRespondidaEm(dto.getRespondidaEm() != null ? 
                dto.getRespondidaEm() : LocalDateTime.now());
        return resposta;
    }
}