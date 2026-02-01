package com.studora;

import com.studora.dto.SubtemaDto;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.service.SubtemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubtemaServiceTest {

    @Mock
    private SubtemaRepository subtemaRepository;

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private com.studora.repository.QuestaoRepository questaoRepository;

    private SubtemaService subtemaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        com.studora.mapper.SubtemaMapper realMapper = org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.SubtemaMapper.class);
        subtemaService = new SubtemaService(subtemaRepository, temaRepository, questaoRepository, realMapper);
    }

    @Test
    void testGetSubtemaById_Success() {
        // Arrange
        Long subtemaId = 1L;
        Subtema subtema = new Subtema();
        subtema.setId(subtemaId);
        subtema.setNome("Requisitos do Ato Administrativo");

        Tema tema = new Tema();
        tema.setId(1L);
        tema.setNome("Atos Administrativos");
        subtema.setTema(tema);

        when(subtemaRepository.findById(subtemaId)).thenReturn(Optional.of(subtema));

        // Act
        SubtemaDto result = subtemaService.getSubtemaById(subtemaId);

        // Assert
        assertNotNull(result);
        assertEquals(subtema.getNome(), result.getNome());
        verify(subtemaRepository, times(1)).findById(subtemaId);
    }

    @Test
    void testGetSubtemaById_NotFound() {
        // Arrange
        Long subtemaId = 1L;
        when(subtemaRepository.findById(subtemaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            subtemaService.getSubtemaById(subtemaId);
        });

        verify(subtemaRepository, times(1)).findById(subtemaId);
    }

    @Test
    void testCreateSubtema_Success() {
        // Arrange
        SubtemaDto subtemaDto = new SubtemaDto();
        subtemaDto.setNome("Atributos do Ato Administrativo");
        subtemaDto.setTemaId(1L);

        Tema tema = new Tema();
        tema.setId(1L);

        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(subtemaRepository.save(any(Subtema.class))).thenAnswer(i -> {
            Subtema s = i.getArgument(0);
            s.setId(1L);
            return s;
        });

        // Act
        SubtemaDto result = subtemaService.createSubtema(subtemaDto);

        // Assert
        assertNotNull(result);
        assertEquals("Atributos do Ato Administrativo", result.getNome());
        assertEquals(1L, result.getTemaId());
        verify(temaRepository, times(1)).findById(1L);
        verify(subtemaRepository, times(1)).save(any(Subtema.class));
    }

    @Test
    void testCreateSubtema_TemaNotFound() {
        // Arrange
        SubtemaDto subtemaDto = new SubtemaDto();
        subtemaDto.setNome("Atributos do Ato Administrativo");
        subtemaDto.setTemaId(1L);

        when(temaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            subtemaService.createSubtema(subtemaDto);
        });

        verify(temaRepository, times(1)).findById(1L);
        verify(subtemaRepository, never()).save(any(Subtema.class));
    }

    @Test
    void testDeleteSubtema_Success() {
        Long id = 1L;
        when(subtemaRepository.existsById(id)).thenReturn(true);
        when(questaoRepository.existsBySubtemasId(id)).thenReturn(false);

        subtemaService.deleteSubtema(id);

        verify(subtemaRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteSubtema_Conflict() {
        Long id = 1L;
        when(subtemaRepository.existsById(id)).thenReturn(true);
        when(questaoRepository.existsBySubtemasId(id)).thenReturn(true);

        assertThrows(com.studora.exception.ConflictException.class, () -> {
            subtemaService.deleteSubtema(id);
        });

        verify(subtemaRepository, never()).deleteById(anyLong());
    }
}
