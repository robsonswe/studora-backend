package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.resposta.RespostaDetailDto;
import com.studora.dto.resposta.RespostaSummaryDto;
import com.studora.dto.request.RespostaCreateRequest;
import com.studora.entity.Alternativa;
import com.studora.entity.Questao;
import com.studora.entity.Resposta;
import com.studora.entity.Simulado;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SimuladoRepository;
import com.studora.service.RespostaService;
import com.studora.mapper.RespostaMapper;
import java.util.Optional;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class RespostaServiceTest {

    @Mock private RespostaRepository respostaRepository;
    @Mock private QuestaoRepository questaoRepository;
    @Mock private AlternativaRepository alternativaRepository;
    @Mock private SimuladoRepository simuladoRepository;

    private RespostaService respostaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RespostaMapper realMapper = org.mapstruct.factory.Mappers.getMapper(RespostaMapper.class);
        respostaService = new RespostaService(respostaRepository, questaoRepository, alternativaRepository, simuladoRepository, realMapper);
    }

    @Test
    void testGetRespostaSummaryById_Success() {
        Long id = 1L;
        Resposta r = new Resposta(); r.setId(id);
        r.setQuestao(new Questao()); r.getQuestao().setId(10L);
        r.setAlternativaEscolhida(new Alternativa()); r.getAlternativaEscolhida().setId(100L);
        
        when(respostaRepository.findByIdWithDetails(id)).thenReturn(Optional.of(r));
        RespostaSummaryDto result = respostaService.getRespostaSummaryById(id);
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void testGetRespostaSummaryById_NotFound() {
        when(respostaRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());
        assertThrows(com.studora.exception.ResourceNotFoundException.class, () -> respostaService.getRespostaSummaryById(1L));
    }

    @Test
    void testGetRespostasByQuestaoId_Success() {
        Long qId = 1L;
        Resposta r = new Resposta(); r.setId(10L); r.setQuestao(new Questao()); r.getQuestao().setId(qId);
        r.setAlternativaEscolhida(new Alternativa()); r.getAlternativaEscolhida().setId(100L);
        
        when(questaoRepository.existsById(qId)).thenReturn(true);
        when(respostaRepository.findByQuestaoIdWithDetails(qId)).thenReturn(Arrays.asList(r));
        
        List<RespostaSummaryDto> result = respostaService.getRespostasByQuestaoId(qId);
        assertEquals(1, result.size());
    }

    @Test
    void testGetRespostasByQuestaoId_NotFound() {
        when(questaoRepository.existsById(1L)).thenReturn(false);
        assertThrows(com.studora.exception.ResourceNotFoundException.class, () -> respostaService.getRespostasByQuestaoId(1L));
    }

    @Test
    void testFindAll() {
        Resposta r = new Resposta(); r.setId(1L);
        r.setQuestao(new Questao()); r.getQuestao().setId(10L);
        r.setAlternativaEscolhida(new Alternativa()); r.getAlternativaEscolhida().setId(100L);
        
        Page<Resposta> page = new PageImpl<>(Collections.singletonList(r));
        when(respostaRepository.findAll(any(Pageable.class))).thenReturn(page);
        
        Page<RespostaSummaryDto> result = respostaService.findAll(Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testCreateResposta_Success() {
        Questao q = new Questao(); q.setId(1L); q.setAnulada(false);
        Alternativa alt = new Alternativa(); alt.setId(1L); alt.setQuestao(q); alt.setCorreta(true);

        RespostaCreateRequest req = new RespostaCreateRequest();
        req.setQuestaoId(1L);
        req.setAlternativaId(1L);
        req.setDificuldadeId(1);
        req.setJustificativa("Test reasoning");

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(q));
        when(alternativaRepository.findById(1L)).thenReturn(Optional.of(alt));
        when(respostaRepository.save(any(Resposta.class))).thenAnswer(i -> {
            Resposta r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        RespostaDetailDto result = respostaService.createResposta(req);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getCorreta());
    }

    @Test
    void testCreateResposta_AnuladaQuestion_Fails() {
        Questao q = new Questao(); q.setId(1L); q.setAnulada(true);
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(q));

        RespostaCreateRequest req = new RespostaCreateRequest();
        req.setQuestaoId(1L);

        assertThrows(com.studora.exception.ValidationException.class, () -> respostaService.createResposta(req));
    }

    @Test
    void testCreateResposta_WrongAlternative_Fails() {
        Questao q1 = new Questao(); q1.setId(1L); q1.setAnulada(false);
        Questao q2 = new Questao(); q2.setId(2L);
        // Alternative belongs to a different question (q2)
        Alternativa alt = new Alternativa(); alt.setId(10L); alt.setQuestao(q2);

        RespostaCreateRequest req = new RespostaCreateRequest();
        req.setQuestaoId(1L);
        req.setAlternativaId(10L);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(q1));
        when(alternativaRepository.findById(10L)).thenReturn(Optional.of(alt));

        assertThrows(com.studora.exception.ValidationException.class, () -> respostaService.createResposta(req));
    }

    @Test
    void testCreateResposta_QuestaoNotFound_Fails() {
        when(questaoRepository.findById(99L)).thenReturn(Optional.empty());

        RespostaCreateRequest req = new RespostaCreateRequest();
        req.setQuestaoId(99L);

        assertThrows(com.studora.exception.ResourceNotFoundException.class, () -> respostaService.createResposta(req));
    }

    @Test
    void testCreateResposta_AlternativaNotFound_Fails() {
        Questao q = new Questao(); q.setId(1L); q.setAnulada(false);
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(q));
        when(alternativaRepository.findById(99L)).thenReturn(Optional.empty());

        RespostaCreateRequest req = new RespostaCreateRequest();
        req.setQuestaoId(1L);
        req.setAlternativaId(99L);

        assertThrows(com.studora.exception.ResourceNotFoundException.class, () -> respostaService.createResposta(req));
    }

    @Test
    void testCreateResposta_WithSimulado_AssignedToSimulado() {
        Questao q = new Questao(); q.setId(1L); q.setAnulada(false);
        Alternativa alt = new Alternativa(); alt.setId(1L); alt.setQuestao(q); alt.setCorreta(false);
        Simulado simulado = new Simulado(); simulado.setId(5L);

        RespostaCreateRequest req = new RespostaCreateRequest();
        req.setQuestaoId(1L);
        req.setAlternativaId(1L);
        req.setSimuladoId(5L);
        req.setDificuldadeId(2);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(q));
        when(alternativaRepository.findById(1L)).thenReturn(Optional.of(alt));
        when(simuladoRepository.findById(5L)).thenReturn(Optional.of(simulado));
        when(respostaRepository.save(any(Resposta.class))).thenAnswer(i -> {
            Resposta r = i.getArgument(0);
            r.setId(7L);
            return r;
        });

        RespostaDetailDto result = respostaService.createResposta(req);
        assertNotNull(result);
        assertFalse(result.getCorreta()); // alternative is wrong
    }

    @Test
    void testCreateResposta_SimuladoNotFound_Fails() {
        Questao q = new Questao(); q.setId(1L); q.setAnulada(false);
        Alternativa alt = new Alternativa(); alt.setId(1L); alt.setQuestao(q); alt.setCorreta(true);

        RespostaCreateRequest req = new RespostaCreateRequest();
        req.setQuestaoId(1L);
        req.setAlternativaId(1L);
        req.setSimuladoId(999L);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(q));
        when(alternativaRepository.findById(1L)).thenReturn(Optional.of(alt));
        when(simuladoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.studora.exception.ResourceNotFoundException.class, () -> respostaService.createResposta(req));
    }

    @Test
    void testDelete_NotFound_ThrowsException() {
        when(respostaRepository.existsById(99L)).thenReturn(false);

        assertThrows(com.studora.exception.ResourceNotFoundException.class, () -> respostaService.delete(99L));
        verify(respostaRepository, never()).deleteById(any());
    }

    @Test
    void testGetRespostasByQuestaoIds_ReturnsList() {
        Resposta r = new Resposta(); r.setId(1L);
        r.setQuestao(new Questao()); r.getQuestao().setId(10L);
        r.setAlternativaEscolhida(new Alternativa()); r.getAlternativaEscolhida().setId(100L);

        when(respostaRepository.findByQuestaoIdInWithDetails(java.util.List.of(10L, 11L)))
                .thenReturn(java.util.List.of(r));

        java.util.List<RespostaSummaryDto> result = respostaService.getRespostasByQuestaoIds(java.util.List.of(10L, 11L));
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}