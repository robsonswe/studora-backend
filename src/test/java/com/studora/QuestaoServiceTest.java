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
import com.studora.mapper.AlternativaMapper;
import com.studora.mapper.QuestaoCargoMapper;
import com.studora.mapper.QuestaoMapper;
import com.studora.repository.*;
import com.studora.service.QuestaoService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
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

    private QuestaoMapper questaoMapper;
    private AlternativaMapper alternativaMapper;
    private QuestaoCargoMapper questaoCargoMapper;

    private QuestaoService questaoService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Instantiate real mappers
        alternativaMapper = Mappers.getMapper(AlternativaMapper.class);
        questaoCargoMapper = Mappers.getMapper(QuestaoCargoMapper.class);
        questaoMapper = Mappers.getMapper(QuestaoMapper.class);

        // Inject dependencies into mappers if necessary (MapStruct 'spring' component model uses fields)
        // We use reflection to set the field 'alternativaMapper' in QuestaoMapperImpl
        try {
            Field field = questaoMapper.getClass().getDeclaredField("alternativaMapper");
            field.setAccessible(true);
            field.set(questaoMapper, alternativaMapper);
        } catch (NoSuchFieldException e) {
            // Ignore if field doesn't exist (might not depend on it in some versions)
        }

        // Manually inject dependencies into Service via Constructor
        questaoService = new QuestaoService(
            questaoRepository,
            concursoRepository,
            subtemaRepository,
            concursoCargoRepository,
            questaoCargoRepository,
            temaRepository,
            disciplinaRepository,
            alternativaRepository,
            respostaRepository,
            questaoMapper,
            alternativaMapper,
            questaoCargoMapper
        );
    }

    @Test
    void testGetQuestaoById_Success() {
        // Arrange
        Long questaoId = 1L;
        Questao questao = new Questao();
        questao.setId(questaoId);
        questao.setEnunciado("Qual a capital do Brasil?");

        com.studora.entity.Instituicao instituicao = new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso.setId(1L);
        questao.setConcurso(concurso);

        when(questaoRepository.findByIdWithDetails(questaoId)).thenReturn(Optional.of(questao));

        // Act
        QuestaoDto result = questaoService.getQuestaoById(questaoId);

        // Assert
        assertNotNull(result);
        assertEquals(questao.getEnunciado(), result.getEnunciado());
        verify(questaoRepository, times(1)).findByIdWithDetails(questaoId);
    }

    @Test
    void testGetQuestaoById_NotFound() {
        // Arrange
        Long questaoId = 1L;
        when(questaoRepository.findByIdWithDetails(questaoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.getQuestaoById(questaoId);
        });

        verify(questaoRepository, times(1)).findByIdWithDetails(questaoId);
    }

    @Test
    void testCreateQuestao_Success() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital do Brasil?");
        questaoDto.setConcursoId(1L);
        questaoDto.setAnulada(false);

        AlternativaDto altDto1 = new AlternativaDto();
        altDto1.setOrdem(1);
        altDto1.setTexto("Brasília");
        altDto1.setCorreta(true);

        AlternativaDto altDto2 = new AlternativaDto();
        altDto2.setOrdem(2);
        altDto2.setTexto("São Paulo");
        altDto2.setCorreta(false);

        questaoDto.setAlternativas(Arrays.asList(altDto1, altDto2));
        questaoDto.setConcursoCargoIds(Arrays.asList(1L));

        Concurso concurso = new Concurso();
        concurso.setId(1L);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(1L);
        cc.setConcurso(concurso);

        // Define behavior for saves
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(cc));
        when(questaoRepository.save(any(Questao.class))).thenAnswer(i -> {
            Questao q = i.getArgument(0);
            q.setId(1L); // Simulate ID generation
            if (q.getAlternativas() == null) q.setAlternativas(new java.util.LinkedHashSet<>());
            if (q.getQuestaoCargos() == null) q.setQuestaoCargos(new java.util.LinkedHashSet<>());
            return q;
        });
        when(alternativaRepository.save(any(com.studora.entity.Alternativa.class))).thenAnswer(i -> i.getArgument(0));
        when(questaoCargoRepository.save(any(QuestaoCargo.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        QuestaoDto result = questaoService.createQuestao(questaoDto);

        // Assert
        assertNotNull(result);
        assertEquals("Qual a capital do Brasil?", result.getEnunciado());
        verify(questaoRepository, times(1)).save(any(Questao.class));
        verify(alternativaRepository, times(2)).save(any(com.studora.entity.Alternativa.class));
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
        QuestaoCargoDto dto = new QuestaoCargoDto();
        dto.setQuestaoId(1L);
        dto.setConcursoCargoId(1L);

        Questao questao = new Questao();
        questao.setId(1L);
        Concurso concurso = new Concurso();
        concurso.setId(1L);
        questao.setConcurso(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(1L);
        cc.setConcurso(concurso);

        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(1L, 1L)).thenReturn(Arrays.asList());
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(cc));
        when(questaoCargoRepository.save(any(QuestaoCargo.class))).thenAnswer(i -> {
            QuestaoCargo qc = i.getArgument(0);
            qc.setId(1L);
            return qc;
        });

        // Act
        QuestaoCargoDto result = questaoService.addCargoToQuestao(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(questaoCargoRepository, times(1)).save(any(QuestaoCargo.class));
    }

    @Test
    void testUpdateQuestao_PreservesIdsWhenReordering() {
        // Arrange
        Long questaoId = 1L;
        Questao existingQuestao = new Questao();
        existingQuestao.setId(questaoId);
        existingQuestao.setEnunciado("Enunciado Original");
        
        // Setup existing alternatives
        com.studora.entity.Alternativa alt1 = new com.studora.entity.Alternativa();
        alt1.setId(10L);
        alt1.setOrdem(1);
        alt1.setTexto("Texto 1");
        alt1.setCorreta(true);
        
        com.studora.entity.Alternativa alt2 = new com.studora.entity.Alternativa();
        alt2.setId(11L);
        alt2.setOrdem(2);
        alt2.setTexto("Texto 2");
        alt2.setCorreta(false);
        
        java.util.Set<com.studora.entity.Alternativa> existingAlts = new java.util.LinkedHashSet<>(Arrays.asList(alt1, alt2));
        existingQuestao.setAlternativas(existingAlts);

        // Setup DTO for update: SWAP orders
        // User wants ID 10 to be Order 2, and ID 11 to be Order 1.
        QuestaoDto updateDto = new QuestaoDto();
        updateDto.setEnunciado("Enunciado Original");
        updateDto.setAnulada(false);
        
        AlternativaDto dto1 = new AlternativaDto();
        dto1.setId(11L); // ID 11
        dto1.setOrdem(1); // Moving to Order 1 (was 2)
        dto1.setTexto("Texto 2"); // Keeping text
        dto1.setCorreta(false);

        AlternativaDto dto2 = new AlternativaDto();
        dto2.setId(10L); // ID 10
        dto2.setOrdem(2); // Moving to Order 2 (was 1)
        dto2.setTexto("Texto 1"); // Keeping text
        dto2.setCorreta(true);

        updateDto.setAlternativas(Arrays.asList(dto1, dto2));

        // Mock repositories
        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(existingQuestao));
        when(questaoRepository.save(any(Questao.class))).thenReturn(existingQuestao);
        when(alternativaRepository.findByQuestaoIdOrderByOrdemAsc(questaoId)).thenReturn(new ArrayList<>(existingAlts));
        when(alternativaRepository.save(any(com.studora.entity.Alternativa.class))).thenAnswer(i -> i.getArgument(0));
        
        // Mock validation requirements
        when(questaoCargoRepository.findByQuestaoId(questaoId)).thenReturn(Arrays.asList(new QuestaoCargo()));

        // Act
        questaoService.updateQuestao(questaoId, updateDto);

        // Assert
        // Verify that the alternatives were updated correctly based on ID, not just overwriting slots
        // In the flawed logic (Order-based), ID 10 (at Order 1) would get "Texto 2".
        // In the correct logic (ID-based), ID 10 should keep "Texto 1" but have Order 2.
        
        assertEquals("Texto 1", alt1.getTexto(), "Alt 10 should keep its text");
        assertEquals(2, alt1.getOrdem(), "Alt 10 should have new order");
        
        assertEquals("Texto 2", alt2.getTexto(), "Alt 11 should keep its text");
        assertEquals(1, alt2.getOrdem(), "Alt 11 should have new order");
    }

    @Test
    void testUpdateQuestao_WithCargoModification() {
        // Arrange
        Long questaoId = 1L;
        Questao existingQuestao = new Questao();
        existingQuestao.setId(questaoId);
        existingQuestao.setEnunciado("Original");
        
        Concurso concurso = new Concurso();
        concurso.setId(1L);
        existingQuestao.setConcurso(concurso);

        // Current associations: CC 1 and CC 2
        ConcursoCargo cc1 = new ConcursoCargo(); cc1.setId(1L); cc1.setConcurso(concurso);
        ConcursoCargo cc2 = new ConcursoCargo(); cc2.setId(2L); cc2.setConcurso(concurso);
        
        QuestaoCargo qc1 = new QuestaoCargo(); qc1.setId(10L); qc1.setQuestao(existingQuestao); qc1.setConcursoCargo(cc1);
        QuestaoCargo qc2 = new QuestaoCargo(); qc2.setId(11L); qc2.setQuestao(existingQuestao); qc2.setConcursoCargo(cc2);
        
        existingQuestao.setQuestaoCargos(new java.util.LinkedHashSet<>(Arrays.asList(qc1, qc2)));

        // Update DTO: Remove CC 2, add CC 3
        QuestaoDto updateDto = new QuestaoDto();
        updateDto.setEnunciado("Original");
        updateDto.setConcursoCargoIds(Arrays.asList(1L, 3L)); // Keeping 1, Adding 3
        updateDto.setAnulada(false);
        
        AlternativaDto alt1 = new AlternativaDto(); alt1.setCorreta(true); alt1.setOrdem(1);
        AlternativaDto alt2 = new AlternativaDto(); alt2.setCorreta(false); alt2.setOrdem(2);
        updateDto.setAlternativas(Arrays.asList(alt1, alt2)); 

        // Mock CC 3
        ConcursoCargo cc3 = new ConcursoCargo(); cc3.setId(3L); cc3.setConcurso(concurso);

        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(existingQuestao));
        when(questaoRepository.save(any(Questao.class))).thenReturn(existingQuestao);
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(cc1));
        when(concursoCargoRepository.findById(3L)).thenReturn(Optional.of(cc3));
        when(questaoCargoRepository.findByQuestaoId(questaoId)).thenReturn(new ArrayList<>(existingQuestao.getQuestaoCargos()));
        when(questaoCargoRepository.save(any(QuestaoCargo.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        questaoService.updateQuestao(questaoId, updateDto);

        // Assert
        verify(questaoCargoRepository, times(1)).delete(qc2); // Removed CC 2
        verify(questaoCargoRepository, times(1)).save(any(QuestaoCargo.class)); // Added CC 3
    }

    @Test
    void testUpdateQuestao_RemoveAllCargos_Fails() {
        // Arrange
        Long questaoId = 1L;
        Questao existingQuestao = new Questao();
        existingQuestao.setId(questaoId);
        existingQuestao.setEnunciado("Original");
        Concurso concurso = new Concurso();
        concurso.setId(1L);
        existingQuestao.setConcurso(concurso);

        QuestaoDto updateDto = new QuestaoDto();
        updateDto.setEnunciado("Original");
        updateDto.setAnulada(false);
        updateDto.setConcursoCargoIds(new ArrayList<>()); // Trying to remove ALL cargos
        
        AlternativaDto alt1 = new AlternativaDto(); alt1.setCorreta(true); alt1.setOrdem(1);
        AlternativaDto alt2 = new AlternativaDto(); alt2.setCorreta(false); alt2.setOrdem(2);
        updateDto.setAlternativas(Arrays.asList(alt1, alt2));

        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(existingQuestao));
        // Important: mock save to return the entity to avoid NPE later in method
        when(questaoRepository.save(any(Questao.class))).thenReturn(existingQuestao);
        when(questaoCargoRepository.findByQuestaoId(questaoId)).thenReturn(new ArrayList<>()); 

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(com.studora.exception.ValidationException.class, () -> {
            questaoService.updateQuestao(questaoId, updateDto);
        });

        assertTrue(exception.getMessage().contains("pelo menos 1 cargo"));
    }

    @Test
    void testUpdateQuestao_ClearsExistingResponses() {
        // Arrange
        Long questaoId = 1L;
        Questao existingQuestao = new Questao();
        existingQuestao.setId(questaoId);
        existingQuestao.setEnunciado("Original");
        Concurso concurso = new Concurso();
        concurso.setId(1L);
        existingQuestao.setConcurso(concurso);

        QuestaoDto updateDto = new QuestaoDto();
        updateDto.setEnunciado("Updated");
        updateDto.setAnulada(false);
        updateDto.setConcursoCargoIds(Arrays.asList(1L));
        
        AlternativaDto alt1 = new AlternativaDto(); alt1.setCorreta(true); alt1.setOrdem(1);
        AlternativaDto alt2 = new AlternativaDto(); alt2.setCorreta(false); alt2.setOrdem(2);
        updateDto.setAlternativas(Arrays.asList(alt1, alt2));

        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(1L);
        cc.setConcurso(concurso);

        QuestaoCargo qc = new QuestaoCargo();
        qc.setQuestao(existingQuestao);
        qc.setConcursoCargo(cc);

        when(questaoRepository.findById(questaoId)).thenReturn(Optional.of(existingQuestao));
        when(questaoRepository.save(any(Questao.class))).thenReturn(existingQuestao);
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(cc));
        when(questaoCargoRepository.findByQuestaoId(questaoId)).thenReturn(Arrays.asList(qc));

        // Act
        questaoService.updateQuestao(questaoId, updateDto);

        // Assert
        verify(respostaRepository, times(1)).deleteByQuestaoId(questaoId);
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
        verify(questaoCargoRepository, never()).save(any(QuestaoCargo.class));
    }

    @Test
    void testAddCargoToQuestao_ConcursoMismatch() {
        // Arrange
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
        concursoCargo.setConcurso(concurso2);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(concursoCargoRepository.findById(2L)).thenReturn(Optional.of(concursoCargo));

        // Act & Assert
        com.studora.exception.ValidationException exception = assertThrows(com.studora.exception.ValidationException.class, () -> {
            questaoService.addCargoToQuestao(questaoCargoDto);
        });

        assertEquals("O concurso do cargo não corresponde ao concurso da questão", exception.getMessage());
        verify(questaoRepository, times(1)).findById(1L);
        verify(concursoCargoRepository, times(1)).findById(2L);
        verify(questaoCargoRepository, never()).save(any(QuestaoCargo.class));
    }

    @Test
    void testAddCargoToQuestao_NonExistentQuestao() {
        // Arrange
        QuestaoCargoDto questaoCargoDto = new QuestaoCargoDto();
        questaoCargoDto.setQuestaoId(999L);
        questaoCargoDto.setConcursoCargoId(2L);

        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(999L, 2L)).thenReturn(List.of());
        when(questaoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.addCargoToQuestao(questaoCargoDto);
        });

        verify(questaoCargoRepository, times(1)).findByQuestaoIdAndConcursoCargoId(999L, 2L);
        verify(questaoRepository, times(1)).findById(999L);
        verify(concursoCargoRepository, never()).findById(anyLong());
        verify(questaoCargoRepository, never()).save(any(QuestaoCargo.class));
    }

    @Test
    void testAddCargoToQuestao_NonExistentConcursoCargo() {
        // Arrange
        QuestaoCargoDto questaoCargoDto = new QuestaoCargoDto();
        questaoCargoDto.setQuestaoId(1L);
        questaoCargoDto.setConcursoCargoId(999L);

        Questao questao = new Questao();
        questao.setId(1L);

        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(1L, 999L)).thenReturn(List.of());
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(concursoCargoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.addCargoToQuestao(questaoCargoDto);
        });

        verify(questaoCargoRepository, times(1)).findByQuestaoIdAndConcursoCargoId(1L, 999L);
        verify(questaoRepository, times(1)).findById(1L);
        verify(concursoCargoRepository, times(1)).findById(999L);
        verify(questaoCargoRepository, never()).save(any(QuestaoCargo.class));
    }

    @Test
    void testCreateQuestao_RequiresAtLeastTwoAlternatives() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setImageUrl("https://exemplo.com/imagem.jpg");
        questaoDto.setConcursoCargoIds(Arrays.asList(1L));

        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Only Alternative");
        alt1.setCorreta(true);
        alt1.setJustificativa("Justification");

        questaoDto.setAlternativas(Arrays.asList(alt1));

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
        verify(questaoRepository, never()).save(any(Questao.class));
    }

    @Test
    void testCreateQuestao_WithZeroCorrectAlternatives_FailsValidation() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setAnulada(false);
        questaoDto.setConcursoCargoIds(Arrays.asList(1L));

        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Paris");
        alt1.setCorreta(false);
        alt1.setJustificativa("Not the correct answer");

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("London");
        alt2.setCorreta(false);
        alt2.setJustificativa("Not the correct answer");

        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));

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

        assertEquals("Uma questão deve ter exatamente 1 alternativa correta", exception.getMessage());
        verify(questaoRepository, never()).save(any(Questao.class));
    }

    @Test
    void testCreateQuestao_WithMultipleCorrectAlternatives_FailsValidation() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setAnulada(false);
        questaoDto.setConcursoCargoIds(Arrays.asList(1L));

        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Paris");
        alt1.setCorreta(true);
        alt1.setJustificativa("Correct answer");

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("London");
        alt2.setCorreta(true);
        alt2.setJustificativa("Also correct (but invalid)");

        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));

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

        assertEquals("Uma questão deve ter exatamente 1 alternativa correta", exception.getMessage());
        verify(questaoRepository, never()).save(any(Questao.class));
    }

    @Test
    void testCreateQuestao_WithExactlyOneCorrectAlternative_Succeeds() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setAnulada(false);
        questaoDto.setConcursoCargoIds(Arrays.asList(1L));

        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Paris");
        alt1.setCorreta(true);
        alt1.setJustificativa("Correct answer");

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("London");
        alt2.setCorreta(false);
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

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(concursoCargo));
        when(questaoRepository.save(any(Questao.class))).thenAnswer(i -> {
            Questao q = i.getArgument(0);
            if (q.getAlternativas() == null) q.setAlternativas(new java.util.LinkedHashSet<>());
            if (q.getQuestaoCargos() == null) q.setQuestaoCargos(new java.util.LinkedHashSet<>());
            return q;
        });
        when(alternativaRepository.save(any(com.studora.entity.Alternativa.class))).thenAnswer(i -> i.getArgument(0));
        when(questaoCargoRepository.save(any(QuestaoCargo.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        QuestaoDto result = questaoService.createQuestao(questaoDto);

        // Assert
        assertNotNull(result);
        verify(concursoRepository, times(1)).findById(1L);
        verify(questaoRepository, times(1)).save(any(Questao.class));
        verify(alternativaRepository, times(2)).save(any(com.studora.entity.Alternativa.class));
    }

    @Test
    void testCreateQuestao_WithExactlyOneCorrectAlternative_AnuladaQuestion_Succeeds() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setAnulada(true);
        questaoDto.setConcursoCargoIds(Arrays.asList(1L));

        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Paris");
        alt1.setCorreta(true);

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("London");
        alt2.setCorreta(true);

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

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(concursoCargoRepository.findById(1L)).thenReturn(Optional.of(concursoCargo));
        when(questaoRepository.save(any(Questao.class))).thenAnswer(i -> {
            Questao q = i.getArgument(0);
            if (q.getAlternativas() == null) q.setAlternativas(new java.util.LinkedHashSet<>());
            if (q.getQuestaoCargos() == null) q.setQuestaoCargos(new java.util.LinkedHashSet<>());
            return q;
        });
        when(alternativaRepository.save(any(com.studora.entity.Alternativa.class))).thenAnswer(i -> i.getArgument(0));
        when(questaoCargoRepository.save(any(QuestaoCargo.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        QuestaoDto result = questaoService.createQuestao(questaoDto);

        // Assert
        assertNotNull(result);
        verify(concursoRepository, times(1)).findById(1L);
        verify(questaoRepository, times(1)).save(any(Questao.class));
        verify(alternativaRepository, times(2)).save(any(com.studora.entity.Alternativa.class));
    }

    @Test
    void testCreateQuestaoWithoutCargo_Association() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);
        questaoDto.setImageUrl("https://exemplo.com/imagem.jpg");

        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Alternativa A");
        alt1.setCorreta(true);

        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("Alternativa B");
        alt2.setCorreta(false);

        questaoDto.setAlternativas(Arrays.asList(alt1, alt2));

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
        assertThrows(RuntimeException.class, () -> {
            questaoService.createQuestao(questaoDto);
        });

        verify(concursoRepository, times(1)).findById(1L);
        verify(questaoRepository, never()).save(any(Questao.class));
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
        verify(questaoCargoRepository, never()).deleteAll(anyList());
    }
}
