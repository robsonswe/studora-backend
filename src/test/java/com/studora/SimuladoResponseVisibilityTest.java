package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.simulado.SimuladoDetailDto;
import com.studora.entity.Alternativa;
import com.studora.entity.Questao;
import com.studora.entity.Resposta;
import com.studora.entity.Simulado;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SimuladoRepository;
import com.studora.service.SimuladoService;
import com.studora.mapper.SimuladoMapper;
import com.studora.mapper.QuestaoMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class SimuladoResponseVisibilityTest {

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
        SimuladoMapper simuladoMapper = org.mapstruct.factory.Mappers.getMapper(SimuladoMapper.class);
        QuestaoMapper questaoMapper = org.mapstruct.factory.Mappers.getMapper(QuestaoMapper.class);
        
        ReflectionTestUtils.setField(simuladoMapper, "questaoMapper", questaoMapper);
        // We also need to set other mappers used by QuestaoMapper if any
        ReflectionTestUtils.setField(questaoMapper, "alternativaMapper", org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.AlternativaMapper.class));
        ReflectionTestUtils.setField(questaoMapper, "respostaMapper", org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.RespostaMapper.class));

        simuladoService = new SimuladoService(
            simuladoRepository, questaoRepository, respostaRepository, simuladoMapper,
            bancaRepository, cargoRepository, disciplinaRepository, temaRepository, subtemaRepository,
            bancaMapper, cargoMapper
        );
    }

    @Test
    void shouldMaintainGlobalRespondidaFlagButHideCorrectAnswerIfNotAnsweredInSimulado() {
        // Arrange
        Long simuladoId = 1L;
        Long questaoId = 3L;

        Simulado simulado = new Simulado();
        simulado.setId(simuladoId);
        simulado.setNome("Simulado Teste");

        Questao questao = new Questao();
        questao.setId(questaoId);
        questao.setEnunciado("Enunciado Teste");

        Alternativa alt = new Alternativa();
        alt.setId(8L);
        alt.setQuestao(questao);
        alt.setTexto("Alternativa Correta");
        alt.setCorreta(true);
        alt.setJustificativa("Porque sim");
        alt.setOrdem(1);
        questao.setAlternativas(Set.of(alt));

        // Response NOT for this simulado (simulado_id is null or different)
        Resposta globalResposta = new Resposta();
        globalResposta.setId(2L);
        globalResposta.setQuestao(questao);
        globalResposta.setAlternativaEscolhida(alt);
        globalResposta.setSimulado(null); // Global response
        questao.setRespostas(Set.of(globalResposta));

        simulado.setQuestoes(new ArrayList<>(List.of(questao)));

        when(simuladoRepository.findById(simuladoId)).thenReturn(Optional.of(simulado));
        when(respostaRepository.findBySimuladoId(simuladoId)).thenReturn(List.of());

        // Act
        SimuladoDetailDto result = simuladoService.getSimuladoDetailById(simuladoId);

        // Assert
        assertNotNull(result);
        QuestaoSummaryDto questaoDto = result.getQuestoes().get(0);
        
        // 1. respondida should be true (global)
        assertTrue(questaoDto.getRespondida(), "respondida should be true because there is a global response");
        
        // 2. respostas should be empty for this simulado
        assertTrue(questaoDto.getRespostas().isEmpty(), "respostas list should be empty for this simulado context");

        // 3. correta and justificativa should be NULL because not answered in this simulado
        assertNull(questaoDto.getAlternativas().get(0).getCorreta(), "correta should be hidden (null) if not answered in this simulado");
        assertNull(questaoDto.getAlternativas().get(0).getJustificativa(), "justificativa should be hidden (null) if not answered in this simulado");
    }
}
