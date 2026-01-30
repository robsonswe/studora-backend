package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.QuestaoCargoDto;
import com.studora.dto.QuestaoDto;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Questao;
import com.studora.entity.QuestaoCargo;
import com.studora.repository.*;
import com.studora.service.QuestaoService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class QuestaoServiceTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private ConcursoRepository concursoRepository;

    @Mock
    private SubtemaRepository subtemaRepository;

    @Mock
    private ConcursoCargoRepository concursoCargoRepository;

    @Mock
    private QuestaoCargoRepository questaoCargoRepository;

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @InjectMocks
    private QuestaoService questaoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetQuestaoById_Success() {
        // Arrange
        Long questaoId = 1L;
        Questao questao = new Questao();
        questao.setId(questaoId);
        questao.setEnunciado("Qual a capital do Brasil?");

        com.studora.entity.Instituicao instituicao =
            new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023);
        concurso.setId(1L);
        questao.setConcurso(concurso);

        when(questaoRepository.findById(questaoId)).thenReturn(
            Optional.of(questao)
        );

        // Act
        QuestaoDto result = questaoService.getQuestaoById(questaoId);

        // Assert
        assertNotNull(result);
        assertEquals(questao.getEnunciado(), result.getEnunciado());
        verify(questaoRepository, times(1)).findById(questaoId);
    }

    @Test
    void testGetQuestaoById_NotFound() {
        // Arrange
        Long questaoId = 1L;
        when(questaoRepository.findById(questaoId)).thenReturn(
            Optional.empty()
        );

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.getQuestaoById(questaoId);
        });

        verify(questaoRepository, times(1)).findById(questaoId);
    }

    @Test
    void testCreateQuestao_Success() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setImageUrl("https://exemplo.com/imagem.jpg");
        // Add at least one cargo association to comply with validation
        questaoDto.setConcursoCargoIds(Arrays.asList(1L));

        com.studora.entity.Instituicao instituicao =
            new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023);
        concurso.setId(1L);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(1L);

        Questao savedQuestao = new Questao();
        savedQuestao.setId(1L);
        savedQuestao.setEnunciado(questaoDto.getEnunciado());
        savedQuestao.setConcurso(concurso);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(concursoCargo));
        when(questaoRepository.save(any(Questao.class))).thenReturn(
            savedQuestao
        );

        // Act
        QuestaoDto result = questaoService.createQuestao(questaoDto);

        // Assert
        assertNotNull(result);
        assertEquals(savedQuestao.getEnunciado(), result.getEnunciado());
        assertEquals(concurso.getId(), result.getConcursoId());
        verify(concursoRepository, times(1)).findById(1L);
        verify(concursoCargoRepository, times(1)).findById(1L);
        verify(questaoRepository, times(1)).save(any(Questao.class));
    }

    @Test
    void testCreateQuestao_ConcursoNotFound() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setImageUrl("https://exemplo.com/imagem.jpg");

        when(concursoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.createQuestao(questaoDto);
        });

        verify(concursoRepository, times(1)).findById(1L);
        verify(questaoRepository, never()).save(any(Questao.class));
    }

    @Test
    void testAddCargoToQuestao_Success() {
        // Arrange
        QuestaoCargoDto questaoCargoDto = new QuestaoCargoDto();
        questaoCargoDto.setQuestaoId(1L);
        questaoCargoDto.setConcursoCargoId(2L);

        Questao questao = new Questao();
        questao.setId(1L);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(2L);

        QuestaoCargo questaoCargo = new QuestaoCargo();
        questaoCargo.setId(5L);
        questaoCargo.setQuestao(questao);
        questaoCargo.setConcursoCargo(concursoCargo);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(concursoCargoRepository.findById(2L)).thenReturn(Optional.of(concursoCargo));
        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(1L, 2L)).thenReturn(List.of()); // No existing association
        when(questaoCargoRepository.save(any(QuestaoCargo.class))).thenReturn(questaoCargo);

        // Act
        QuestaoCargoDto result = questaoService.addCargoToQuestao(questaoCargoDto);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(1L, result.getQuestaoId());
        assertEquals(2L, result.getConcursoCargoId());
        verify(questaoRepository, times(1)).findById(1L);
        verify(concursoCargoRepository, times(1)).findById(2L);
        verify(questaoCargoRepository, times(1)).findByQuestaoIdAndConcursoCargoId(1L, 2L);
        verify(questaoCargoRepository, times(1)).save(any(QuestaoCargo.class));
    }

    @Test
    void testAddCargoToQuestao_AlreadyExists() {
        // Arrange
        QuestaoCargoDto questaoCargoDto = new QuestaoCargoDto();
        questaoCargoDto.setQuestaoId(1L);
        questaoCargoDto.setConcursoCargoId(2L);

        Questao questao = new Questao();
        questao.setId(1L);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(2L);

        QuestaoCargo existingQuestaoCargo = new QuestaoCargo();
        existingQuestaoCargo.setId(5L);
        existingQuestaoCargo.setQuestao(questao);
        existingQuestaoCargo.setConcursoCargo(concursoCargo);

        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(1L, 2L)).thenReturn(Arrays.asList(existingQuestaoCargo));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.addCargoToQuestao(questaoCargoDto);
        });

        verify(questaoCargoRepository, times(1)).findByQuestaoIdAndConcursoCargoId(1L, 2L);
        verify(questaoCargoRepository, never()).save(any(QuestaoCargo.class)); // Should not save if already exists
    }

    @Test
    void testCreateQuestaoWithoutCargo_Association() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setImageUrl("https://exemplo.com/imagem.jpg");
        // Deliberately not setting concursoCargoIds to test validation

        com.studora.entity.Instituicao instituicao =
            new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023);
        concurso.setId(1L);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.createQuestao(questaoDto);
        });

        verify(concursoRepository, times(1)).findById(1L);
        verify(questaoRepository, never()).save(any(Questao.class)); // Should not save if no cargo association
    }

    @Test
    void testRemoveLastCargoFromQuestao_FailsValidation() {
        // Arrange
        Long questaoId = 1L;
        Long concursoCargoId = 2L;

        QuestaoCargo existingAssociation = new QuestaoCargo();
        existingAssociation.setQuestao(new Questao());
        existingAssociation.getQuestao().setId(questaoId);
        existingAssociation.setConcursoCargo(new ConcursoCargo());
        existingAssociation.getConcursoCargo().setId(concursoCargoId);

        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(questaoId, concursoCargoId))
            .thenReturn(Arrays.asList(existingAssociation));
        when(questaoCargoRepository.findByQuestaoId(questaoId))
            .thenReturn(Arrays.asList(existingAssociation)); // Only one association exists

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.removeCargoFromQuestao(questaoId, concursoCargoId);
        });

        verify(questaoCargoRepository, times(1)).findByQuestaoIdAndConcursoCargoId(questaoId, concursoCargoId);
        verify(questaoCargoRepository, times(1)).findByQuestaoId(questaoId);
        verify(questaoCargoRepository, never()).deleteAll(anyList()); // Should not delete if it would leave no associations
    }

    @Test
    void testGetQuestoesAnuladas_Success() {
        // Arrange
        Concurso concurso = new Concurso();
        concurso.setId(1L);

        Questao questao1 = new Questao();
        questao1.setId(1L);
        questao1.setEnunciado("Questão anulada 1");
        questao1.setAnulada(true);
        questao1.setConcurso(concurso);

        Questao questao2 = new Questao();
        questao2.setId(2L);
        questao2.setEnunciado("Questão anulada 2");
        questao2.setAnulada(true);
        questao2.setConcurso(concurso);

        List<Questao> anuladas = Arrays.asList(questao1, questao2);

        when(questaoRepository.findByAnuladaTrue()).thenReturn(anuladas);

        // Act
        List<QuestaoDto> result = questaoService.getQuestoesAnuladas();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(q -> q.getAnulada()));
        verify(questaoRepository, times(1)).findByAnuladaTrue();
    }

    @Test
    void testGetQuestoesByCargoId_Success() {
        // Arrange
        Long cargoId = 1L;

        // Create a ConcursoCargo
        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(cargoId);

        // Create Concurso
        Concurso concurso = new Concurso();
        concurso.setId(1L);

        // Create QuestaoCargo associations
        Questao questao1 = new Questao();
        questao1.setId(1L);
        questao1.setEnunciado("Questão 1");
        questao1.setConcurso(concurso);

        QuestaoCargo qc1 = new QuestaoCargo();
        qc1.setQuestao(questao1);
        qc1.setConcursoCargo(concursoCargo);

        Questao questao2 = new Questao();
        questao2.setId(2L);
        questao2.setEnunciado("Questão 2");
        questao2.setConcurso(concurso);

        QuestaoCargo qc2 = new QuestaoCargo();
        qc2.setQuestao(questao2);
        qc2.setConcursoCargo(concursoCargo);

        List<QuestaoCargo> questaoCargos = Arrays.asList(qc1, qc2);
        List<Long> questaoIds = Arrays.asList(1L, 2L);
        List<Questao> questoes = Arrays.asList(questao1, questao2);

        when(questaoCargoRepository.findByConcursoCargoId(cargoId)).thenReturn(questaoCargos);
        when(questaoRepository.findAllById(questaoIds)).thenReturn(questoes);

        // Act
        List<QuestaoDto> result = questaoService.getQuestoesByCargoId(cargoId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(questaoCargoRepository, times(1)).findByConcursoCargoId(cargoId);
        verify(questaoRepository, times(1)).findAllById(questaoIds);
    }

    @Test
    void testGetQuestoesByTemaId_Success() {
        // Arrange
        Long temaId = 1L;

        com.studora.entity.Tema tema = new com.studora.entity.Tema();
        tema.setId(temaId);
        tema.setNome("Direito Constitucional");

        Concurso concurso = new Concurso();
        concurso.setId(1L);

        Questao questao1 = new Questao();
        questao1.setId(1L);
        questao1.setEnunciado("Questão 1");
        questao1.setConcurso(concurso);

        List<Questao> questoes = Arrays.asList(questao1);

        when(temaRepository.findById(temaId)).thenReturn(Optional.of(tema));
        when(questaoRepository.findBySubtemasTemaId(temaId)).thenReturn(questoes);

        // Act
        List<QuestaoDto> result = questaoService.getQuestoesByTemaId(temaId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(temaRepository, times(1)).findById(temaId);
        verify(questaoRepository, times(1)).findBySubtemasTemaId(temaId);
    }

    @Test
    void testGetQuestoesByDisciplinaId_Success() {
        // Arrange
        Long disciplinaId = 1L;

        com.studora.entity.Disciplina disciplina = new com.studora.entity.Disciplina();
        disciplina.setId(disciplinaId);
        disciplina.setNome("Direito");

        Concurso concurso = new Concurso();
        concurso.setId(1L);

        Questao questao1 = new Questao();
        questao1.setId(1L);
        questao1.setEnunciado("Questão 1");
        questao1.setConcurso(concurso);

        List<Questao> questoes = Arrays.asList(questao1);

        when(disciplinaRepository.findById(disciplinaId)).thenReturn(Optional.of(disciplina));
        when(questaoRepository.findBySubtemasTemaDisciplinaId(disciplinaId)).thenReturn(questoes);

        // Act
        List<QuestaoDto> result = questaoService.getQuestoesByDisciplinaId(disciplinaId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(disciplinaRepository, times(1)).findById(disciplinaId);
        verify(questaoRepository, times(1)).findBySubtemasTemaDisciplinaId(disciplinaId);
    }
}
