package com.studora.service;

import com.studora.dto.AlternativaDto;
import com.studora.dto.RespostaComAlternativasDto;
import com.studora.dto.RespostaDto;
import com.studora.dto.request.RespostaCreateRequest;
import com.studora.dto.request.RespostaUpdateRequest;
import com.studora.entity.Alternativa;
import com.studora.entity.Questao;
import com.studora.entity.Resposta;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<RespostaDto> findAll(Pageable pageable) {
        return respostaRepository.findAll(pageable)
                .map(this::convertToDto);
    }
    
    public RespostaDto getRespostaById(Long id) {
        Resposta resposta = respostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resposta", "ID", id));
        return convertToDto(resposta);
    }
    
    public RespostaDto getRespostaByQuestaoId(Long questaoId) {
        Resposta resposta = respostaRepository.findByQuestaoId(questaoId);
        if (resposta != null) {
            return convertToDto(resposta);
        } else {
            throw new ResourceNotFoundException("Resposta", "ID da Questão", questaoId);
        }
    }
    
    public RespostaDto createResposta(RespostaCreateRequest respostaCreateRequest) {
        Questao questao = questaoRepository.findById(respostaCreateRequest.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", respostaCreateRequest.getQuestaoId()));

        // Check if the question is annulled
        if (questao.getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        // Check if a response already exists for this question (business rule: only one response per question)
        Resposta existingResposta = respostaRepository.findByQuestaoId(respostaCreateRequest.getQuestaoId());
        if (existingResposta != null) {
            throw new ValidationException("Já existe uma resposta para esta questão. Use a operação de atualização para modificar a resposta.");
        }

        Alternativa alternativa = alternativaRepository.findById(respostaCreateRequest.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", respostaCreateRequest.getAlternativaId()));

        Resposta resposta = convertToEntityFromRequest(respostaCreateRequest);
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);

        Resposta savedResposta = respostaRepository.save(resposta);
        return convertToDto(savedResposta);
    }
    
    public RespostaDto updateResposta(Long id, RespostaUpdateRequest respostaUpdateRequest) {
        Resposta existingResposta = respostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resposta", "ID", id));

        // Only allow updating the alternativaId, questaoId should remain unchanged
        Alternativa alternativa = alternativaRepository.findById(respostaUpdateRequest.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", respostaUpdateRequest.getAlternativaId()));

        // Check if the question is annulled
        if (existingResposta.getQuestao().getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        // Update only the alternativaId and update the timestamp
        existingResposta.setAlternativaEscolhida(alternativa);
        existingResposta.setRespondidaEm(LocalDateTime.now()); // Automatically update timestamp

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
        dto.setCorreta(resposta.getAlternativaEscolhida().getCorreta()); // Include the correct field
        dto.setRespondidaEm(resposta.getRespondidaEm());
        return dto;
    }
    
    private Resposta convertToEntity(RespostaDto dto) {
        Resposta resposta = new Resposta();
        resposta.setRespondidaEm(dto.getRespondidaEm() != null ?
                dto.getRespondidaEm() : LocalDateTime.now());
        return resposta;
    }

    private Resposta convertToEntityFromRequest(RespostaCreateRequest request) {
        Resposta resposta = new Resposta();
        resposta.setRespondidaEm(LocalDateTime.now()); // Always set the timestamp automatically
        return resposta;
    }

    public RespostaComAlternativasDto createRespostaWithAlternativas(RespostaCreateRequest respostaCreateRequest) {
        Questao questao = questaoRepository.findById(respostaCreateRequest.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", respostaCreateRequest.getQuestaoId()));

        // Check if the question is annulled
        if (questao.getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        // Check if a response already exists for this question (business rule: only one response per question)
        Resposta existingResposta = respostaRepository.findByQuestaoId(respostaCreateRequest.getQuestaoId());
        if (existingResposta != null) {
            throw new ValidationException("Já existe uma resposta para esta questão. Use a operação de atualização para modificar a resposta.");
        }

        Alternativa alternativa = alternativaRepository.findById(respostaCreateRequest.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", respostaCreateRequest.getAlternativaId()));

        Resposta resposta = convertToEntityFromRequest(respostaCreateRequest);
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);

        Resposta savedResposta = respostaRepository.save(resposta);
        return convertToRespostaComAlternativasDto(savedResposta);
    }

    public RespostaComAlternativasDto updateRespostaWithAlternativas(Long id, RespostaUpdateRequest respostaUpdateRequest) {
        Resposta existingResposta = respostaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resposta", "ID", id));

        // Only allow updating the alternativaId, questaoId should remain unchanged
        Alternativa alternativa = alternativaRepository.findById(respostaUpdateRequest.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", respostaUpdateRequest.getAlternativaId()));

        // Check if the question is annulled
        if (existingResposta.getQuestao().getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        // Update only the alternativaId and update the timestamp
        existingResposta.setAlternativaEscolhida(alternativa);
        existingResposta.setRespondidaEm(LocalDateTime.now()); // Automatically update timestamp

        Resposta updatedResposta = respostaRepository.save(existingResposta);
        return convertToRespostaComAlternativasDto(updatedResposta);
    }

    private RespostaComAlternativasDto convertToRespostaComAlternativasDto(Resposta resposta) {
        RespostaComAlternativasDto dto = new RespostaComAlternativasDto();
        dto.setId(resposta.getId());
        dto.setQuestaoId(resposta.getQuestao().getId());
        dto.setAlternativaId(resposta.getAlternativaEscolhida().getId());
        dto.setCorreta(resposta.getAlternativaEscolhida().getCorreta());
        dto.setRespondidaEm(resposta.getRespondidaEm());

        // Get all alternatives for the question
        List<Alternativa> allAlternativas = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(resposta.getQuestao().getId());
        List<AlternativaDto> alternativasDto = allAlternativas.stream()
                .map(this::convertAlternativaToDto)
                .collect(Collectors.toList());
        dto.setAlternativas(alternativasDto);

        return dto;
    }

    private AlternativaDto convertAlternativaToDto(Alternativa alternativa) {
        AlternativaDto dto = new AlternativaDto();
        dto.setId(alternativa.getId());
        dto.setOrdem(alternativa.getOrdem());
        dto.setTexto(alternativa.getTexto());
        dto.setJustificativa(alternativa.getJustificativa());
        dto.setCorreta(alternativa.getCorreta()); // Include the correct field
        return dto;
    }
}