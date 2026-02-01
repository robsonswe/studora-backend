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
}
