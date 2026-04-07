package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.MetricsLevel;
import com.studora.dto.tema.TemaDetailDto;
import com.studora.dto.tema.TemaSummaryDto;
import com.studora.dto.request.TemaCreateRequest;
import com.studora.entity.Disciplina;
import com.studora.entity.Tema;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.EstudoSubtemaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.service.TemaService;
import com.studora.service.SubtemaService;
import com.studora.mapper.TemaMapper;
import com.studora.mapper.DisciplinaMapper;
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

class TemaServiceTest {

    @Mock
    private TemaRepository temaRepository;
    @Mock
    private DisciplinaRepository disciplinaRepository;
    @Mock
    private SubtemaRepository subtemaRepository;
    @Mock
    private EstudoSubtemaRepository estudoSubtemaRepository;
    @Mock
    private QuestaoRepository questaoRepository;
    @Mock
    private RespostaRepository respostaRepository;
    @Mock
    private SubtemaService subtemaService;

    private TemaService temaService;

    private static List<Object[]> emptyObjectList() {
        return new ArrayList<>();
    }

    private static List<Object[]> listOf(Object[]... items) {
        List<Object[]> list = new ArrayList<>();
        for (Object[] item : items) list.add(item);
        return list;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        TemaMapper realMapper = org.mapstruct.factory.Mappers.getMapper(TemaMapper.class);
        DisciplinaMapper discMapper = org.mapstruct.factory.Mappers.getMapper(DisciplinaMapper.class);
        ReflectionTestUtils.setField(realMapper, "disciplinaMapper", discMapper);

        temaService = new TemaService(temaRepository, disciplinaRepository, subtemaRepository,
                estudoSubtemaRepository, questaoRepository, respostaRepository, realMapper, subtemaService, Runnable::run);
    }

    @Test
    void testFindById_Lean() {
        Disciplina disc = new Disciplina("Direito"); disc.setId(1L);
        Tema tema = new Tema();
        tema.setId(1L);
        tema.setNome("Atos");
        tema.setDisciplina(disc);

        when(temaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(tema));
        when(subtemaService.findByTemaId(1L, null)).thenReturn(Collections.emptyList());

        TemaDetailDto result = temaService.getTemaDetailById(1L, null);
        assertNotNull(result);
        assertEquals("Atos", result.getNome());
        assertNull(result.getTotalEstudos());
        assertNotNull(result.getDisciplina());
        assertEquals(1L, result.getDisciplina().getId());
        assertNull(result.getDisciplina().getTotalEstudos());
    }

    @Test
    void testFindById_WithStats() {
        Disciplina disc = new Disciplina("Direito"); disc.setId(1L);
        Tema tema = new Tema();
        tema.setId(1L);
        tema.setNome("Atos");
        tema.setDisciplina(disc);

        when(temaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(tema));
        when(estudoSubtemaRepository.countByTemaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 5L}));
        when(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.findLatestResponseDatesByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(subtemaRepository.countByTemaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 3L}));
        when(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 2L}));
        when(questaoRepository.countQuestoesByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.countRespondidasByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.countAcertadasByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.avgTempoByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.getDificuldadeStatsByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        // Disciplina enrichment
        when(estudoSubtemaRepository.countByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 10L}));
        when(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.findLatestResponseDatesByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(temaRepository.countByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 2L}));
        when(subtemaRepository.countByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 3L}));
        when(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 2L}));
        when(temaRepository.countTemasEstudadosByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 1L}));
        when(questaoRepository.countQuestoesByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.countRespondidasByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.countAcertadasByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.avgTempoByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.getDificuldadeStatsByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(subtemaService.findByTemaId(1L, MetricsLevel.FULL)).thenReturn(Collections.emptyList());

        TemaDetailDto result = temaService.getTemaDetailById(1L, MetricsLevel.FULL);
        assertNotNull(result);
        assertEquals(5L, result.getTotalEstudos());
        assertEquals(3L, result.getTotalSubtemas());
        assertEquals(2L, result.getSubtemasEstudados());
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

        temaService.create(request);
        verify(temaRepository).save(any(Tema.class));
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

        temaService.update(1L, req);
        verify(temaRepository).save(any());
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

    @Test
    void testFindAll_Lean() {
        Disciplina disc = new Disciplina("Direito"); disc.setId(1L);
        Tema tema = new Tema(); tema.setId(1L); tema.setNome("Atos"); tema.setDisciplina(disc);
        Page<Tema> page = new PageImpl<>(List.of(tema));

        when(temaRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<TemaSummaryDto> result = temaService.findAll(null, Pageable.unpaged(), null);
        assertEquals(1, result.getTotalElements());
        assertNull(result.getContent().get(0).getTotalEstudos());
    }

    @Test
    void testFindByDisciplinaId_Lean() {
        Disciplina disc = new Disciplina("Direito"); disc.setId(1L);
        Tema tema = new Tema(); tema.setId(1L); tema.setNome("Atos"); tema.setDisciplina(disc);

        when(disciplinaRepository.existsById(1L)).thenReturn(true);
        when(temaRepository.findByDisciplinaId(1L)).thenReturn(List.of(tema));

        List<TemaSummaryDto> result = temaService.findByDisciplinaId(1L, null);
        assertEquals(1, result.size());
        assertNull(result.get(0).getTotalEstudos());
    }
}
