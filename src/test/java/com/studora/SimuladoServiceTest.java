package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.studora.dto.request.SimuladoGenerationRequest;
import com.studora.entity.Questao;
import com.studora.entity.Simulado;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SimuladoRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.service.SimuladoService;
import com.studora.mapper.QuestaoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class SimuladoServiceTest {

    private SimuladoService simuladoService;

    @Mock
    private SimuladoRepository simuladoRepository;

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private RespostaRepository respostaRepository;

    @Mock
    private SubtemaRepository subtemaRepository;

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private QuestaoMapper questaoMapper;

    @Mock
    private com.studora.mapper.RespostaMapper respostaMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        simuladoService = new SimuladoService(
            simuladoRepository,
            questaoRepository,
            respostaRepository,
            subtemaRepository,
            temaRepository,
            questaoMapper,
            respostaMapper
        );
    }

    @Test
    void testGerarSimulado_VennDiagramLogic() {
        // Arrange
        SimuladoGenerationRequest request = new SimuladoGenerationRequest();
        request.setNome("Test Mock Exam");
        
        SimuladoGenerationRequest.ItemSelection item = new SimuladoGenerationRequest.ItemSelection();
        item.setId(1L);
        item.setQuantidade(20);
        request.setDisciplinas(List.of(item));

        // Mock randomization query returning 20 IDs
        List<Long> ids = new ArrayList<>();
        for (long i = 1; i <= 20; i++) ids.add(i);
        
        when(questaoRepository.findIdsByDisciplinaWithPreferences(anyLong(), any(), any(), any(), any(), any()))
            .thenReturn(ids);

        // Mock loading entities for these IDs
        List<Questao> questoes = new ArrayList<>();
        for (Long id : ids) {
            Questao q = new Questao(); q.setId(id);
            questoes.add(q);
        }
        when(questaoRepository.findAllById(any())).thenReturn(questoes);
        when(questaoMapper.toDto(any(com.studora.entity.Questao.class))).thenReturn(new com.studora.dto.QuestaoDto());
        
        when(simuladoRepository.save(any())).thenAnswer(i -> {
            Simulado s = i.getArgument(0);
            s.setId(1L);
            return s;
        });

        // Act
        simuladoService.gerarSimulado(request);

        // Assert
        verify(questaoRepository, times(1)).findIdsByDisciplinaWithPreferences(eq(1L), any(), any(), any(), any(), any());
        verify(simuladoRepository).save(argThat(s -> s.getNome().equals("Test Mock Exam")));
    }

    @Test
    void testIniciarSimulado_Success() {
        // Arrange
        Simulado simulado = new Simulado();
        simulado.setId(1L);
        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(simulado));
        when(simuladoRepository.save(any())).thenReturn(simulado);

        // Act
        simuladoService.iniciarSimulado(1L);

        // Assert
        assertNotNull(simulado.getStartedAt());
        verify(simuladoRepository).save(simulado);
    }

    @Test
    void testFinalizarSimulado_Success() {
        // Arrange
        Simulado simulado = new Simulado();
        simulado.setId(1L);
        simulado.setStartedAt(java.time.LocalDateTime.now().minusHours(1));
        
        Questao q1 = new Questao(); q1.setId(10L);
        simulado.setQuestoes(new java.util.LinkedHashSet<>(List.of(q1)));

        when(simuladoRepository.findByIdWithQuestoes(1L)).thenReturn(Optional.of(simulado));
        when(questaoMapper.toDto(q1)).thenReturn(new com.studora.dto.QuestaoDto());
        
        com.studora.entity.Resposta resp1 = new com.studora.entity.Resposta();
        resp1.setQuestao(q1);
        when(respostaRepository.findBySimuladoId(1L)).thenReturn(List.of(resp1));
        
        when(simuladoRepository.save(any())).thenReturn(simulado);
        
        // Act
        var result = simuladoService.finalizarSimulado(1L);

        // Assert
        assertNotNull(simulado.getFinishedAt());
    }

    @Test
    void testFinalizarSimulado_FailsWithUnansweredQuestions() {
        // Arrange
        Simulado simulado = new Simulado();
        simulado.setId(1L);
        simulado.setStartedAt(java.time.LocalDateTime.now().minusHours(1));
        
        Questao q1 = new Questao(); q1.setId(10L);
        Questao q2 = new Questao(); q2.setId(11L);
        simulado.setQuestoes(new java.util.LinkedHashSet<>(List.of(q1, q2)));

        when(simuladoRepository.findByIdWithQuestoes(1L)).thenReturn(Optional.of(simulado));
        
        // Only one question answered
        com.studora.entity.Resposta resp1 = new com.studora.entity.Resposta();
        resp1.setQuestao(q1);
        when(respostaRepository.findBySimuladoId(1L)).thenReturn(List.of(resp1));

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class,
            () -> simuladoService.finalizarSimulado(1L)
        );

        assertTrue(exception.getMessage().contains("questões sem resposta"));
        verify(simuladoRepository, never()).save(any());
    }

    @Test
    void testDeleteSimulado() {
        // Arrange
        when(simuladoRepository.existsById(1L)).thenReturn(true);

        // Act
        simuladoService.deleteSimulado(1L);

        // Assert
        verify(respostaRepository).detachSimulado(1L);
        verify(simuladoRepository).deleteById(1L);
    }
}
