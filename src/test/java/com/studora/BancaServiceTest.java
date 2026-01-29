package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.BancaDto;
import com.studora.entity.Banca;
import com.studora.repository.BancaRepository;
import com.studora.service.BancaService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BancaServiceTest {

    @Mock
    private BancaRepository bancaRepository;

    @InjectMocks
    private BancaService bancaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById() {
        Banca banca = new Banca();
        banca.setId(1L);
        banca.setNome("Test");

        when(bancaRepository.findById(1L)).thenReturn(Optional.of(banca));

        BancaDto result = bancaService.findById(1L);
        assertNotNull(result);
        assertEquals("Test", result.getNome());
    }

    @Test
    void testSave() {
        BancaDto dto = new BancaDto();
        dto.setNome("Test");

        Banca entity = new Banca();
        entity.setId(1L);
        entity.setNome("Test");

        when(bancaRepository.save(any(Banca.class))).thenReturn(entity);

        BancaDto result = bancaService.save(dto);
        assertEquals(1L, result.getId());
    }
}
