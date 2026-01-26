package com.studora;

import com.studora.dto.ConcursoDto;
import com.studora.entity.Concurso;
import com.studora.repository.ConcursoRepository;
import com.studora.service.ConcursoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConcursoServiceTest {

    @Mock
    private ConcursoRepository concursoRepository;

    @InjectMocks
    private ConcursoService concursoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllConcursos() {
        // Arrange
        Concurso concurso1 = new Concurso("Concurso 1", "Banca 1", 2023, "Cargo 1", "Nível 1", "Área 1");
        concurso1.setId(1L);
        Concurso concurso2 = new Concurso("Concurso 2", "Banca 2", 2024, "Cargo 2", "Nível 2", "Área 2");
        concurso2.setId(2L);

        when(concursoRepository.findAll()).thenReturn(Arrays.asList(concurso1, concurso2));

        // Act
        List<ConcursoDto> result = concursoService.getAllConcursos();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Concurso 1", result.get(0).getNome());
        assertEquals("Concurso 2", result.get(1).getNome());

        verify(concursoRepository, times(1)).findAll();
    }

    @Test
    void testGetAllConcursos_Empty() {
        // Arrange
        when(concursoRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ConcursoDto> result = concursoService.getAllConcursos();

        // Assert
        assertTrue(result.isEmpty());
        verify(concursoRepository, times(1)).findAll();
    }

    @Test
    void testGetConcursoById_Success() {
        // Arrange
        Concurso concurso = new Concurso("Concurso 1", "Banca 1", 2023, "Cargo 1", "Nível 1", "Área 1");
        concurso.setId(1L);
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));

        // Act
        ConcursoDto result = concursoService.getConcursoById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Concurso 1", result.getNome());
        verify(concursoRepository, times(1)).findById(1L);
    }

    @Test
    void testGetConcursoById_NotFound() {
        // Arrange
        when(concursoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoService.getConcursoById(1L);
        });

        verify(concursoRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateConcurso() {
        // Arrange
        ConcursoDto concursoDto = new ConcursoDto("Concurso 1", "Banca 1", 2023, "Cargo 1", "Nível 1", "Área 1");
        Concurso savedConcurso = new Concurso("Concurso 1", "Banca 1", 2023, "Cargo 1", "Nível 1", "Área 1");
        savedConcurso.setId(1L);

        when(concursoRepository.save(any(Concurso.class))).thenReturn(savedConcurso);

        // Act
        ConcursoDto result = concursoService.createConcurso(concursoDto);

        // Assert
        assertNotNull(result);
        assertEquals("Concurso 1", result.getNome());
        verify(concursoRepository, times(1)).save(any(Concurso.class));
    }

    @Test
    void testCreateConcurso_NullDto() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            concursoService.createConcurso(null);
        });
    }

    @Test
    void testUpdateConcurso_Success() {
        // Arrange
        Long concursoId = 1L;
        ConcursoDto concursoDto = new ConcursoDto("Updated Concurso", "Updated Banca", 2024, "Updated Cargo", "Updated Nível", "Updated Área");
        Concurso existingConcurso = new Concurso("Old Concurso", "Old Banca", 2023, "Old Cargo", "Old Nível", "Old Área");
        existingConcurso.setId(concursoId);

        when(concursoRepository.findById(concursoId)).thenReturn(Optional.of(existingConcurso));
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ConcursoDto updatedDto = concursoService.updateConcurso(concursoId, concursoDto);

        // Assert
        assertNotNull(updatedDto);
        assertEquals(concursoDto.getNome(), updatedDto.getNome());
        assertEquals(concursoDto.getBanca(), updatedDto.getBanca());
        assertEquals(concursoDto.getAno(), updatedDto.getAno());
        assertEquals(concursoDto.getCargo(), updatedDto.getCargo());
        assertEquals(concursoDto.getNivel(), updatedDto.getNivel());
        assertEquals(concursoDto.getArea(), updatedDto.getArea());

        verify(concursoRepository, times(1)).findById(concursoId);
        verify(concursoRepository, times(1)).save(any(Concurso.class));
    }

    @Test
    void testUpdateConcurso_NotFound() {
        // Arrange
        Long concursoId = 1L;
        ConcursoDto concursoDto = new ConcursoDto("Updated Concurso", "Updated Banca", 2024, "Updated Cargo", "Updated Nível", "Updated Área");

        when(concursoRepository.findById(concursoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoService.updateConcurso(concursoId, concursoDto);
        });

        verify(concursoRepository, times(1)).findById(concursoId);
        verify(concursoRepository, never()).save(any(Concurso.class));
    }

    @Test
    void testDeleteConcurso_Success() {
        // Arrange
        Long concursoId = 1L;

        when(concursoRepository.existsById(concursoId)).thenReturn(true);
        doNothing().when(concursoRepository).deleteById(concursoId);

        // Act
        concursoService.deleteConcurso(concursoId);

        // Assert
        verify(concursoRepository, times(1)).existsById(concursoId);
        verify(concursoRepository, times(1)).deleteById(concursoId);
    }

    @Test
    void testDeleteConcurso_NotFound() {
        // Arrange
        Long concursoId = 1L;

        when(concursoRepository.existsById(concursoId)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoService.deleteConcurso(concursoId);
        });

        verify(concursoRepository, times(1)).existsById(concursoId);
        verify(concursoRepository, never()).deleteById(anyLong());
    }
}