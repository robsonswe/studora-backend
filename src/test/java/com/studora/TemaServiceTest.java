package com.studora;

import com.studora.dto.TemaDto;
import com.studora.entity.Disciplina;
import com.studora.entity.Tema;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.TemaRepository;
import com.studora.service.TemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TemaServiceTest {

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private com.studora.repository.SubtemaRepository subtemaRepository;

    @InjectMocks
    private TemaService temaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTemaById_Success() {
        // Arrange
        Long temaId = 1L;
        Tema tema = new Tema();
        tema.setId(temaId);
        tema.setNome("Controle de Constitucionalidade");

        Disciplina disciplina = new Disciplina();
        disciplina.setId(1L);
        disciplina.setNome("Direito Constitucional");
        tema.setDisciplina(disciplina);

        when(temaRepository.findById(temaId)).thenReturn(Optional.of(tema));

        // Act
        TemaDto result = temaService.getTemaById(temaId);

        // Assert
        assertNotNull(result);
        assertEquals(tema.getNome(), result.getNome());
        verify(temaRepository, times(1)).findById(temaId);
    }

    @Test
    void testGetTemaById_NotFound() {
        // Arrange
        Long temaId = 1L;
        when(temaRepository.findById(temaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            temaService.getTemaById(temaId);
        });

        verify(temaRepository, times(1)).findById(temaId);
    }

    @Test
    void testCreateTema_Success() {
        // Arrange
        TemaDto temaDto = new TemaDto();
        temaDto.setNome("Atos Administrativos");
        temaDto.setDisciplinaId(1L);

        Disciplina disciplina = new Disciplina();
        disciplina.setId(1L);

        Tema savedTema = new Tema();
        savedTema.setId(1L);
        savedTema.setNome(temaDto.getNome());
        savedTema.setDisciplina(disciplina);

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disciplina));
        when(temaRepository.save(any(Tema.class))).thenReturn(savedTema);

        // Act
        TemaDto result = temaService.createTema(temaDto);

        // Assert
        assertNotNull(result);
        assertEquals(savedTema.getNome(), result.getNome());
        assertEquals(disciplina.getId(), result.getDisciplinaId());
        verify(disciplinaRepository, times(1)).findById(1L);
        verify(temaRepository, times(1)).save(any(Tema.class));
    }

    @Test
    void testCreateTema_DisciplinaNotFound() {
        // Arrange
        TemaDto temaDto = new TemaDto();
        temaDto.setNome("Atos Administrativos");
        temaDto.setDisciplinaId(1L);

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            temaService.createTema(temaDto);
        });

        verify(disciplinaRepository, times(1)).findById(1L);
        verify(temaRepository, never()).save(any(Tema.class));
    }

    @Test
    void testDeleteTema_Success() {
        Long id = 1L;
        when(temaRepository.existsById(id)).thenReturn(true);
        when(subtemaRepository.existsByTemaId(id)).thenReturn(false);

        temaService.deleteTema(id);

        verify(temaRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteTema_Conflict() {
        Long id = 1L;
        when(temaRepository.existsById(id)).thenReturn(true);
        when(subtemaRepository.existsByTemaId(id)).thenReturn(true);

        assertThrows(com.studora.exception.ConflictException.class, () -> {
            temaService.deleteTema(id);
        });

        verify(temaRepository, never()).deleteById(anyLong());
    }
}
