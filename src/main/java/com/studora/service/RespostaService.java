package com.studora.service;

import com.studora.dto.RespostaComAlternativasDto;
import com.studora.dto.RespostaDto;
import com.studora.dto.request.RespostaCreateRequest;
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
    
    public java.util.List<RespostaDto> getRespostasByQuestaoId(Long questaoId) {
        java.util.List<Resposta> respostas = respostaRepository.findByQuestaoIdWithDetails(questaoId);
        if (respostas.isEmpty()) {
            throw new ResourceNotFoundException("Resposta", "ID da Questão", questaoId);
        }
        return respostas.stream()
                .map(respostaMapper::toDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public RespostaDto createResposta(RespostaCreateRequest request) {
        log.info("Criando nova tentativa para a questão ID: {}", request.getQuestaoId());
        Questao questao = questaoRepository.findById(request.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", request.getQuestaoId()));

        // Check if the question is annulled
        if (questao.getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        Alternativa alternativa = alternativaRepository.findById(request.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", request.getAlternativaId()));

        // Validate that the alternative belongs to the question
        if (!alternativa.getQuestao().getId().equals(questao.getId())) {
            throw new ValidationException("A alternativa escolhida não pertence à questão informada");
        }

        Resposta resposta = respostaMapper.toEntity(request);
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);

        Resposta savedResposta = respostaRepository.save(resposta);
        return respostaMapper.toDto(savedResposta);
    }

    public RespostaComAlternativasDto createRespostaWithAlternativas(RespostaCreateRequest request) {
        log.info("Criando nova tentativa com alternativas para a questão ID: {}", request.getQuestaoId());
        Questao questao = questaoRepository.findById(request.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", request.getQuestaoId()));

        if (questao.getAnulada()) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        Alternativa alternativa = alternativaRepository.findById(request.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", request.getAlternativaId()));

        if (!alternativa.getQuestao().getId().equals(questao.getId())) {
            throw new ValidationException("A alternativa escolhida não pertence à questão informada");
        }

        Resposta resposta = respostaMapper.toEntity(request);
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);

        Resposta savedResposta = respostaRepository.save(resposta);
        return respostaMapper.toComAlternativasDto(savedResposta);
    }

    @Transactional
    public void deleteResposta(Long id) {
        log.info("Excluindo resposta ID: {}", id);
        if (!respostaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resposta", "ID", id);
        }
        respostaRepository.deleteById(id);
    }
}