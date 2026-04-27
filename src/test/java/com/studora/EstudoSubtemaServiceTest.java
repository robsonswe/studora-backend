package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.subtema.EstudoSubtemaDto;
import com.studora.entity.EstudoSubtema;
import com.studora.entity.Subtema;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.EstudoSubtemaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.service.EstudoSubtemaService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class EstudoSubtemaServiceTest {

    @Mock
    private EstudoSubtemaRepository estudoSubtemaRepository;

    @Mock
    private SubtemaRepository subtemaRepository;

    private EstudoSubtemaService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EstudoSubtemaService(estudoSubtemaRepository, subtemaRepository);
    }

    // ==================== markAsStudied ====================

    @Test
    void markAsStudied_subtemaExists_savesAndReturnsId() {
        Subtema subtema = new Subtema();
        subtema.setId(1L);

        EstudoSubtema saved = new EstudoSubtema(subtema);
        saved.setId(42L);

        when(subtemaRepository.findById(1L)).thenReturn(Optional.of(subtema));
        when(estudoSubtemaRepository.save(any(EstudoSubtema.class))).thenReturn(saved);

        Long result = service.markAsStudied(1L);

        assertEquals(42L, result);
        verify(estudoSubtemaRepository).save(any(EstudoSubtema.class));
        verify(estudoSubtemaRepository).flush();
    }

    @Test
    void markAsStudied_subtemaNotFound_throwsResourceNotFoundException() {
        when(subtemaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.markAsStudied(99L));
        verify(estudoSubtemaRepository, never()).save(any());
    }

    // ==================== deleteEstudo ====================

    @Test
    void deleteEstudo_exists_deletesSuccessfully() {
        when(estudoSubtemaRepository.existsById(10L)).thenReturn(true);

        service.deleteEstudo(10L);

        verify(estudoSubtemaRepository).deleteById(10L);
        verify(estudoSubtemaRepository).flush();
    }

    @Test
    void deleteEstudo_notFound_throwsResourceNotFoundException() {
        when(estudoSubtemaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteEstudo(99L));
        verify(estudoSubtemaRepository, never()).deleteById(any());
    }

    // ==================== getEstudosBySubtema ====================

    @Test
    void getEstudosBySubtema_returnsMappedDtos() {
        Subtema subtema = new Subtema();
        subtema.setId(1L);

        EstudoSubtema e1 = new EstudoSubtema(subtema);
        e1.setId(1L);

        EstudoSubtema e2 = new EstudoSubtema(subtema);
        e2.setId(2L);

        when(estudoSubtemaRepository.findBySubtemaIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(e1, e2));

        List<EstudoSubtemaDto> result = service.getEstudosBySubtema(1L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getSubtemaId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void getEstudosBySubtema_noStudies_returnsEmptyList() {
        when(estudoSubtemaRepository.findBySubtemaIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.emptyList());

        List<EstudoSubtemaDto> result = service.getEstudosBySubtema(1L);

        assertTrue(result.isEmpty());
    }

    // ==================== getStudyCount ====================

    @Test
    void getStudyCount_returnsRepositoryCount() {
        when(estudoSubtemaRepository.countBySubtemaId(1L)).thenReturn(5L);

        long count = service.getStudyCount(1L);

        assertEquals(5L, count);
    }

    @Test
    void getStudyCount_noStudies_returnsZero() {
        when(estudoSubtemaRepository.countBySubtemaId(1L)).thenReturn(0L);

        long count = service.getStudyCount(1L);

        assertEquals(0L, count);
    }
}
