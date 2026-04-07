package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.MetricsLevel;
import com.studora.dto.subtema.SubtemaDetailDto;
import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.dto.request.SubtemaCreateRequest;
import com.studora.entity.Tema;
import com.studora.entity.Subtema;
import com.studora.repository.TemaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.service.SubtemaService;
import com.studora.mapper.SubtemaMapper;
import com.studora.mapper.TemaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class SubtemaServiceTest {

    @Mock
    private SubtemaRepository subtemaRepository;
    @Mock
    private TemaRepository temaRepository;
    @Mock
    private QuestaoRepository questaoRepository;
    @Mock
    private RespostaRepository respostaRepository;
    @Mock
    private com.studora.repository.EstudoSubtemaRepository estudoSubtemaRepository;

    private SubtemaService subtemaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        SubtemaMapper realMapper = org.mapstruct.factory.Mappers.getMapper(SubtemaMapper.class);
        TemaMapper temaMapper = org.mapstruct.factory.Mappers.getMapper(TemaMapper.class);
        ReflectionTestUtils.setField(realMapper, "temaMapper", temaMapper);

        subtemaService = new SubtemaService(subtemaRepository, temaRepository, questaoRepository, respostaRepository, estudoSubtemaRepository, realMapper, Runnable::run);
    }

    @Test
    void testFindById_Lean() {
        Tema tema = new Tema(); tema.setId(1L); tema.setNome("Atos");
        Subtema sub = new Subtema();
        sub.setId(1L);
        sub.setNome("Espécies");
        sub.setTema(tema);

        when(subtemaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(sub));

        SubtemaDetailDto result = subtemaService.getSubtemaDetailById(1L, null);
        assertNotNull(result);
        assertEquals("Espécies", result.getNome());
        assertNull(result.getTotalEstudos());
    }

    @Test
    void testFindById_WithStats() {
        Tema tema = new Tema(); tema.setId(1L); tema.setNome("Atos");
        Subtema sub = new Subtema();
        sub.setId(1L);
        sub.setNome("Espécies");
        sub.setTema(tema);

        when(subtemaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(sub));
        when(estudoSubtemaRepository.countBySubtemaId(1L)).thenReturn(5L);
        when(estudoSubtemaRepository.findFirstBySubtemaIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.empty());
        when(respostaRepository.findLatestResponseDatesBySubtemaIds(List.of(1L))).thenReturn(new ArrayList<>());
        when(questaoRepository.countQuestoesBySubtemaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 10L}));
        when(respostaRepository.countRespondidasBySubtemaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 7L}));
        when(respostaRepository.countAcertadasBySubtemaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 5L}));
        when(respostaRepository.avgTempoBySubtemaIds(List.of(1L))).thenReturn(new ArrayList<>());
        when(respostaRepository.getDificuldadeStatsBySubtemaIds(List.of(1L))).thenReturn(new ArrayList<>());

        SubtemaDetailDto result = subtemaService.getSubtemaDetailById(1L, MetricsLevel.FULL);
        assertNotNull(result);
        assertEquals("Espécies", result.getNome());
        assertEquals(5L, result.getTotalEstudos());
        assertEquals(10L, result.getTotalQuestoes());
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

        subtemaService.create(request);
        verify(subtemaRepository).save(any(Subtema.class));
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

        subtemaService.update(1L, req);
        verify(subtemaRepository).save(any());
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

    @Test
    void testFindAll_Lean() {
        Tema tema = new Tema(); tema.setId(1L);
        Subtema sub = new Subtema(); sub.setId(1L); sub.setNome("Espécies"); sub.setTema(tema);
        Page<Subtema> page = new PageImpl<>(List.of(sub));

        when(subtemaRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<SubtemaSummaryDto> result = subtemaService.findAll(null, Pageable.unpaged(), null);
        assertEquals(1, result.getTotalElements());
        assertNull(result.getContent().get(0).getTotalEstudos());
    }

    @Test
    void testFindByTemaId_Lean() {
        Tema tema = new Tema(); tema.setId(1L);
        Subtema sub = new Subtema(); sub.setId(1L); sub.setNome("Espécies"); sub.setTema(tema);

        when(temaRepository.existsById(1L)).thenReturn(true);
        when(subtemaRepository.findByTemaId(1L)).thenReturn(List.of(sub));

        List<SubtemaSummaryDto> result = subtemaService.findByTemaId(1L, null);
        assertEquals(1, result.size());
        assertNull(result.get(0).getTotalEstudos());
    }

    private static List<Object[]> listOf(Object[]... items) {
        List<Object[]> list = new ArrayList<>();
        for (Object[] item : items) list.add(item);
        return list;
    }
}
