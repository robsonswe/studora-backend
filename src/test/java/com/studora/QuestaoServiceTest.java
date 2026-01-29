package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.QuestaoDto;
import com.studora.entity.Concurso;
import com.studora.entity.Questao;
import com.studora.repository.*;
import com.studora.service.QuestaoService;
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

        com.studora.entity.Instituicao instituicao =
            new com.studora.entity.Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição");

        com.studora.entity.Banca banca = new com.studora.entity.Banca();
        banca.setId(1L);
        banca.setNome("Banca");

        Concurso concurso = new Concurso(instituicao, banca, 2023);
        concurso.setId(1L);

        Questao savedQuestao = new Questao();
        savedQuestao.setId(1L);
        savedQuestao.setEnunciado(questaoDto.getEnunciado());
        savedQuestao.setConcurso(concurso);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
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
        verify(questaoRepository, times(1)).save(any(Questao.class));
    }

    @Test
    void testCreateQuestao_ConcursoNotFound() {
        // Arrange
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital da França?");
        questaoDto.setConcursoId(1L);

        when(concursoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            questaoService.createQuestao(questaoDto);
        });

        verify(concursoRepository, times(1)).findById(1L);
        verify(questaoRepository, never()).save(any(Questao.class));
    }
}
