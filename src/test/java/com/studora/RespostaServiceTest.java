package com.studora;

import com.studora.dto.RespostaDto;
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
        RespostaDto respostaDto = new RespostaDto();
        respostaDto.setQuestaoId(1L);
        respostaDto.setAlternativaId(1L);

        Questao questao = new Questao();
        questao.setId(1L);

        Alternativa alternativa = new Alternativa();
        alternativa.setId(1L);

        Resposta savedResposta = new Resposta();
        savedResposta.setId(1L);
        savedResposta.setQuestao(questao);
        savedResposta.setAlternativaEscolhida(alternativa);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findById(1L)).thenReturn(Optional.of(alternativa));
        when(respostaRepository.save(any(Resposta.class))).thenReturn(savedResposta);

        // Act
        RespostaDto result = respostaService.createResposta(respostaDto);

        // Assert
        assertNotNull(result);
        assertEquals(savedResposta.getId(), result.getId());
        assertEquals(questao.getId(), result.getQuestaoId());
        assertEquals(alternativa.getId(), result.getAlternativaId());
        verify(questaoRepository, times(1)).findById(1L);
        verify(alternativaRepository, times(1)).findById(1L);
        verify(respostaRepository, times(1)).save(any(Resposta.class));
    }

    @Test
    void testCreateResposta_QuestaoNotFound() {
        // Arrange
        RespostaDto respostaDto = new RespostaDto();
        respostaDto.setQuestaoId(1L);
        respostaDto.setAlternativaId(1L);

        when(questaoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            respostaService.createResposta(respostaDto);
        });

        verify(questaoRepository, times(1)).findById(1L);
        verify(alternativaRepository, never()).findById(anyLong());
        verify(respostaRepository, never()).save(any(Resposta.class));
    }

    @Test
    void testCreateResposta_AlternativaNotFound() {
        // Arrange
        RespostaDto respostaDto = new RespostaDto();
        respostaDto.setQuestaoId(1L);
        respostaDto.setAlternativaId(1L);

        Questao questao = new Questao();
        questao.setId(1L);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            respostaService.createResposta(respostaDto);
        });

        verify(questaoRepository, times(1)).findById(1L);
        verify(alternativaRepository, times(1)).findById(1L);
        verify(respostaRepository, never()).save(any(Resposta.class));
    }
}
