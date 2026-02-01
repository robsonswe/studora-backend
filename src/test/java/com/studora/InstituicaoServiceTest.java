package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.InstituicaoDto;
import com.studora.entity.Instituicao;
import com.studora.repository.InstituicaoRepository;
import com.studora.service.InstituicaoService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class InstituicaoServiceTest {

    @Mock
    private InstituicaoRepository instituicaoRepository;

    @Mock
    private com.studora.repository.ConcursoRepository concursoRepository;

    private InstituicaoService instituicaoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        com.studora.mapper.InstituicaoMapper realMapper = org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.InstituicaoMapper.class);
        instituicaoService = new InstituicaoService(instituicaoRepository, realMapper, concursoRepository);
    }

    @Test
    void testFindById() {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Test");

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(instituicao));

        InstituicaoDto result = instituicaoService.findById(1L);
        assertNotNull(result);
        assertEquals("Test", result.getNome());
    }

    @Test
    void testSave() {
        InstituicaoDto dto = new InstituicaoDto();
        dto.setNome("Test");

        when(instituicaoRepository.save(any(Instituicao.class))).thenAnswer(i -> {
            Instituicao inst = i.getArgument(0);
            inst.setId(1L);
            return inst;
        });

        InstituicaoDto result = instituicaoService.save(dto);
        assertEquals(1L, result.getId());
        assertEquals("Test", result.getNome());
    }

    @Test
    void testSave_Conflict_DuplicateName_CaseInsensitive() {
        InstituicaoDto dto = new InstituicaoDto();
        dto.setNome("banco central");
        dto.setArea("Financeira");

        Instituicao existing = new Instituicao();
        existing.setId(1L);
        existing.setNome("Banco Central");

        when(instituicaoRepository.findByNomeIgnoreCase("banco central")).thenReturn(Optional.of(existing));

        assertThrows(com.studora.exception.ConflictException.class, () -> {
            instituicaoService.save(dto);
        });
    }
}
