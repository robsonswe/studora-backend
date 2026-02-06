package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.disciplina.DisciplinaDetailDto;
import com.studora.dto.request.DisciplinaCreateRequest;
import com.studora.entity.Disciplina;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.TemaRepository;
import com.studora.service.DisciplinaService;
import com.studora.mapper.DisciplinaMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class DisciplinaServiceTest {

    @Mock
    private DisciplinaRepository disciplinaRepository;
    @Mock
    private TemaRepository temaRepository;

    private DisciplinaService disciplinaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        DisciplinaMapper realMapper = org.mapstruct.factory.Mappers.getMapper(DisciplinaMapper.class);
        disciplinaService = new DisciplinaService(disciplinaRepository, temaRepository, realMapper);
    }

    @Test
    void testFindById() {
        Disciplina d = new Disciplina();
        d.setId(1L);
        d.setNome("Direito");

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(d));

        DisciplinaDetailDto result = disciplinaService.getDisciplinaDetailById(1L);
        assertNotNull(result);
        assertEquals("Direito", result.getNome());
    }

    @Test
    void testCreate_Success() {
        DisciplinaCreateRequest request = new DisciplinaCreateRequest();
        request.setNome("Matemática");

        when(disciplinaRepository.findByNomeIgnoreCase("Matemática")).thenReturn(Optional.empty());
        when(disciplinaRepository.save(any(Disciplina.class))).thenAnswer(i -> {
            Disciplina d = i.getArgument(0);
            d.setId(1L);
            return d;
        });

        DisciplinaDetailDto result = disciplinaService.create(request);
        assertEquals(1L, result.getId());
        assertEquals("Matemática", result.getNome());
    }

    @Test
    void testCreate_DuplicateCaseInsensitive() {
        DisciplinaCreateRequest req = new DisciplinaCreateRequest();
        req.setNome("direto");

        Disciplina existing = new Disciplina();
        existing.setId(2L);
        existing.setNome("DIREITO");

        when(disciplinaRepository.findByNomeIgnoreCase("direto")).thenReturn(Optional.of(existing));

        assertThrows(com.studora.exception.ConflictException.class, () -> {
            disciplinaService.create(req);
        });
    }

    @Test
    void testUpdate_Success() {
        Disciplina d = new Disciplina(); d.setId(1L); d.setNome("Old");
        com.studora.dto.request.DisciplinaUpdateRequest req = new com.studora.dto.request.DisciplinaUpdateRequest();
        req.setNome("New");
        
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(d));
        when(disciplinaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        DisciplinaDetailDto result = disciplinaService.update(1L, req);
        assertEquals("New", result.getNome());
    }

    @Test
    void testDelete_Success() {
        Long id = 1L;
        when(disciplinaRepository.existsById(id)).thenReturn(true);
        when(temaRepository.existsByDisciplinaId(id)).thenReturn(false);

        disciplinaService.delete(id);

        verify(disciplinaRepository, times(1)).deleteById(id);
    }

    @Test
    void testDelete_Conflict() {
        Long id = 1L;
        when(disciplinaRepository.existsById(id)).thenReturn(true);
        when(temaRepository.existsByDisciplinaId(id)).thenReturn(true);

        assertThrows(com.studora.exception.ValidationException.class, () -> {
            disciplinaService.delete(id);
        });
    }
}