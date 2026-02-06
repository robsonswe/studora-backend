package com.studora.service;

import com.studora.dto.resposta.RespostaDetailDto;
import com.studora.dto.resposta.RespostaSummaryDto;
import com.studora.dto.request.RespostaCreateRequest;
import com.studora.entity.Alternativa;
import com.studora.entity.Questao;
import com.studora.entity.Resposta;
import com.studora.entity.Simulado;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.RespostaMapper;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SimuladoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RespostaService {

    private final RespostaRepository respostaRepository;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final SimuladoRepository simuladoRepository;
    private final RespostaMapper respostaMapper;

    @Transactional(readOnly = true)
    public Page<RespostaSummaryDto> findAll(Pageable pageable) {
        return respostaRepository.findAll(pageable)
                .map(respostaMapper::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public RespostaSummaryDto getRespostaSummaryById(Long id) {
        Resposta resposta = respostaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resposta", "ID", id));
        return respostaMapper.toSummaryDto(resposta);
    }

    @Transactional(readOnly = true)
    public List<RespostaSummaryDto> getRespostasByQuestaoId(Long questaoId) {
        if (!questaoRepository.existsById(questaoId)) {
            throw new ResourceNotFoundException("Questão", "ID", questaoId);
        }
        return respostaRepository.findByQuestaoIdWithDetails(questaoId).stream()
                .map(respostaMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RespostaSummaryDto> getRespostasByQuestaoIds(Collection<Long> questaoIds) {
        return respostaRepository.findByQuestaoIdInWithDetails(questaoIds).stream()
                .map(respostaMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    public RespostaDetailDto createResposta(RespostaCreateRequest request) {
        log.info("Criando nova tentativa para a questão ID: {}", request.getQuestaoId());
        
        Questao questao = questaoRepository.findById(request.getQuestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Questão", "ID", request.getQuestaoId()));

        if (Boolean.TRUE.equals(questao.getAnulada())) {
            throw new ValidationException("Não é possível responder a uma questão anulada");
        }

        Alternativa alternativa = alternativaRepository.findById(request.getAlternativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Alternativa", "ID", request.getAlternativaId()));

        if (!alternativa.getQuestao().getId().equals(questao.getId())) {
            throw new ValidationException("A alternativa selecionada não pertence a esta questão");
        }

        Resposta resposta = respostaMapper.toEntity(request);
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);

        if (request.getSimuladoId() != null) {
            Simulado simulado = simuladoRepository.findById(request.getSimuladoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Simulado", "ID", request.getSimuladoId()));
            resposta.setSimulado(simulado);
        }

        return respostaMapper.toDetailDto(respostaRepository.save(resposta));
    }

    public void delete(Long id) {
        log.info("Excluindo resposta ID: {}", id);
        if (!respostaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resposta", "ID", id);
        }
        respostaRepository.deleteById(id);
    }
}