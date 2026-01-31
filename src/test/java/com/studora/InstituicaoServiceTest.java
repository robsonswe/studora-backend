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

    @InjectMocks
    private InstituicaoService instituicaoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() {
        Instituicao inst = new Instituicao();
        inst.setId(10L);
        inst.setNome("Test Inst");
        inst.setArea("Test Area");

        when(instituicaoRepository.findById(10L)).thenReturn(Optional.of(inst));

        InstituicaoDto result = instituicaoService.findById(10L);
        assertNotNull(result);
        assertEquals("Test Inst", result.getNome());
        assertEquals("Test Area", result.getArea());
    }

    @Test
    void testSave() {
        InstituicaoDto dto = new InstituicaoDto();
        dto.setNome("New Inst");
        dto.setArea("New Area");

        Instituicao entity = new Instituicao();
        entity.setId(1L);
        entity.setNome("New Inst");
        entity.setArea("New Area");

        when(instituicaoRepository.save(any(Instituicao.class))).thenReturn(
            entity
        );

        InstituicaoDto result = instituicaoService.save(dto);
        assertEquals("New Inst", result.getNome());
        assertEquals("New Area", result.getArea());
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
