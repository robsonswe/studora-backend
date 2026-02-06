package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.tema.TemaDetailDto;
import com.studora.dto.request.TemaCreateRequest;
import com.studora.entity.Disciplina;
import com.studora.entity.Tema;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.service.TemaService;
import com.studora.mapper.TemaMapper;
import com.studora.mapper.DisciplinaMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class TemaServiceTest {

    @Mock
    private TemaRepository temaRepository;
    @Mock
    private DisciplinaRepository disciplinaRepository;
    @Mock
    private SubtemaRepository subtemaRepository;

    private TemaService temaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        TemaMapper realMapper = org.mapstruct.factory.Mappers.getMapper(TemaMapper.class);
        DisciplinaMapper discMapper = org.mapstruct.factory.Mappers.getMapper(DisciplinaMapper.class);
        ReflectionTestUtils.setField(realMapper, "disciplinaMapper", discMapper);
        
        temaService = new TemaService(temaRepository, disciplinaRepository, subtemaRepository, realMapper);
    }

    @Test
    void testFindById() {
        Disciplina disc = new Disciplina("Direito"); disc.setId(1L);
        Tema tema = new Tema();
        tema.setId(1L);
        tema.setNome("Atos");
        tema.setDisciplina(disc);

        when(temaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(tema));

        TemaDetailDto result = temaService.getTemaDetailById(1L);
        assertNotNull(result);
        assertEquals("Atos", result.getNome());
    }

    @Test
    void testCreate_Success() {
        Disciplina disc = new Disciplina("Direito"); disc.setId(1L);
        TemaCreateRequest request = new TemaCreateRequest();
        request.setDisciplinaId(1L);
        request.setNome("Contratos");

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(disc));
        when(temaRepository.findByDisciplinaIdAndNomeIgnoreCase(1L, "Contratos")).thenReturn(Optional.empty());
        when(temaRepository.save(any(Tema.class))).thenAnswer(i -> {
            Tema t = i.getArgument(0);
            t.setId(1L);
            return t;
        });

        TemaDetailDto result = temaService.create(request);
        assertEquals(1L, result.getId());
        assertEquals("Contratos", result.getNome());
    }

    @Test
    void testCreate_DisciplinaNotFound() {
        TemaCreateRequest request = new TemaCreateRequest();
        request.setDisciplinaId(1L);
        request.setNome("Contratos");

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(com.studora.exception.ResourceNotFoundException.class, () -> {
            temaService.create(request);
        });
    }

    @Test
    void testUpdate_Success() {
        Disciplina d = new Disciplina(); d.setId(1L);
        Tema t = new Tema(); t.setId(1L); t.setNome("Old"); t.setDisciplina(d);
        com.studora.dto.request.TemaUpdateRequest req = new com.studora.dto.request.TemaUpdateRequest();
        req.setNome("New"); req.setDisciplinaId(1L);
        
        when(temaRepository.findById(1L)).thenReturn(Optional.of(t));
        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(d));
        when(temaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        TemaDetailDto result = temaService.update(1L, req);
        assertEquals("New", result.getNome());
    }

    @Test
    void testDelete_Success() {
        Long id = 1L;
        when(temaRepository.existsById(id)).thenReturn(true);
        when(subtemaRepository.existsByTemaId(id)).thenReturn(false);

        temaService.delete(id);

        verify(temaRepository, times(1)).deleteById(id);
    }

    @Test
    void testDelete_Conflict() {
        Long id = 1L;
        when(temaRepository.existsById(id)).thenReturn(true);
        when(subtemaRepository.existsByTemaId(id)).thenReturn(true);

        assertThrows(com.studora.exception.ValidationException.class, () -> {
            temaService.delete(id);
        });
    }
}