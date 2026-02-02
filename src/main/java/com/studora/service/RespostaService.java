package com.studora.service;

import com.studora.dto.RespostaComAlternativasDto;
import com.studora.dto.RespostaDto;
import com.studora.dto.request.RespostaCreateRequest;
import com.studora.dto.request.RespostaUpdateRequest;
import com.studora.entity.Alternativa;
import com.studora.entity.Questao;
import com.studora.entity.Resposta;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.RespostaMapper;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RespostaService {

    private final RespostaRepository respostaRepository;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final RespostaMapper respostaMapper;

    public Page<RespostaDto> findAll(Pageable pageable) {
        return respostaRepository.findAll(pageable)
                .map(respostaMapper::toDto);
    }
    
    public RespostaDto getRespostaById(Long id) {
        Resposta resposta = respostaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resposta", "ID", id));
        return respostaMapper.toDto(resposta);
    }
    
    public RespostaDto getRespostaByQuestaoId(Long questaoId) {
        return respostaRepository.findByQuestaoIdWithDetails(questaoId)
                .map(respostaMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Resposta", "ID da Questão", questaoId));
    }
    
    public RespostaDto createResposta(RespostaCreateRequest respostaCreateRequest) {
        log.info("Criando nova resposta para a questão ID: {}", respostaCreateRequest.getQuestaoId());
        Questao questao = questaoRepository.findById(respostaCreateRequest.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", respostaCreateRequest.getQuestaoId()));

        // Check if the question is annulled
        if (questao.getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        // Check if a response already exists for this question
        if (respostaRepository.findByQuestaoId(respostaCreateRequest.getQuestaoId()) != null) {
            throw new ValidationException("Já existe uma resposta para esta questão. Use a operação de atualização para modificar a resposta.");
        }

        Alternativa alternativa = alternativaRepository.findById(respostaCreateRequest.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", respostaCreateRequest.getAlternativaId()));

        // Validate that the alternative belongs to the question
        if (!alternativa.getQuestao().getId().equals(questao.getId())) {
            throw new ValidationException("A alternativa escolhida não pertence à questão informada");
        }

        Resposta resposta = new Resposta();
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);

        Resposta savedResposta = respostaRepository.save(resposta);
        return respostaMapper.toDto(savedResposta);
    }
    
    public RespostaDto updateResposta(Long id, RespostaUpdateRequest respostaUpdateRequest) {
        log.info("Atualizando resposta ID: {}", id);
        Resposta existingResposta = respostaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resposta", "ID", id));

        // Only allow updating the alternativaId, questaoId should remain unchanged
        Alternativa alternativa = alternativaRepository.findById(respostaUpdateRequest.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", respostaUpdateRequest.getAlternativaId()));

        // Validate that the alternative belongs to the question
        if (!alternativa.getQuestao().getId().equals(existingResposta.getQuestao().getId())) {
            throw new ValidationException("A alternativa escolhida não pertence à questão desta resposta");
        }

        // Check if the question is annulled
        if (existingResposta.getQuestao().getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        // Update only the alternativaId
        existingResposta.setAlternativaEscolhida(alternativa);
        // Date will be updated by @LastModifiedDate in BaseEntity

        Resposta updatedResposta = respostaRepository.save(existingResposta);
        return respostaMapper.toDto(updatedResposta);
    }
    
    public void deleteResposta(Long id) {
        log.info("Excluindo resposta ID: {}", id);
        if (!respostaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resposta", "ID", id);
        }
        respostaRepository.deleteById(id);
    }

    public RespostaComAlternativasDto createRespostaWithAlternativas(RespostaCreateRequest respostaCreateRequest) {
        Questao questao = questaoRepository.findById(respostaCreateRequest.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", respostaCreateRequest.getQuestaoId()));

        // Check if the question is annulled
        if (questao.getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        // Check if a response already exists for this question (business rule: only one response per question)
        if (respostaRepository.findByQuestaoId(respostaCreateRequest.getQuestaoId()) != null) {
            throw new ValidationException("Já existe uma resposta para esta questão. Use a operação de atualização para modificar a resposta.");
        }

        Alternativa alternativa = alternativaRepository.findById(respostaCreateRequest.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", respostaCreateRequest.getAlternativaId()));

        // Validate that the alternative belongs to the question
        if (!alternativa.getQuestao().getId().equals(questao.getId())) {
            throw new ValidationException("A alternativa escolhida não pertence à questão informada");
        }

        Resposta resposta = new Resposta();
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);

        Resposta savedResposta = respostaRepository.save(resposta);
        return respostaMapper.toComAlternativasDto(savedResposta);
    }

    public RespostaComAlternativasDto updateRespostaWithAlternativas(Long id, RespostaUpdateRequest respostaUpdateRequest) {
        Resposta existingResposta = respostaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resposta", "ID", id));

        // Only allow updating the alternativaId, questaoId should remain unchanged
        Alternativa alternativa = alternativaRepository.findById(respostaUpdateRequest.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", respostaUpdateRequest.getAlternativaId()));

        // Validate that the alternative belongs to the question
        if (!alternativa.getQuestao().getId().equals(existingResposta.getQuestao().getId())) {
            throw new ValidationException("A alternativa escolhida não pertence à questão desta resposta");
        }

        // Check if the question is annulled
        if (existingResposta.getQuestao().getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        // Update only the alternativaId
        existingResposta.setAlternativaEscolhida(alternativa);

        Resposta updatedResposta = respostaRepository.save(existingResposta);
        return respostaMapper.toComAlternativasDto(updatedResposta);
    }
}