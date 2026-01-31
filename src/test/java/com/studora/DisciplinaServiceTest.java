package com.studora;

import com.studora.dto.DisciplinaDto;
import com.studora.entity.Disciplina;
import com.studora.repository.DisciplinaRepository;
import com.studora.service.DisciplinaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DisciplinaServiceTest {

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private com.studora.repository.TemaRepository temaRepository;

    @InjectMocks
    private DisciplinaService disciplinaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetDisciplinaById_Success() {
        // Arrange
        Long disciplinaId = 1L;
        Disciplina disciplina = new Disciplina();
        disciplina.setId(disciplinaId);
        disciplina.setNome("Direito Constitucional");

        when(disciplinaRepository.findById(disciplinaId)).thenReturn(Optional.of(disciplina));

        // Act
        DisciplinaDto result = disciplinaService.getDisciplinaById(disciplinaId);

        // Assert
        assertNotNull(result);
        assertEquals(disciplina.getNome(), result.getNome());
        verify(disciplinaRepository, times(1)).findById(disciplinaId);
    }

    @Test
    void testGetDisciplinaById_NotFound() {
        // Arrange
        Long disciplinaId = 1L;
        when(disciplinaRepository.findById(disciplinaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            disciplinaService.getDisciplinaById(disciplinaId);
        });

        verify(disciplinaRepository, times(1)).findById(disciplinaId);
    }

    @Test
    void testCreateDisciplina_Success() {
        // Arrange
        DisciplinaDto disciplinaDto = new DisciplinaDto();
        disciplinaDto.setNome("Direito Administrativo");

        Disciplina savedDisciplina = new Disciplina();
        savedDisciplina.setId(1L);
        savedDisciplina.setNome(disciplinaDto.getNome());

        when(disciplinaRepository.save(any(Disciplina.class))).thenReturn(savedDisciplina);

        // Act
        DisciplinaDto result = disciplinaService.createDisciplina(disciplinaDto);

        // Assert
        assertNotNull(result);
        assertEquals(savedDisciplina.getNome(), result.getNome());
        verify(disciplinaRepository, times(1)).save(any(Disciplina.class));
    }

    @Test
    void testDeleteDisciplina_Success() {
        Long id = 1L;
        when(disciplinaRepository.existsById(id)).thenReturn(true);
        when(temaRepository.existsByDisciplinaId(id)).thenReturn(false);

        disciplinaService.deleteDisciplina(id);

        verify(disciplinaRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteDisciplina_Conflict() {
        Long id = 1L;
        when(disciplinaRepository.existsById(id)).thenReturn(true);
        when(temaRepository.existsByDisciplinaId(id)).thenReturn(true);

        assertThrows(com.studora.exception.ConflictException.class, () -> {
            disciplinaService.deleteDisciplina(id);
        });

        verify(disciplinaRepository, never()).deleteById(anyLong());
    }
}
