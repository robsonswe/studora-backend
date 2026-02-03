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

    @Mock
    private com.studora.repository.SimuladoRepository simuladoRepository;

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
            simuladoRepository,
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

        when(respostaRepository.findByIdWithDetails(respostaId)).thenReturn(Optional.of(resposta));

        // Act
        RespostaDto result = respostaService.getRespostaById(respostaId);

        // Assert
        assertNotNull(result);
        assertEquals(resposta.getId(), result.getId());
        verify(respostaRepository, times(1)).findByIdWithDetails(respostaId);
    }

    @Test
    void testGetRespostaById_NotFound() {
        // Arrange
        Long respostaId = 1L;
        when(respostaRepository.findByIdWithDetails(respostaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            respostaService.getRespostaById(respostaId);
        });

        verify(respostaRepository, times(1)).findByIdWithDetails(respostaId);
    }

    @Test
    void testGetRespostaByQuestaoId_Success() {
        // Arrange
        Long questaoId = 1L;
        Resposta resposta = new Resposta();
        resposta.setId(1L);

        Questao questao = new Questao();
        questao.setId(questaoId);
        resposta.setQuestao(questao);

        Alternativa alternativa = new Alternativa();
        alternativa.setId(1L);
        resposta.setAlternativaEscolhida(alternativa);

        when(respostaRepository.findByQuestaoIdWithDetails(questaoId)).thenReturn(java.util.List.of(resposta));

        // Act
        java.util.List<RespostaDto> result = respostaService.getRespostasByQuestaoId(questaoId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(questaoId, result.get(0).getQuestaoId());
        verify(respostaRepository, times(1)).findByQuestaoIdWithDetails(questaoId);
    }

    @Test
    void testGetRespostaByQuestaoId_NotFound() {
        // Arrange
        Long questaoId = 1L;
        when(respostaRepository.findByQuestaoIdWithDetails(questaoId)).thenReturn(java.util.Collections.emptyList());

        // Act & Assert
        assertThrows(com.studora.exception.ResourceNotFoundException.class, () -> {
            respostaService.getRespostasByQuestaoId(questaoId);
        });

        verify(respostaRepository, times(1)).findByQuestaoIdWithDetails(questaoId);
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

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findById(1L)).thenReturn(Optional.of(alternativa));
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
        verify(respostaRepository, times(1)).save(any(Resposta.class));
    }

    @Test
    void testCreateResposta_QuestaoNotFound() {
        // Arrange
        RespostaCreateRequest respostaCreateRequest = new RespostaCreateRequest();
        confirmCreateRequest(respostaCreateRequest);

        when(questaoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            respostaService.createResposta(respostaCreateRequest);
        });

        verify(questaoRepository, times(1)).findById(1L);
        verify(alternativaRepository, never()).findById(anyLong());
        verify(respostaRepository, never()).save(any(Resposta.class));
    }

    private void confirmCreateRequest(RespostaCreateRequest req) {
        req.setQuestaoId(1L);
        req.setAlternativaId(1L);
    }

    @Test
    void testCreateResposta_AlternativaNotFound() {
        // Arrange
        RespostaCreateRequest respostaCreateRequest = new RespostaCreateRequest();
        confirmCreateRequest(respostaCreateRequest);

        Questao questao = new Questao();
        questao.setId(1L);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            respostaService.createResposta(respostaCreateRequest);
        });

        verify(questaoRepository, times(1)).findById(1L);
        verify(alternativaRepository, times(1)).findById(1L);
        verify(respostaRepository, never()).save(any(Resposta.class));
    }

    @Test
    void testCreateResposta_AlternativaDoesNotBelongToQuestao() {
        // Arrange
        RespostaCreateRequest respostaCreateRequest = new RespostaCreateRequest();
        confirmCreateRequest(respostaCreateRequest);

        Questao questao1 = new Questao();
        questao1.setId(1L);
        questao1.setAnulada(false);

        Questao questao2 = new Questao();
        questao2.setId(2L);

        Alternativa alternativa = new Alternativa();
        alternativa.setId(1L);
        alternativa.setQuestao(questao2); // Belongs to a different question

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao1));
        when(alternativaRepository.findById(1L)).thenReturn(Optional.of(alternativa));

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(com.studora.exception.ValidationException.class, () -> {
            respostaService.createResposta(respostaCreateRequest);
        });

        assertEquals("A alternativa escolhida não pertence à questão informada", exception.getMessage());
        verify(respostaRepository, never()).save(any(Resposta.class));
    }
}
