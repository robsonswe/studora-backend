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

        when(instituicaoRepository.findById(10L)).thenReturn(Optional.of(inst));

        InstituicaoDto result = instituicaoService.findById(10L);
        assertNotNull(result);
        assertEquals("Test Inst", result.getNome());
    }

    @Test
    void testSave() {
        InstituicaoDto dto = new InstituicaoDto();
        dto.setNome("New Inst");

        Instituicao entity = new Instituicao();
        entity.setId(1L);
        entity.setNome("New Inst");

        when(instituicaoRepository.save(any(Instituicao.class))).thenReturn(
            entity
        );

        InstituicaoDto result = instituicaoService.save(dto);
        assertEquals("New Inst", result.getNome());
    }
}
