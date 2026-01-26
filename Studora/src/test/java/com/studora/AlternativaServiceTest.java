package com.studora;

import com.studora.dto.AlternativaDto;
import com.studora.entity.Alternativa;
import com.studora.entity.Questao;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.service.AlternativaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlternativaServiceTest {

    @Mock
    private AlternativaRepository alternativaRepository;

    @Mock
    private QuestaoRepository questaoRepository;

    @InjectMocks
    private AlternativaService alternativaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAlternativaById_Success() {
        // Arrange
        Long alternativaId = 1L;
        Alternativa alternativa = new Alternativa();
        alternativa.setId(alternativaId);
        alternativa.setTexto("Brasília");
        alternativa.setCorreta(true);
        Questao mockQuestao = new Questao();
        mockQuestao.setId(10L); // Set a dummy ID for the mock Questao
        alternativa.setQuestao(mockQuestao);

        when(alternativaRepository.findById(alternativaId)).thenReturn(Optional.of(alternativa));

        // Act
        AlternativaDto result = alternativaService.getAlternativaById(alternativaId);

        // Assert
        assertNotNull(result);
        assertEquals(alternativa.getTexto(), result.getTexto());
        assertTrue(result.getCorreta());
        verify(alternativaRepository, times(1)).findById(alternativaId);
    }

    @Test
    void testGetAlternativaById_NotFound() {
        // Arrange
        Long alternativaId = 1L;
        when(alternativaRepository.findById(alternativaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            alternativaService.getAlternativaById(alternativaId);
        });

        verify(alternativaRepository, times(1)).findById(alternativaId);
    }

    @Test
    void testCreateAlternativa_Success() {
        // Arrange
        AlternativaDto alternativaDto = new AlternativaDto();
        alternativaDto.setTexto("Paris");
        alternativaDto.setCorreta(true);
        alternativaDto.setQuestaoId(1L);

        Questao questao = new Questao();
        questao.setId(1L);

        Alternativa savedAlternativa = new Alternativa();
        savedAlternativa.setId(1L);
        savedAlternativa.setTexto(alternativaDto.getTexto());
        savedAlternativa.setCorreta(alternativaDto.getCorreta());
        savedAlternativa.setQuestao(questao);

        when(questaoRepository.findById(1L)).thenReturn(Optional.of(questao));
        when(alternativaRepository.save(any(Alternativa.class))).thenReturn(savedAlternativa);

        // Act
        AlternativaDto result = alternativaService.createAlternativa(alternativaDto);

        // Assert
        assertNotNull(result);
        assertEquals(savedAlternativa.getTexto(), result.getTexto());
        assertTrue(result.getCorreta());
        assertEquals(questao.getId(), result.getQuestaoId());
        verify(questaoRepository, times(1)).findById(1L);
        verify(alternativaRepository, times(1)).save(any(Alternativa.class));
    }

    @Test
    void testCreateAlternativa_QuestaoNotFound() {
        // Arrange
        AlternativaDto alternativaDto = new AlternativaDto();
        alternativaDto.setTexto("Paris");
        alternativaDto.setCorreta(true);
        alternativaDto.setQuestaoId(1L);

        when(questaoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            alternativaService.createAlternativa(alternativaDto);
        });

        verify(questaoRepository, times(1)).findById(1L);
        verify(alternativaRepository, never()).save(any(Alternativa.class));
    }
}
