package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.simulado.SimuladoDetailDto;
import com.studora.dto.simulado.SimuladoSummaryDto;
import com.studora.entity.Questao;
import com.studora.entity.Simulado;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SimuladoRepository;
import com.studora.service.SimuladoService;
import com.studora.mapper.SimuladoMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SimuladoServiceTest {

    @Mock private SimuladoRepository simuladoRepository;
    @Mock private QuestaoRepository questaoRepository;
    @Mock private RespostaRepository respostaRepository;
    @Mock private com.studora.repository.BancaRepository bancaRepository;
    @Mock private com.studora.repository.CargoRepository cargoRepository;
    @Mock private com.studora.repository.DisciplinaRepository disciplinaRepository;
    @Mock private com.studora.repository.TemaRepository temaRepository;
    @Mock private com.studora.repository.SubtemaRepository subtemaRepository;
    @Mock private com.studora.mapper.BancaMapper bancaMapper;
    @Mock private com.studora.mapper.CargoMapper cargoMapper;

    private SimuladoService simuladoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SimuladoMapper realMapper = org.mapstruct.factory.Mappers.getMapper(SimuladoMapper.class);
        
        // Inject QuestaoMapper into SimuladoMapper using ReflectionTestUtils (real mappers)
        com.studora.mapper.QuestaoMapper questaoMapper = org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.QuestaoMapper.class);
        org.springframework.test.util.ReflectionTestUtils.setField(realMapper, "questaoMapper", questaoMapper);
        
        simuladoService = new SimuladoService(
            simuladoRepository, questaoRepository, respostaRepository, realMapper,
            bancaRepository, cargoRepository, disciplinaRepository, temaRepository, subtemaRepository,
            bancaMapper, cargoMapper
        );
    }

    @Test
    void testFindById() {
        Simulado s = new Simulado();
        s.setId(1L);
        s.setNome("Simulado A");

        when(simuladoRepository.findById(1L)).thenReturn(Optional.of(s));

        SimuladoDetailDto result = simuladoService.getSimuladoDetailById(1L);
        assertNotNull(result);
        assertEquals("Simulado A", result.getNome());
    }

    @Test
    void testGerarSimulado_VennDiagramLogic() {
        // Arrange
        com.studora.dto.request.SimuladoGenerationRequest request = new com.studora.dto.request.SimuladoGenerationRequest();
        request.setNome("Test Mock Exam");

        com.studora.dto.request.SimuladoGenerationRequest.ItemSelection item = new com.studora.dto.request.SimuladoGenerationRequest.ItemSelection();
        item.setId(1L);
        item.setQuantidade(20);
        request.setDisciplinas(java.util.List.of(item));

        // Mock randomization query returning 20 IDs
        java.util.List<Long> ids = new java.util.ArrayList<>();
        for (long i = 1; i <= 20; i++) ids.add(i);

        when(questaoRepository.findIdsByDisciplinaWithPreferences(anyLong(), any(), any(), any(), any(), any()))
            .thenReturn(ids);

        // Mock loading entities for these IDs
        java.util.List<Questao> questoes = new java.util.ArrayList<>();
        for (Long id : ids) {
            Questao q = new Questao(); q.setId(id);
            questoes.add(q);
        }
        when(questaoRepository.findAllById(any())).thenReturn(questoes);
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
        simulado.setQuestoes(new java.util.ArrayList<>(java.util.List.of(q1)));

        when(simuladoRepository.findByIdWithQuestoes(1L)).thenReturn(Optional.of(simulado));
        when(respostaRepository.countBySimuladoId(1L)).thenReturn(1);
        when(simuladoRepository.save(any())).thenReturn(simulado);

        // Act
        simuladoService.finalizarSimulado(1L);

        // Assert
        assertNotNull(simulado.getFinishedAt());
        verify(simuladoRepository).save(simulado);
    }

    @Test
    void testFinalizarSimulado_FailsWithUnansweredQuestions() {
        // Arrange
        Simulado simulado = new Simulado();
        simulado.setId(1L);
        simulado.setStartedAt(java.time.LocalDateTime.now().minusHours(1));

        Questao q1 = new Questao(); q1.setId(10L);
        Questao q2 = new Questao(); q2.setId(11L);
        simulado.setQuestoes(new java.util.ArrayList<>(java.util.List.of(q1, q2)));

        when(simuladoRepository.findByIdWithQuestoes(1L)).thenReturn(Optional.of(simulado));
        // Only one question answered
        when(respostaRepository.countBySimuladoId(1L)).thenReturn(1);

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class,
            () -> simuladoService.finalizarSimulado(1L)
        );

        assertTrue(exception.getMessage().contains("questões sem resposta"));
        verify(simuladoRepository, never()).save(any());
    }

    @Test
    void testDelete() {
        when(simuladoRepository.existsById(1L)).thenReturn(true);
        simuladoService.delete(1L);
        verify(simuladoRepository, times(1)).deleteById(1L);
        verify(respostaRepository, times(1)).detachSimulado(1L);
    }
}