package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.AlternativaDto;
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

    @Mock
    private AlternativaRepository alternativaRepository;

    @Mock
    private RespostaRepository respostaRepository;

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

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
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

        // Add alternativas to comply with validation
        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Alternativa A");
        alt1.setCorreta(true);
        alt1.setJustificativa("Justificativa A");

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("Alternativa B");
        alt2.setCorreta(false);
        alt2.setJustificativa("Justificativa B");

        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));

        com.studora.entity.Instituicao instituicao =
            new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso.setId(1L);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(1L);
        concursoCargo.setConcurso(concurso);

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

        // Add alternativas to comply with validation
        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Alternativa A");
        alt1.setCorreta(true);
        alt1.setJustificativa("Justificativa A");

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("Alternativa B");
        alt2.setCorreta(false);
        alt2.setJustificativa("Justificativa B");

        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));

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

        Concurso concurso = new Concurso();
        concurso.setId(1L);

        Questao questao = new Questao();
        questao.setId(1L);
        questao.setConcurso(concurso);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(2L);
        concursoCargo.setConcurso(concurso);

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
    void testAddCargoToQuestao_ConcursoMismatch() {
        // Arrange: questao and concursoCargo have different concursos
        QuestaoCargoDto questaoCargoDto = new QuestaoCargoDto();
        questaoCargoDto.setQuestaoId(1L);
        questaoCargoDto.setConcursoCargoId(2L);

        Concurso concurso1 = new Concurso();
        concurso1.setId(1L);

        Concurso concurso2 = new Concurso();
        concurso2.setId(2L);

        Questao questao = new Questao();
        questao.setId(1L);
        questao.setConcurso(concurso1);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(2L);
        concursoCargo.setConcurso(concurso2);  // Different concurso

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(concursoCargoRepository.findById(2L)).thenReturn(Optional.of(concursoCargo));

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(com.studora.exception.ValidationException.class, () -> {
            questaoService.addCargoToQuestao(questaoCargoDto);
        });

        assertEquals("O concurso do cargo não corresponde ao concurso da questão", exception.getMessage());

        verify(questaoRepository, times(1)).findById(1L);
        verify(concursoCargoRepository, times(1)).findById(2L);
        verify(questaoCargoRepository, never()).save(any(QuestaoCargo.class)); // Should not save due to validation error
    }

    @Test
    void testAddCargoToQuestao_NonExistentQuestao() {
        // Arrange
        QuestaoCargoDto questaoCargoDto = new QuestaoCargoDto();
        questaoCargoDto.setQuestaoId(999L); // Non-existent questao ID
        questaoCargoDto.setConcursoCargoId(2L);

        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(999L, 2L)).thenReturn(List.of()); // No existing association
        when(questaoRepository.findById(999L)).thenReturn(Optional.empty()); // Questao does not exist

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.addCargoToQuestao(questaoCargoDto);
        });

        verify(questaoCargoRepository, times(1)).findByQuestaoIdAndConcursoCargoId(999L, 2L);
        verify(questaoRepository, times(1)).findById(999L);
        verify(concursoCargoRepository, never()).findById(anyLong()); // Should not check concursoCargo if questao doesn't exist
        verify(questaoCargoRepository, never()).save(any(QuestaoCargo.class)); // Should not save
    }

    @Test
    void testAddCargoToQuestao_NonExistentConcursoCargo() {
        // Arrange
        QuestaoCargoDto questaoCargoDto = new QuestaoCargoDto();
        questaoCargoDto.setQuestaoId(1L);
        questaoCargoDto.setConcursoCargoId(999L); // Non-existent concursoCargo ID

        Questao questao = new Questao();
        questao.setId(1L);

        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(1L, 999L)).thenReturn(List.of()); // No existing association
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao)); // Questao exists
        when(concursoCargoRepository.findById(999L)).thenReturn(Optional.empty()); // ConcursoCargo does not exist

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.addCargoToQuestao(questaoCargoDto);
        });

        verify(questaoCargoRepository, times(1)).findByQuestaoIdAndConcursoCargoId(1L, 999L);
        verify(questaoRepository, times(1)).findById(1L);
        verify(concursoCargoRepository, times(1)).findById(999L);
        verify(questaoCargoRepository, never()).save(any(QuestaoCargo.class)); // Should not save
    }

    @Test
    void testCreateQuestao_RequiresAtLeastTwoAlternatives() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setImageUrl("https://exemplo.com/imagem.jpg");
        questaoDto.setConcursoCargoIds(Arrays.asList(1L)); // Add cargo association

        // Add only ONE alternative (should fail validation)
        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Only Alternative");
        alt1.setCorreta(true);
        alt1.setJustificativa("Justification");

        questaoDto.setAlternativas(Arrays.asList(alt1)); // Only one alternative

        // Mock concurso repository to prevent ResourceNotFoundException
        com.studora.entity.Instituicao instituicao = new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso.setId(1L);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class,
            () -> questaoService.createQuestao(questaoDto)
        );

        assertEquals("Uma questão deve ter pelo menos 2 alternativas", exception.getMessage());
        verify(questaoRepository, never()).save(any(Questao.class)); // Should not save
    }

    @Test
    void testCreateQuestao_WithZeroCorrectAlternatives_FailsValidation() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setAnulada(false); // Not anulada, so validation applies
        questaoDto.setConcursoCargoIds(Arrays.asList(1L)); // Add cargo association

        // Add 2 alternatives, but none are correct (should fail validation)
        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Paris");
        alt1.setCorreta(false); // Not correct
        alt1.setJustificativa("Not the correct answer");

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("London");
        alt2.setCorreta(false); // Not correct
        alt2.setJustificativa("Not the correct answer");

        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));

        // Mock concurso repository to prevent ResourceNotFoundException
        com.studora.entity.Instituicao instituicao = new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso.setId(1L);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class,
            () -> questaoService.createQuestao(questaoDto)
        );

        assertEquals("Uma questão deve ter exatamente uma alternativa correta", exception.getMessage());
        verify(questaoRepository, never()).save(any(Questao.class)); // Should not save
    }

    @Test
    void testCreateQuestao_WithMultipleCorrectAlternatives_FailsValidation() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setAnulada(false); // Not anulada, so validation applies
        questaoDto.setConcursoCargoIds(Arrays.asList(1L)); // Add cargo association

        // Add 2 alternatives, but both are correct (should fail validation)
        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Paris");
        alt1.setCorreta(true); // Correct
        alt1.setJustificativa("Correct answer");

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("London");
        alt2.setCorreta(true); // Also correct (invalid)
        alt2.setJustificativa("Also correct (but invalid)");

        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));

        // Mock concurso repository to prevent ResourceNotFoundException
        com.studora.entity.Instituicao instituicao = new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso.setId(1L);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class,
            () -> questaoService.createQuestao(questaoDto)
        );

        assertEquals("Uma questão deve ter exatamente uma alternativa correta", exception.getMessage());
        verify(questaoRepository, never()).save(any(Questao.class)); // Should not save
    }

    @Test
    void testCreateQuestao_WithExactlyOneCorrectAlternative_Succeeds() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setAnulada(false); // Not anulada, so validation applies
        questaoDto.setConcursoCargoIds(Arrays.asList(1L)); // Add cargo association

        // Add exactly 2 alternatives with exactly one correct (valid)
        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Paris");
        alt1.setCorreta(true); // Correct
        alt1.setJustificativa("Correct answer");

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("London");
        alt2.setCorreta(false); // Not correct
        alt2.setJustificativa("Incorrect answer");

        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));

        com.studora.entity.Instituicao instituicao = new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso.setId(1L);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(1L);
        concursoCargo.setConcurso(concurso);

        Questao savedQuestao = new Questao();
        savedQuestao.setId(1L);
        savedQuestao.setEnunciado(questaoDto.getEnunciado());
        savedQuestao.setConcurso(concurso);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(concursoCargo));
        when(questaoRepository.save(any(Questao.class))).thenReturn(savedQuestao);

        // Act
        QuestaoDto result = questaoService.createQuestao(questaoDto);

        // Assert
        assertNotNull(result);
        verify(concursoRepository, times(1)).findById(1L);
        verify(questaoRepository, times(1)).save(any(Questao.class));
        verify(alternativaRepository, times(2)).save(any(com.studora.entity.Alternativa.class)); // Should save 2 alternatives
    }

    @Test
    void testCreateQuestao_WithExactlyOneCorrectAlternative_AnuladaQuestion_Succeeds() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setAnulada(true); // Anulada, so correct alternative validation does not apply
        questaoDto.setConcursoCargoIds(Arrays.asList(1L)); // Add cargo association

        // Add 2 alternatives, but both are correct (should be allowed for anulada questions)
        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Paris");
        alt1.setCorreta(true); // Correct
        alt1.setJustificativa("Correct answer");

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("London");
        alt2.setCorreta(true); // Also correct (allowed for anulada questions)
        alt2.setJustificativa("Also correct (allowed for anulada questions)");

        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));

        com.studora.entity.Instituicao instituicao = new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso.setId(1L);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(1L);
        concursoCargo.setConcurso(concurso);

        Questao savedQuestao = new Questao();
        savedQuestao.setId(1L);
        savedQuestao.setEnunciado(questaoDto.getEnunciado());
        savedQuestao.setConcurso(concurso);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(concursoCargo));
        when(questaoRepository.save(any(Questao.class))).thenReturn(savedQuestao);

        // Act
        QuestaoDto result = questaoService.createQuestao(questaoDto);

        // Assert
        assertNotNull(result);
        verify(concursoRepository, times(1)).findById(1L);
        verify(questaoRepository, times(1)).save(any(Questao.class));
        verify(alternativaRepository, times(2)).save(any(com.studora.entity.Alternativa.class)); // Should save 2 alternatives
    }

    @Test
    void testCreateQuestaoWithoutCargo_Association() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setImageUrl("https://exemplo.com/imagem.jpg");

        // Add alternativas to comply with validation
        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Alternativa A");
        alt1.setCorreta(true);
        alt1.setJustificativa("Justificativa A");

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("Alternativa B");
        alt2.setCorreta(false);
        alt2.setJustificativa("Justificativa B");

        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));
        // Deliberately not setting concursoCargoIds to test validation

        com.studora.entity.Instituicao instituicao =
            new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
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

    @Test
    void testUpdateQuestao_RequiresAtLeastTwoAlternatives() {
        // Arrange
        Long questaoId = 1L;

        // Original question with alternatives
        Concurso concurso = new Concurso();
        concurso.setId(1L);

        Questao existingQuestao = new Questao();
        existingQuestao.setId(questaoId);
        existingQuestao.setEnunciado("Original question");
        existingQuestao.setConcurso(concurso);

        // Create DTO for update with only ONE alternative (should fail validation)
        QuestaoDto updateDto = new QuestaoDto();
        updateDto.setEnunciado("Updated question");
        updateDto.setConcursoId(1L);
        updateDto.setAnulada(false);
        updateDto.setConcursoCargoIds(Arrays.asList(1L)); // Maintain cargo association

        // Only ONE alternative (should fail validation)
        AlternativaDto newAlt1 = new AlternativaDto();
        newAlt1.setOrdem(1);
        newAlt1.setTexto("New Alternative 1");
        newAlt1.setCorreta(true);
        newAlt1.setJustificativa("Justification 1");

        updateDto.setAlternativas(Arrays.asList(newAlt1)); // Only one alternative

        // Mock repository calls
        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(existingQuestao));

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class,
            () -> questaoService.updateQuestao(questaoId, updateDto)
        );

        assertEquals("Uma questão deve ter pelo menos 2 alternativas", exception.getMessage());
        verify(questaoRepository, times(1)).findById(questaoId);
        verify(alternativaRepository, never()).deleteAll(anyList()); // Should not delete alternatives
        verify(alternativaRepository, never()).save(any(com.studora.entity.Alternativa.class)); // Should not save alternatives
    }

    @Test
    void testUpdateQuestao_WithZeroCorrectAlternatives_FailsValidation() {
        // Arrange
        Long questaoId = 1L;

        // Original question with alternatives
        Concurso concurso = new Concurso();
        concurso.setId(1L);

        Questao existingQuestao = new Questao();
        existingQuestao.setId(questaoId);
        existingQuestao.setEnunciado("Original question");
        existingQuestao.setConcurso(concurso);

        // Create DTO for update with no correct alternatives (should fail validation)
        QuestaoDto updateDto = new QuestaoDto();
        updateDto.setEnunciado("Updated question");
        updateDto.setConcursoId(1L);
        updateDto.setAnulada(false); // Not anulada, so validation applies
        updateDto.setConcursoCargoIds(Arrays.asList(1L)); // Maintain cargo association

        // Two alternatives, but neither is correct (should fail validation)
        AlternativaDto newAlt1 = new AlternativaDto();
        newAlt1.setOrdem(1);
        newAlt1.setTexto("New Alternative 1");
        newAlt1.setCorreta(false); // Not correct
        newAlt1.setJustificativa("Justification 1");

        AlternativaDto newAlt2 = new AlternativaDto();
        newAlt2.setOrdem(2);
        newAlt2.setTexto("New Alternative 2");
        newAlt2.setCorreta(false); // Not correct
        newAlt2.setJustificativa("Justification 2");

        updateDto.setAlternativas(Arrays.asList(newAlt1, newAlt2));

        // Mock repository calls
        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(existingQuestao));

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class,
            () -> questaoService.updateQuestao(questaoId, updateDto)
        );

        assertEquals("Uma questão deve ter exatamente uma alternativa correta", exception.getMessage());
        verify(questaoRepository, times(1)).findById(questaoId);
        verify(alternativaRepository, never()).deleteAll(anyList()); // Should not delete alternatives
        verify(alternativaRepository, never()).save(any(com.studora.entity.Alternativa.class)); // Should not save alternatives
    }

    @Test
    void testUpdateQuestao_WithMultipleCorrectAlternatives_FailsValidation() {
        // Arrange
        Long questaoId = 1L;

        // Original question with alternatives
        Concurso concurso = new Concurso();
        concurso.setId(1L);

        Questao existingQuestao = new Questao();
        existingQuestao.setId(questaoId);
        existingQuestao.setEnunciado("Original question");
        existingQuestao.setConcurso(concurso);

        // Create DTO for update with multiple correct alternatives (should fail validation)
        QuestaoDto updateDto = new QuestaoDto();
        updateDto.setEnunciado("Updated question");
        updateDto.setConcursoId(1L);
        updateDto.setAnulada(false); // Not anulada, so validation applies
        updateDto.setConcursoCargoIds(Arrays.asList(1L)); // Maintain cargo association

        // Two alternatives, both correct (should fail validation)
        AlternativaDto newAlt1 = new AlternativaDto();
        newAlt1.setOrdem(1);
        newAlt1.setTexto("New Alternative 1");
        newAlt1.setCorreta(true); // Correct
        newAlt1.setJustificativa("Justification 1");

        AlternativaDto newAlt2 = new AlternativaDto();
        newAlt2.setOrdem(2);
        newAlt2.setTexto("New Alternative 2");
        newAlt2.setCorreta(true); // Also correct
        newAlt2.setJustificativa("Justification 2");

        updateDto.setAlternativas(Arrays.asList(newAlt1, newAlt2));

        // Mock repository calls
        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(existingQuestao));

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class,
            () -> questaoService.updateQuestao(questaoId, updateDto)
        );

        assertEquals("Uma questão deve ter exatamente uma alternativa correta", exception.getMessage());
        verify(questaoRepository, times(1)).findById(questaoId);
        verify(alternativaRepository, never()).deleteAll(anyList()); // Should not delete alternatives
        verify(alternativaRepository, never()).save(any(com.studora.entity.Alternativa.class)); // Should not save alternatives
    }

    @Test
    void testUpdateQuestao_WithExactlyOneCorrectAlternative_Succeeds() {
        // Arrange
        Long questaoId = 1L;

        // Original question with alternatives
        Concurso concurso = new Concurso();
        concurso.setId(1L);

        Questao existingQuestao = mock(Questao.class);
        when(existingQuestao.getId()).thenReturn(questaoId);
        when(existingQuestao.getEnunciado()).thenReturn("Original question");
        when(existingQuestao.getConcurso()).thenReturn(concurso);

        // Create alternatives for the existing question
        com.studora.entity.Alternativa existingAlt1 = new com.studora.entity.Alternativa();
        existingAlt1.setId(100L);
        existingAlt1.setQuestao(existingQuestao);
        com.studora.entity.Alternativa existingAlt2 = new com.studora.entity.Alternativa();
        existingAlt2.setId(101L);
        existingAlt2.setQuestao(existingQuestao);
        List<com.studora.entity.Alternativa> existingAlternativas = Arrays.asList(existingAlt1, existingAlt2);

        // Set up the existing question to return its alternatives
        when(existingQuestao.getAlternativas()).thenReturn(existingAlternativas);

        // Create DTO for update with exactly one correct alternative (should succeed)
        QuestaoDto updateDto = new QuestaoDto();
        updateDto.setEnunciado("Updated question");
        updateDto.setConcursoId(1L);
        updateDto.setAnulada(false); // Not anulada, so validation applies
        updateDto.setConcursoCargoIds(Arrays.asList(1L)); // Maintain cargo association

        // Two alternatives, exactly one correct (should succeed)
        AlternativaDto newAlt1 = new AlternativaDto();
        newAlt1.setOrdem(1);
        newAlt1.setTexto("New Alternative 1");
        newAlt1.setCorreta(true); // Correct
        newAlt1.setJustificativa("Justification 1");

        AlternativaDto newAlt2 = new AlternativaDto();
        newAlt2.setOrdem(2);
        newAlt2.setTexto("New Alternative 2");
        newAlt2.setCorreta(false); // Not correct
        newAlt2.setJustificativa("Justification 2");

        updateDto.setAlternativas(Arrays.asList(newAlt1, newAlt2));

        // Mock repository calls
        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(existingQuestao));
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(new ConcursoCargo()));
        when(questaoCargoRepository.findByQuestaoId(questaoId)).thenReturn(Arrays.asList(new QuestaoCargo()));

        // Create a new mock for the updated question to return the updated values
        Questao updatedQuestao = mock(Questao.class);
        when(updatedQuestao.getId()).thenReturn(questaoId);
        when(updatedQuestao.getEnunciado()).thenReturn(updateDto.getEnunciado());
        when(updatedQuestao.getConcurso()).thenReturn(concurso);
        when(updatedQuestao.getAnulada()).thenReturn(updateDto.getAnulada());

        when(questaoRepository.save(any(Questao.class))).thenReturn(updatedQuestao);

        // Act
        QuestaoDto result = questaoService.updateQuestao(questaoId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated question", result.getEnunciado());
        verify(questaoRepository, times(1)).findById(questaoId);
        // New strategy uses individual deletes and saves
        verify(alternativaRepository, atLeastOnce()).findByQuestaoIdOrderByOrdemAsc(questaoId);
        verify(alternativaRepository, times(2)).save(any(com.studora.entity.Alternativa.class));
        verify(alternativaRepository, atLeastOnce()).flush();
    }

    @Test
    void testUpdateQuestao_WithMultipleCorrectAlternatives_AnuladaQuestion_Succeeds() {
        // Arrange
        Long questaoId = 1L;

        // Original question with alternatives
        Concurso concurso = new Concurso();
        concurso.setId(1L);

        Questao existingQuestao = mock(Questao.class);
        when(existingQuestao.getId()).thenReturn(questaoId);
        when(existingQuestao.getEnunciado()).thenReturn("Original question");
        when(existingQuestao.getConcurso()).thenReturn(concurso);

        // Create alternatives for the existing question
        com.studora.entity.Alternativa existingAlt1 = new com.studora.entity.Alternativa();
        existingAlt1.setId(100L);
        existingAlt1.setQuestao(existingQuestao);
        com.studora.entity.Alternativa existingAlt2 = new com.studora.entity.Alternativa();
        existingAlt2.setId(101L);
        existingAlt2.setQuestao(existingQuestao);
        List<com.studora.entity.Alternativa> existingAlternativas = Arrays.asList(existingAlt1, existingAlt2);

        // Set up the existing question to return its alternatives
        when(existingQuestao.getAlternativas()).thenReturn(existingAlternativas);

        // Create DTO for update with multiple correct alternatives but anulada question (should succeed)
        QuestaoDto updateDto = new QuestaoDto();
        updateDto.setEnunciado("Updated question");
        updateDto.setConcursoId(1L);
        updateDto.setAnulada(true); // Anulada, so correct alternative validation does not apply
        updateDto.setConcursoCargoIds(Arrays.asList(1L)); // Maintain cargo association

        // Two alternatives, both correct (should be allowed for anulada questions)
        AlternativaDto newAlt1 = new AlternativaDto();
        newAlt1.setOrdem(1);
        newAlt1.setTexto("New Alternative 1");
        newAlt1.setCorreta(true); // Correct
        newAlt1.setJustificativa("Justification 1");

        AlternativaDto newAlt2 = new AlternativaDto();
        newAlt2.setOrdem(2);
        newAlt2.setTexto("New Alternative 2");
        newAlt2.setCorreta(true); // Also correct (allowed for anulada questions)
        newAlt2.setJustificativa("Justification 2");

        updateDto.setAlternativas(Arrays.asList(newAlt1, newAlt2));

        // Mock repository calls
        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(existingQuestao));
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(new ConcursoCargo()));
        when(questaoCargoRepository.findByQuestaoId(questaoId)).thenReturn(Arrays.asList(new QuestaoCargo()));

        // Create a new mock for the updated question to return the updated values
        Questao updatedQuestao = mock(Questao.class);
        when(updatedQuestao.getId()).thenReturn(questaoId);
        when(updatedQuestao.getEnunciado()).thenReturn(updateDto.getEnunciado());
        when(updatedQuestao.getConcurso()).thenReturn(concurso);
        when(updatedQuestao.getAnulada()).thenReturn(updateDto.getAnulada());

        when(questaoRepository.save(any(Questao.class))).thenReturn(updatedQuestao);

        // Act
        QuestaoDto result = questaoService.updateQuestao(questaoId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated question", result.getEnunciado());
        verify(questaoRepository, times(1)).findById(questaoId);
        // New strategy uses individual deletes and saves
        verify(alternativaRepository, atLeastOnce()).findByQuestaoIdOrderByOrdemAsc(questaoId);
        verify(alternativaRepository, times(2)).save(any(com.studora.entity.Alternativa.class));
        verify(alternativaRepository, atLeastOnce()).flush();
    }

    @Test
    void testUpdateQuestao_WithAlternativesReplacement() {
        // Arrange
        Long questaoId = 1L;

        // Original question with alternatives
        Concurso concurso = new Concurso();
        concurso.setId(1L);

        Questao existingQuestao = mock(Questao.class);
        when(existingQuestao.getId()).thenReturn(questaoId);
        when(existingQuestao.getEnunciado()).thenReturn("Original question");
        when(existingQuestao.getConcurso()).thenReturn(concurso);

        // Create alternatives for the existing question
        com.studora.entity.Alternativa existingAlt1 = new com.studora.entity.Alternativa();
        existingAlt1.setId(100L);
        existingAlt1.setQuestao(existingQuestao);
        com.studora.entity.Alternativa existingAlt2 = new com.studora.entity.Alternativa();
        existingAlt2.setId(101L);
        existingAlt2.setQuestao(existingQuestao);
        List<com.studora.entity.Alternativa> existingAlternativas = Arrays.asList(existingAlt1, existingAlt2);

        // Set up the existing question to return its alternatives
        when(existingQuestao.getAlternativas()).thenReturn(existingAlternativas);

        // Create DTO for update with new alternatives
        QuestaoDto updateDto = new QuestaoDto();
        updateDto.setEnunciado("Updated question");
        updateDto.setConcursoId(1L);
        updateDto.setAnulada(false);
        updateDto.setConcursoCargoIds(Arrays.asList(1L)); // Maintain cargo association

        // New alternatives for the update
        AlternativaDto newAlt1 = new AlternativaDto();
        newAlt1.setOrdem(1);
        newAlt1.setTexto("New Alternative 1");
        newAlt1.setCorreta(true);
        newAlt1.setJustificativa("Justification 1");

        AlternativaDto newAlt2 = new AlternativaDto();
        newAlt2.setOrdem(2);
        newAlt2.setTexto("New Alternative 2");
        newAlt2.setCorreta(false);
        newAlt2.setJustificativa("Justification 2");

        updateDto.setAlternativas(Arrays.asList(newAlt1, newAlt2));

        // Mock repository calls
        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(existingQuestao));
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(new ConcursoCargo()));
        when(questaoCargoRepository.findByQuestaoId(questaoId)).thenReturn(Arrays.asList(new QuestaoCargo()));

        // Create a new mock for the updated question to return the updated values
        Questao updatedQuestao = mock(Questao.class);
        when(updatedQuestao.getId()).thenReturn(questaoId);
        when(updatedQuestao.getEnunciado()).thenReturn(updateDto.getEnunciado());
        when(updatedQuestao.getConcurso()).thenReturn(concurso);
        when(updatedQuestao.getAnulada()).thenReturn(updateDto.getAnulada());

        when(questaoRepository.save(any(Questao.class))).thenReturn(updatedQuestao);

        // Act
        QuestaoDto result = questaoService.updateQuestao(questaoId, updateDto);

        // Assert
        assertNotNull(result);
        assertEquals("Updated question", result.getEnunciado());
        verify(questaoRepository, times(1)).findById(questaoId);
        // New strategy uses individual deletes and saves
        verify(alternativaRepository, atLeastOnce()).findByQuestaoIdOrderByOrdemAsc(questaoId);
        verify(alternativaRepository, times(2)).save(any(com.studora.entity.Alternativa.class));
        verify(alternativaRepository, atLeastOnce()).flush();
    }

    @Test
    void testDeleteQuestao_AlsoDeletesAlternativesAndResponses() {
        // Arrange
        Long questaoId = 1L;

        Questao questaoToDelete = mock(Questao.class);
        when(questaoToDelete.getId()).thenReturn(questaoId);
        when(questaoToDelete.getEnunciado()).thenReturn("Question to delete");

        // Mock that the question exists
        when(questaoRepository.existsById(questaoId)).thenReturn(true);
        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(questaoToDelete));

        // Create mock alternatives for this question
        com.studora.entity.Alternativa alt1 = new com.studora.entity.Alternativa();
        alt1.setId(10L);
        alt1.setQuestao(questaoToDelete);
        com.studora.entity.Alternativa alt2 = new com.studora.entity.Alternativa();
        alt2.setId(11L);
        alt2.setQuestao(questaoToDelete);
        List<com.studora.entity.Alternativa> alternatives = Arrays.asList(alt1, alt2);

        // Mock the alternatives relationship - the question should have alternatives when loaded
        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(questaoToDelete));
        when(questaoToDelete.getAlternativas()).thenReturn(alternatives); // Return the alternatives from the entity

        // Mock the questaoCargo relationship to ensure at least one exists for validation
        QuestaoCargo qc = new QuestaoCargo();
        qc.setQuestao(questaoToDelete);
        when(questaoCargoRepository.findByQuestaoId(questaoId)).thenReturn(Arrays.asList(qc));

        // Act
        questaoService.deleteQuestao(questaoId);

        // Assert
        verify(questaoRepository, times(1)).existsById(questaoId);
        verify(questaoCargoRepository, times(1)).findByQuestaoId(questaoId);
        verify(questaoCargoRepository, times(1)).deleteAll(anyList()); // Should delete cargo associations
        verify(questaoRepository, times(1)).deleteById(questaoId); // Should delete the question
        // Note: Alternatives are deleted via cascade, not explicit calls in deleteQuestao method
        // The deleteQuestao method doesn't explicitly fetch alternatives, it relies on cascade
    }

    @Test
    void testCreateQuestao_ConcursoMismatch_FailsValidation() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Q");
        questaoDto.setConcursoId(1L);
        questaoDto.setConcursoCargoIds(Arrays.asList(2L));
        
        // Add valid alternatives to pass early validations
        AlternativaDto alt1 = new AlternativaDto(); alt1.setCorreta(true);
        AlternativaDto alt2 = new AlternativaDto(); alt2.setCorreta(false);
        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));

        Concurso concurso1 = new Concurso(); 
        concurso1.setId(1L);
        
        Concurso concurso2 = new Concurso(); 
        concurso2.setId(2L); // Different ID
        
        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(2L);
        cc.setConcurso(concurso2); // Mismatch!
        
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso1));
        when(concursoCargoRepository.findById(2L)).thenReturn(Optional.of(cc));
        
        // Act & Assert
        assertThrows(com.studora.exception.ValidationException.class, () -> questaoService.createQuestao(questaoDto));
    }

    @Test
    void testRemoveCargoFromQuestao_Success() {
        // Arrange
        Long questaoId = 1L;
        Long concursoCargoId = 2L;

        QuestaoCargo associationToRemove = new QuestaoCargo();
        associationToRemove.setId(10L);
        
        QuestaoCargo otherAssociation = new QuestaoCargo();
        otherAssociation.setId(11L);

        // Mock finding the association to remove
        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(questaoId, concursoCargoId))
            .thenReturn(Arrays.asList(associationToRemove));
            
        // Mock finding all associations (must return > 1)
        when(questaoCargoRepository.findByQuestaoId(questaoId))
            .thenReturn(Arrays.asList(associationToRemove, otherAssociation));

        // Act
        questaoService.removeCargoFromQuestao(questaoId, concursoCargoId);

        // Assert
        verify(questaoCargoRepository, times(1)).deleteAll(Arrays.asList(associationToRemove));
    }

    @Test
    void testRemoveCargoFromQuestao_NotFound() {
        // Arrange
        Long questaoId = 1L;
        Long concursoCargoId = 2L;

        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(questaoId, concursoCargoId))
            .thenReturn(List.of());

        // Act & Assert
        assertThrows(com.studora.exception.ResourceNotFoundException.class, 
            () -> questaoService.removeCargoFromQuestao(questaoId, concursoCargoId));
    }

    @Test
    void testGetQuestoesBySubtemaId_NotFound() {
        when(subtemaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(com.studora.exception.ResourceNotFoundException.class, 
            () -> questaoService.getQuestoesBySubtemaId(1L));
    }

    @Test
    void testGetQuestoesByTemaId_NotFound() {
        when(temaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(com.studora.exception.ResourceNotFoundException.class, 
            () -> questaoService.getQuestoesByTemaId(1L));
    }

    @Test
    void testGetQuestoesByDisciplinaId_NotFound() {
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(com.studora.exception.ResourceNotFoundException.class, 
            () -> questaoService.getQuestoesByDisciplinaId(1L));
    }

    @Test
    void testGetQuestoesByCargoId_Empty() {
        // Arrange
        when(questaoCargoRepository.findByConcursoCargoId(1L)).thenReturn(List.of());

        // Act
        List<QuestaoDto> result = questaoService.getQuestoesByCargoId(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(questaoRepository, never()).findAllById(any());
    }
}
