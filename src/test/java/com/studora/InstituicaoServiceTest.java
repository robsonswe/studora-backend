package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.instituicao.InstituicaoDetailDto;
import com.studora.dto.request.InstituicaoCreateRequest;
import com.studora.entity.Instituicao;
import com.studora.repository.InstituicaoRepository;
import com.studora.service.InstituicaoService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        inst.setNome("USP");

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));

        InstituicaoDetailDto result = instituicaoService.getInstituicaoDetailById(1L);
        assertNotNull(result);
        assertEquals("USP", result.getNome());
    }

    @Test
    void testCreate_Success() {
        InstituicaoCreateRequest request = new InstituicaoCreateRequest();
        request.setNome("UNICAMP");

        when(instituicaoRepository.findByNomeIgnoreCase("UNICAMP")).thenReturn(Optional.empty());
        when(instituicaoRepository.save(any(Instituicao.class))).thenAnswer(i -> {
            Instituicao inst = i.getArgument(0);
            inst.setId(1L);
            return inst;
        });

        InstituicaoDetailDto result = instituicaoService.create(request);
        assertEquals(1L, result.getId());
        assertEquals("UNICAMP", result.getNome());
    }

    @Test
    void testCreate_Conflict_DuplicateName_CaseInsensitive() {
        InstituicaoCreateRequest req = new InstituicaoCreateRequest();
        req.setNome("banco central");

        Instituicao existing = new Instituicao();
        existing.setId(1L);
        existing.setNome("Banco Central");

        when(instituicaoRepository.findByNomeIgnoreCase("banco central")).thenReturn(Optional.of(existing));

        assertThrows(com.studora.exception.ConflictException.class, () -> {
            instituicaoService.create(req);
        });
    }

    @Test
    void testUpdate_Success() {
        Long id = 1L;
        Instituicao inst = new Instituicao();
        inst.setId(id);
        inst.setNome("Old Name");

        com.studora.dto.request.InstituicaoUpdateRequest req = new com.studora.dto.request.InstituicaoUpdateRequest();
        req.setNome("New Name");

        when(instituicaoRepository.findById(id)).thenReturn(Optional.of(inst));
        when(instituicaoRepository.findByNomeIgnoreCase("New Name")).thenReturn(Optional.empty());
        when(instituicaoRepository.save(any(Instituicao.class))).thenAnswer(i -> i.getArgument(0));

        InstituicaoDetailDto result = instituicaoService.update(id, req);
        assertEquals("New Name", result.getNome());
    }

    @Test
    void testDelete_FailsWithAssociatedExams() {
        when(instituicaoRepository.existsById(1L)).thenReturn(true);
        when(concursoRepository.existsByInstituicaoId(1L)).thenReturn(true);

        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class, 
            () -> instituicaoService.delete(1L)
        );

        assertTrue(exception.getMessage().contains("possui concursos associados"));
    }
}