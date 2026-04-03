package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.disciplina.DisciplinaDetailDto;
import com.studora.dto.request.DisciplinaCreateRequest;
import com.studora.entity.Disciplina;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.EstudoSubtemaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.service.DisciplinaService;
import com.studora.service.TemaService;
import com.studora.mapper.DisciplinaMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    @Mock
    private SubtemaRepository subtemaRepository;
    @Mock
    private EstudoSubtemaRepository estudoSubtemaRepository;
    @Mock
    private QuestaoRepository questaoRepository;
    @Mock
    private RespostaRepository respostaRepository;
    @Mock
    private TemaService temaService;

    private DisciplinaService disciplinaService;

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
        DisciplinaMapper realMapper = org.mapstruct.factory.Mappers.getMapper(DisciplinaMapper.class);
        disciplinaService = new DisciplinaService(disciplinaRepository, temaRepository, subtemaRepository,
                estudoSubtemaRepository, questaoRepository, respostaRepository, realMapper, temaService, Runnable::run);
    }

    @Test
    void testFindById() {
        Disciplina d = new Disciplina();
        d.setId(1L);
        d.setNome("Direito");

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(d));
        when(estudoSubtemaRepository.countByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(temaRepository.countByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(subtemaRepository.countByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(temaRepository.countTemasEstudadosByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(temaService.findByDisciplinaId(1L)).thenReturn(Collections.emptyList());
        // Questao stats mocks
        when(questaoRepository.countQuestoesByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.countRespondidasByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.countAcertadasByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.avgTempoByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.findAllByDisciplinaIdsWithDetails(any())).thenReturn(Collections.emptyList());

        DisciplinaDetailDto result = disciplinaService.getDisciplinaDetailById(1L);
        assertNotNull(result);
        assertEquals("Direito", result.getNome());
        assertEquals(0L, result.getTotalEstudos());
        assertEquals(0L, result.getTotalTemas());
        assertEquals(0L, result.getTotalSubtemas());
        assertEquals(0L, result.getTemasEstudados());
        assertEquals(0L, result.getSubtemasEstudados());
    }

    @Test
    void testFindById_WithStats() {
        Disciplina d = new Disciplina();
        d.setId(1L);
        d.setNome("Direito");

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(d));
        when(estudoSubtemaRepository.countByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 10L}));
        when(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(temaRepository.countByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 2L}));
        when(subtemaRepository.countByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 4L}));
        when(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 3L}));
        // temasEstudados is now computed via native query countTemasEstudadosByDisciplinaIds
        when(temaRepository.countTemasEstudadosByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 1L}));
        when(temaService.findByDisciplinaId(1L)).thenReturn(Collections.emptyList());
        // Questao stats mocks
        when(questaoRepository.countQuestoesByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.countRespondidasByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.countAcertadasByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.avgTempoByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.findAllByDisciplinaIdsWithDetails(List.of(1L))).thenReturn(Collections.emptyList());

        DisciplinaDetailDto result = disciplinaService.getDisciplinaDetailById(1L);
        assertNotNull(result);
        assertEquals(10L, result.getTotalEstudos());
        assertEquals(2L, result.getTotalTemas());
        assertEquals(4L, result.getTotalSubtemas());
        assertEquals(3L, result.getSubtemasEstudados());
        assertEquals(1L, result.getTemasEstudados()); // only tema 10 has all subtemas studied
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
