package com.studora;

import com.studora.dto.RespostaDto;
import com.studora.dto.request.RespostaCreateRequest;
import com.studora.entity.Alternativa;
import com.studora.entity.Questao;
import com.studora.entity.Resposta;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.service.RespostaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RespostaServiceTest {

    @Mock
    private RespostaRepository respostaRepository;

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private AlternativaRepository alternativaRepository;

    @InjectMocks
    private RespostaService respostaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Use real mapper
        com.studora.mapper.RespostaMapper realRespostaMapper = org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.RespostaMapper.class);
        
        respostaService = new RespostaService(
            respostaRepository,
            questaoRepository,
            alternativaRepository,
            realRespostaMapper
        );
    }

    @Test
    void testGetRespostaById_Success() {
        // Arrange
        Long respostaId = 1L;
        Resposta resposta = new Resposta();
        resposta.setId(respostaId);

        Questao questao = new Questao();
        questao.setId(1L);
        resposta.setQuestao(questao);

        Alternativa alternativa = new Alternativa();
        alternativa.setId(1L);
        resposta.setAlternativaEscolhida(alternativa);

        RespostaDto dto = new RespostaDto();
        dto.setId(respostaId);
        dto.setQuestaoId(1L);
        dto.setAlternativaId(1L);

        when(respostaRepository.findById(respostaId)).thenReturn(Optional.of(resposta));

        // Act
        RespostaDto result = respostaService.getRespostaById(respostaId);

        // Assert
        assertNotNull(result);
        assertEquals(resposta.getId(), result.getId());
        verify(respostaRepository, times(1)).findById(respostaId);
    }

    @Test
    void testGetRespostaById_NotFound() {
        // Arrange
        Long respostaId = 1L;
        when(respostaRepository.findById(respostaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            respostaService.getRespostaById(respostaId);
        });

        verify(respostaRepository, times(1)).findById(respostaId);
    }

    @Test
    void testCreateResposta_Success() {
        // Arrange
        RespostaCreateRequest respostaCreateRequest = new RespostaCreateRequest();
        respostaCreateRequest.setQuestaoId(1L);
        respostaCreateRequest.setAlternativaId(1L);

        Questao questao = new Questao();
        questao.setId(1L);
        questao.setAnulada(false);

        Alternativa alternativa = new Alternativa();
        alternativa.setId(1L);
        alternativa.setQuestao(questao);

        Resposta savedResposta = new Resposta();
        savedResposta.setId(1L);
        savedResposta.setQuestao(questao);
        savedResposta.setAlternativaEscolhida(alternativa);

        RespostaDto resultDto = new RespostaDto();
        resultDto.setId(1L);
        resultDto.setQuestaoId(1L);
        resultDto.setAlternativaId(1L);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findById(1L)).thenReturn(Optional.of(alternativa));
        when(respostaRepository.findByQuestaoId(1L)).thenReturn(null); // No existing response for this question
        when(respostaRepository.save(any(Resposta.class))).thenAnswer(i -> {
            Resposta r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        // Act
        RespostaDto result = respostaService.createResposta(respostaCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(questao.getId(), result.getQuestaoId());
        assertEquals(alternativa.getId(), result.getAlternativaId());
        verify(questaoRepository, times(1)).findById(1L);
        verify(alternativaRepository, times(1)).findById(1L);
        verify(respostaRepository, times(1)).findByQuestaoId(1L);
        verify(respostaRepository, times(1)).save(any(Resposta.class));
    }

    @Test
    void testCreateResposta_QuestaoNotFound() {
        // Arrange
        RespostaCreateRequest respostaCreateRequest = new RespostaCreateRequest();
        respostaCreateRequest.setQuestaoId(1L);
        respostaCreateRequest.setAlternativaId(1L);

        when(questaoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            respostaService.createResposta(respostaCreateRequest);
        });

        verify(questaoRepository, times(1)).findById(1L);
        verify(alternativaRepository, never()).findById(anyLong());
        verify(respostaRepository, never()).save(any(Resposta.class));
    }

    @Test
    void testCreateResposta_AlternativaNotFound() {
        // Arrange
        RespostaCreateRequest respostaCreateRequest = new RespostaCreateRequest();
        respostaCreateRequest.setQuestaoId(1L);
        respostaCreateRequest.setAlternativaId(1L);

        Questao questao = new Questao();
        questao.setId(1L);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findById(1L)).thenReturn(Optional.empty());
        when(respostaRepository.findByQuestaoId(1L)).thenReturn(null); // No existing response for this question

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            respostaService.createResposta(respostaCreateRequest);
        });

        verify(questaoRepository, times(1)).findById(1L);
        verify(alternativaRepository, times(1)).findById(1L);
        verify(respostaRepository, times(1)).findByQuestaoId(1L);
        verify(respostaRepository, never()).save(any(Resposta.class));
    }
}
