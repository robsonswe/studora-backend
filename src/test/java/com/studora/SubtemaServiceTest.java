package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.subtema.SubtemaDetailDto;
import com.studora.dto.request.SubtemaCreateRequest;
import com.studora.entity.Tema;
import com.studora.entity.Subtema;
import com.studora.repository.TemaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.service.SubtemaService;
import com.studora.mapper.SubtemaMapper;
import com.studora.mapper.TemaMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

class SubtemaServiceTest {

    @Mock
    private SubtemaRepository subtemaRepository;
    @Mock
    private TemaRepository temaRepository;
    @Mock
    private QuestaoRepository questaoRepository;

    private SubtemaService subtemaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        SubtemaMapper realMapper = org.mapstruct.factory.Mappers.getMapper(SubtemaMapper.class);
        TemaMapper temaMapper = org.mapstruct.factory.Mappers.getMapper(TemaMapper.class);
        ReflectionTestUtils.setField(realMapper, "temaMapper", temaMapper);
        
        subtemaService = new SubtemaService(subtemaRepository, temaRepository, questaoRepository, realMapper);
    }

    @Test
    void testFindById() {
        Tema tema = new Tema(); tema.setId(1L); tema.setNome("Atos");
        Subtema sub = new Subtema();
        sub.setId(1L);
        sub.setNome("Espécies");
        sub.setTema(tema);

        when(subtemaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(sub));

        SubtemaDetailDto result = subtemaService.getSubtemaDetailById(1L);
        assertNotNull(result);
        assertEquals("Espécies", result.getNome());
    }

    @Test
    void testCreate_Success() {
        Tema tema = new Tema(); tema.setId(1L); tema.setNome("Atos");
        SubtemaCreateRequest request = new SubtemaCreateRequest();
        request.setTemaId(1L);
        request.setNome("Classificação");

        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(subtemaRepository.findByTemaIdAndNomeIgnoreCase(1L, "Classificação")).thenReturn(Optional.empty());
        when(subtemaRepository.save(any(Subtema.class))).thenAnswer(i -> {
            Subtema s = i.getArgument(0);
            s.setId(1L);
            return s;
        });

        SubtemaDetailDto result = subtemaService.create(request);
        assertEquals(1L, result.getId());
        assertEquals("Classificação", result.getNome());
    }

    @Test
    void testCreate_TemaNotFound() {
        SubtemaCreateRequest request = new SubtemaCreateRequest();
        request.setTemaId(1L);
        request.setNome("Classificação");

        when(temaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(com.studora.exception.ResourceNotFoundException.class, () -> {
            subtemaService.create(request);
        });
    }

    @Test
    void testUpdate_Success() {
        Tema t = new Tema(); t.setId(1L);
        Subtema s = new Subtema(); s.setId(1L); s.setNome("Old"); s.setTema(t);
        com.studora.dto.request.SubtemaUpdateRequest req = new com.studora.dto.request.SubtemaUpdateRequest();
        req.setNome("New"); req.setTemaId(1L);
        
        when(subtemaRepository.findById(1L)).thenReturn(Optional.of(s));
        when(temaRepository.findById(1L)).thenReturn(Optional.of(t));
        when(subtemaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        SubtemaDetailDto result = subtemaService.update(1L, req);
        assertEquals("New", result.getNome());
    }

    @Test
    void testDelete_Success() {
        Long id = 1L;
        when(subtemaRepository.existsById(id)).thenReturn(true);
        when(questaoRepository.existsBySubtemasId(id)).thenReturn(false);

        subtemaService.delete(id);

        verify(subtemaRepository, times(1)).deleteById(id);
    }

    @Test
    void testDelete_Conflict() {
        Long id = 1L;
        when(subtemaRepository.existsById(id)).thenReturn(true);
        when(questaoRepository.existsBySubtemasId(id)).thenReturn(true);

        assertThrows(com.studora.exception.ValidationException.class, () -> {
            subtemaService.delete(id);
        });
    }
}