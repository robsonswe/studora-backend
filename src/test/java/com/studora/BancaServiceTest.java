package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.banca.BancaDetailDto;
import com.studora.dto.request.BancaCreateRequest;
import com.studora.dto.request.BancaUpdateRequest;
import com.studora.entity.Banca;
import com.studora.exception.ConflictException;
import com.studora.repository.BancaRepository;
import com.studora.service.BancaService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BancaServiceTest {

    @Mock
    private BancaRepository bancaRepository;

    @Mock
    private com.studora.repository.ConcursoRepository concursoRepository;

    private BancaService bancaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        com.studora.mapper.BancaMapper realMapper = org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.BancaMapper.class);
        bancaService = new BancaService(bancaRepository, realMapper, concursoRepository);
    }

    @Test
    void testFindById() {
        Banca banca = new Banca();
        banca.setId(1L);
        banca.setNome("Test");

        when(bancaRepository.findById(1L)).thenReturn(Optional.of(banca));

        BancaDetailDto result = bancaService.getBancaDetailById(1L);
        assertNotNull(result);
        assertEquals("Test", result.getNome());
    }

    @Test
    void testCreate_Success() {
        BancaCreateRequest request = new BancaCreateRequest();
        request.setNome("Test");

        when(bancaRepository.findByNomeIgnoreCase("Test")).thenReturn(Optional.empty());
        when(bancaRepository.save(any(Banca.class))).thenAnswer(i -> {
            Banca b = i.getArgument(0);
            b.setId(1L);
            return b;
        });

        BancaDetailDto result = bancaService.create(request);
        assertEquals(1L, result.getId());
        assertEquals("Test", result.getNome());
    }

    @Test
    void testCreate_CaseInsensitiveDuplicate() {
        BancaCreateRequest req = new BancaCreateRequest();
        req.setNome("cespe");

        Banca existingBanca = new Banca();
        existingBanca.setId(2L);
        existingBanca.setNome("CESPE");

        when(bancaRepository.findByNomeIgnoreCase("cespe")).thenReturn(Optional.of(existingBanca));

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            bancaService.create(req);
        });

        assertTrue(exception.getMessage().contains("Já existe uma banca com o nome 'cespe'"));
    }

    @Test
    void testUpdate_Success() {
        Long id = 1L;
        Banca banca = new Banca();
        banca.setId(id);
        banca.setNome("Old Name");

        BancaUpdateRequest req = new BancaUpdateRequest();
        req.setNome("New Name");

        when(bancaRepository.findById(id)).thenReturn(Optional.of(banca));
        when(bancaRepository.findByNomeIgnoreCase("New Name")).thenReturn(Optional.empty());
        when(bancaRepository.save(any(Banca.class))).thenAnswer(i -> i.getArgument(0));

        BancaDetailDto result = bancaService.update(id, req);
        assertEquals("New Name", result.getNome());
    }

    @Test
    void testUpdate_CaseInsensitiveDuplicate() {
        // Test that case-insensitive duplicates are caught
        BancaUpdateRequest request = new BancaUpdateRequest();
        request.setNome("cespe"); // lowercase

        Banca targetBanca = new Banca();
        targetBanca.setId(1L);
        targetBanca.setNome("Original");

        Banca existingBanca = new Banca();
        existingBanca.setId(2L);
        existingBanca.setNome("CESPE"); // uppercase

        when(bancaRepository.findById(1L)).thenReturn(Optional.of(targetBanca));
        when(bancaRepository.findByNomeIgnoreCase("cespe")).thenReturn(Optional.of(existingBanca));

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            bancaService.update(1L, request);
        });

        assertTrue(exception.getMessage().contains("Já existe uma banca com o nome 'cespe'"));
    }

    @Test
    void testDelete_FailsWithAssociatedExams() {
        when(bancaRepository.existsById(1L)).thenReturn(true);
        when(concursoRepository.existsByBancaId(1L)).thenReturn(true);

        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class, 
            () -> bancaService.delete(1L)
        );

        assertTrue(exception.getMessage().contains("possui concursos associados"));
    }
}
