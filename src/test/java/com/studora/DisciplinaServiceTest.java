package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.MetricsLevel;
import com.studora.dto.disciplina.DisciplinaDetailDto;
import com.studora.dto.disciplina.DisciplinaSummaryDto;
import com.studora.dto.request.DisciplinaCreateRequest;
import com.studora.dto.request.DisciplinaUpdateRequest;
import com.studora.entity.Disciplina;
import com.studora.mapper.DisciplinaMapper;
import com.studora.mapper.TemaMapper;
import com.studora.mapper.SubtemaMapper;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.EstudoSubtemaRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.service.DisciplinaService;
import com.studora.service.TemaService;
import com.studora.mapper.DisciplinaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private com.studora.service.StatsAssembler statsAssembler;

    private DisciplinaService disciplinaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        DisciplinaMapper realMapper = org.mapstruct.factory.Mappers.getMapper(DisciplinaMapper.class);
        TemaMapper temaMapper = org.mapstruct.factory.Mappers.getMapper(TemaMapper.class);
        SubtemaMapper subtemaMapper = org.mapstruct.factory.Mappers.getMapper(SubtemaMapper.class);
        disciplinaService = new DisciplinaService(disciplinaRepository, temaRepository, subtemaRepository,
                estudoSubtemaRepository, realMapper, temaMapper, subtemaMapper, statsAssembler);
    }

    @Test
    void testFindById() {
        Disciplina d = new Disciplina();
        d.setId(1L);
        d.setNome("Direito");

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(d));

        DisciplinaDetailDto result = disciplinaService.getDisciplinaDetailById(1L, null);
        assertNotNull(result);
        assertEquals("Direito", result.getNome());
        assertNull(result.getQuestaoStats());
    }

    @Test
    void testFindById_WithStats() {
        Disciplina d = new Disciplina();
        d.setId(1L);
        d.setNome("Direito");

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(d));

        com.studora.dto.QuestaoStatsDto mockStats = new com.studora.dto.QuestaoStatsDto();
        when(statsAssembler.buildStats(1L, "DISCIPLINA", MetricsLevel.FULL)).thenReturn(mockStats);

        DisciplinaDetailDto result = disciplinaService.getDisciplinaDetailById(1L, MetricsLevel.FULL);
        assertNotNull(result);
        assertEquals("Direito", result.getNome());
        assertNotNull(result.getQuestaoStats());
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

        disciplinaService.create(request);
        verify(disciplinaRepository).save(any(Disciplina.class));
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
        DisciplinaUpdateRequest req = new DisciplinaUpdateRequest();
        req.setNome("New");

        when(disciplinaRepository.findById(1L)).thenReturn(Optional.of(d));
        when(disciplinaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        disciplinaService.update(1L, req);
        verify(disciplinaRepository).save(any());
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

    @Test
    void testFindAll_Lean() {
        Disciplina d = new Disciplina(); d.setId(1L); d.setNome("Direito");
        Page<Disciplina> page = new PageImpl<>(List.of(d));

        when(disciplinaRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<DisciplinaSummaryDto> result = disciplinaService.findAll(null, Pageable.unpaged(), null);
        assertEquals(1, result.getTotalElements());
        assertEquals("Direito", result.getContent().get(0).getNome());
    }
}
