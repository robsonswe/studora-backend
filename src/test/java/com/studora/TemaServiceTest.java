package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.tema.TemaDetailDto;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    void testFindById() {
        Disciplina disc = new Disciplina("Direito"); disc.setId(1L);
        Tema tema = new Tema();
        tema.setId(1L);
        tema.setNome("Atos");
        tema.setDisciplina(disc);

        when(temaRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(tema));
        when(estudoSubtemaRepository.countByTemaIds(any())).thenReturn(emptyObjectList());
        when(estudoSubtemaRepository.findLatestStudyDatesByTemaIds(any())).thenReturn(emptyObjectList());
        when(subtemaRepository.countByTemaIds(any())).thenReturn(emptyObjectList());
        when(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(any())).thenReturn(emptyObjectList());
        when(subtemaService.findByTemaId(1L)).thenReturn(Collections.emptyList());
        // Questao stats mocks
        when(questaoRepository.countQuestoesByTemaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.countRespondidasByTemaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.countAcertadasByTemaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.avgTempoByTemaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.findAllByTemaIdsWithDetails(any())).thenReturn(Collections.emptyList());
        // Disciplina enrichment mocks
        when(estudoSubtemaRepository.countByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(temaRepository.countByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(estudoSubtemaRepository.countDistinctStudiedTemasByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(temaRepository.findByDisciplinaId(1L)).thenReturn(Collections.emptyList());
        // Disciplina questao stats mocks
        when(questaoRepository.countQuestoesByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.countRespondidasByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.countAcertadasByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.avgTempoByDisciplinaIds(any())).thenReturn(emptyObjectList());
        when(respostaRepository.findAllByDisciplinaIdsWithDetails(any())).thenReturn(Collections.emptyList());

        TemaDetailDto result = temaService.getTemaDetailById(1L);
        assertNotNull(result);
        assertEquals("Atos", result.getNome());
        assertEquals(0L, result.getTotalEstudos());
        assertNull(result.getUltimoEstudo());
        assertEquals(0L, result.getTotalSubtemas());
        assertEquals(0L, result.getSubtemasEstudados());
        // Nested disciplina assertions
        assertNotNull(result.getDisciplina());
        assertEquals(1L, result.getDisciplina().getId());
        assertEquals("Direito", result.getDisciplina().getNome());
        assertEquals(0L, result.getDisciplina().getTotalEstudos());
        assertEquals(0L, result.getDisciplina().getTotalTemas());
        assertEquals(0L, result.getDisciplina().getTotalSubtemas());
        assertEquals(0L, result.getDisciplina().getTemasEstudados());
        assertEquals(0L, result.getDisciplina().getSubtemasEstudados());
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
        when(subtemaRepository.countByTemaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 3L}));
        when(estudoSubtemaRepository.countDistinctStudiedSubtemasByTemaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 2L}));
        when(subtemaService.findByTemaId(1L)).thenReturn(Collections.emptyList());
        // Questao stats mocks
        when(questaoRepository.countQuestoesByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.countRespondidasByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.countAcertadasByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.avgTempoByTemaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.findAllByTemaIdsWithDetails(List.of(1L))).thenReturn(Collections.emptyList());
        // Disciplina enrichment mocks
        when(estudoSubtemaRepository.countByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 10L}));
        when(estudoSubtemaRepository.findLatestStudyDatesByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(temaRepository.countByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 2L}));
        when(subtemaRepository.countByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 3L}));
        when(estudoSubtemaRepository.countDistinctStudiedSubtemasByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 2L}));
        when(estudoSubtemaRepository.countDistinctStudiedTemasByDisciplinaIds(List.of(1L))).thenReturn(listOf(new Object[]{1L, 1L}));
        when(temaRepository.findByDisciplinaId(1L)).thenReturn(java.util.List.of(tema));
        // Disciplina questao stats mocks
        when(questaoRepository.countQuestoesByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.countRespondidasByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.countAcertadasByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.avgTempoByDisciplinaIds(List.of(1L))).thenReturn(emptyObjectList());
        when(respostaRepository.findAllByDisciplinaIdsWithDetails(List.of(1L))).thenReturn(Collections.emptyList());

        TemaDetailDto result = temaService.getTemaDetailById(1L);
        assertNotNull(result);
        assertEquals("Atos", result.getNome());
        assertEquals(5L, result.getTotalEstudos());
        assertEquals(3L, result.getTotalSubtemas());
        assertEquals(2L, result.getSubtemasEstudados());
        // Nested disciplina assertions
        assertNotNull(result.getDisciplina());
        assertEquals(10L, result.getDisciplina().getTotalEstudos());
        assertEquals(2L, result.getDisciplina().getTotalTemas());
        assertEquals(3L, result.getDisciplina().getTotalSubtemas());
        assertEquals(2L, result.getDisciplina().getSubtemasEstudados());
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
