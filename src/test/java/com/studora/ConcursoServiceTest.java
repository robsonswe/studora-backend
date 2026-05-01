package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.MetricsLevel;
import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.request.*;
import com.studora.entity.*;
import com.studora.exception.ValidationException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.BancaRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.InstituicaoRepository;
import com.studora.repository.CargoRepository;
import com.studora.service.ConcursoService;
import com.studora.mapper.ConcursoMapper;
import com.studora.mapper.InstituicaoMapper;
import com.studora.mapper.BancaMapper;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

class ConcursoServiceTest {

    @Mock
    private ConcursoRepository concursoRepository;
    @Mock
    private InstituicaoRepository instituicaoRepository;
    @Mock
    private BancaRepository bancaRepository;
    @Mock
    private CargoRepository cargoRepository;
    @Mock
    private ConcursoCargoRepository concursoCargoRepository;
    @Mock
    private com.studora.repository.SubtemaRepository subtemaRepository;
    @Mock
    private jakarta.persistence.EntityManager entityManager;

    @Mock
    private com.studora.service.StatsAssembler statsAssembler;

    private ConcursoService concursoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ConcursoMapper realMapper = org.mapstruct.factory.Mappers.getMapper(ConcursoMapper.class);
        InstituicaoMapper instMapper = org.mapstruct.factory.Mappers.getMapper(InstituicaoMapper.class);
        BancaMapper bancaMapper = org.mapstruct.factory.Mappers.getMapper(BancaMapper.class);
        com.studora.mapper.CargoMapper cargoMapper = org.mapstruct.factory.Mappers
                .getMapper(com.studora.mapper.CargoMapper.class);
        com.studora.mapper.ProvaMapper provaMapper = org.mapstruct.factory.Mappers
                .getMapper(com.studora.mapper.ProvaMapper.class);

        ReflectionTestUtils.setField(realMapper, "instituicaoMapper", instMapper);
        ReflectionTestUtils.setField(realMapper, "bancaMapper", bancaMapper);
        ReflectionTestUtils.setField(realMapper, "provaMapper", provaMapper);

        concursoService = new ConcursoService(
                concursoRepository,
                instituicaoRepository,
                bancaRepository,
                cargoRepository,
                concursoCargoRepository,
                subtemaRepository,
                null, // EstudoSubtemaRepository mocked/null
                realMapper,
                statsAssembler,
                entityManager);
    }

    @Test
    void testFindById() {
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Concurso concurso = new Concurso(inst, banca, 2023, 1);
        concurso.setId(1L);

        when(concursoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(concurso));

        ConcursoDetailDto result = concursoService.getConcursoDetailById(1L, null);
        assertNotNull(result);
        assertEquals(2023, result.getAno());
    }

    @Test
    void testFindById_NotFound() {
        when(concursoRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> concursoService.getConcursoDetailById(1L, null));
    }

    @Test
    void testFindAll() {
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Concurso c1 = new Concurso(inst, banca, 2023, 1);
        Concurso c2 = new Concurso(inst, banca, 2024, 2);

        Page<Concurso> page = new PageImpl<>(Arrays.asList(c1, c2));
        when(concursoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<ConcursoSummaryDto> result = concursoService.findAll(new com.studora.dto.concurso.ConcursoFilter(),
                Pageable.unpaged());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testCreate_Success() {
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Cargo cargo = new Cargo();
        cargo.setId(10L);

        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(1L);
        request.setBancaId(1L);
        request.setAno(2023);
        request.setMes(1);
        request.setCargos(List.of(10L));

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));
        when(bancaRepository.findById(1L)).thenReturn(Optional.of(banca));
        when(cargoRepository.findById(10L)).thenReturn(Optional.of(cargo));
        when(concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(1L, 1L, 2023, 1)).thenReturn(false);

        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> {
            Concurso c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        concursoService.create(request);
        verify(concursoRepository).save(any(Concurso.class));
    }

    @Test
    void testCreate_DuplicateConflict() {
        ConcursoCreateRequest req = new ConcursoCreateRequest();
        req.setInstituicaoId(1L);
        req.setBancaId(1L);
        req.setAno(2023);
        req.setMes(1);
        req.setCargos(List.of(1L));

        when(concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(1L, 1L, 2023, 1)).thenReturn(true);

        assertThrows(com.studora.exception.ConflictException.class, () -> {
            concursoService.create(req);
        });
    }

    @Test
    void testUpdate_Success() {
        Long id = 1L;
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Cargo cargo = new Cargo();
        cargo.setId(10L);

        Concurso existing = new Concurso(inst, banca, 2023, 1);
        existing.setId(id);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(existing);
        cc.setCargo(cargo);
        cc.setId(100L);
        existing.addConcursoCargo(cc);

        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setAno(2024);
        req.setCargos(List.of(10L)); // Same cargo

        when(concursoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));

        concursoService.update(id, req);
        verify(concursoRepository).save(any(Concurso.class));
    }

    @Test
    void testUpdate_AddAndRemoveCargos() {
        Long id = 1L;
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);

        Cargo cargo1 = new Cargo();
        cargo1.setId(10L);
        Cargo cargo2 = new Cargo();
        cargo2.setId(20L);

        Concurso existing = new Concurso(inst, banca, 2023, 1);
        existing.setId(id);

        // Initially has Cargo 1
        ConcursoCargo cc1 = new ConcursoCargo();
        cc1.setConcurso(existing);
        cc1.setCargo(cargo1);
        cc1.setId(100L);
        existing.addConcursoCargo(cc1);

        // Update: Remove Cargo 1, Add Cargo 2
        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setCargos(List.of(20L));

        when(concursoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(cargoRepository.findById(20L)).thenReturn(Optional.of(cargo2));

        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));

        concursoService.update(id, req);

        assertEquals(1, existing.getConcursoCargos().size());
        assertEquals(20L, existing.getConcursoCargos().iterator().next().getCargo().getId());
    }

    @Test
    void testUpdate_RemoveCargo_FailIfUsed() {
        Long id = 1L;
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Cargo cargo1 = new Cargo();
        cargo1.setId(10L);

        Concurso existing = new Concurso(inst, banca, 2023, 1);
        existing.setId(id);

        ConcursoCargo cc1 = new ConcursoCargo();
        cc1.setConcurso(existing);
        cc1.setCargo(cargo1);
        cc1.setId(100L);
        existing.addConcursoCargo(cc1);

        Prova prova = new Prova();
        prova.addCargo(cc1);
        existing.getProvas().add(prova);

        // Update: Remove Cargo 1
        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setCargos(List.of(20L)); // Try to replace with 20L

        when(concursoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(cargoRepository.findById(20L)).thenReturn(Optional.of(new Cargo()));

        assertThrows(ValidationException.class, () -> concursoService.update(id, req));
    }

    @Test
    void testDelete_Success() {
        Long id = 1L;
        when(concursoRepository.existsById(id)).thenReturn(true);
        when(concursoRepository.findById(id)).thenReturn(Optional.of(new Concurso()));

        concursoService.delete(id);
        verify(concursoRepository).deleteById(id);
    }

    @Test
    void testDelete_NotFound() {
        when(concursoRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> concursoService.delete(1L));
    }

    @Test
    void testCreate_WithDataProva() {
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Cargo cargo = new Cargo();
        cargo.setId(10L);

        java.time.LocalDateTime dataProva = java.time.LocalDateTime.of(2024, 6, 15, 8, 0);

        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(1L);
        request.setBancaId(1L);
        request.setAno(2024);
        request.setMes(6);
        request.setDataProva(dataProva);
        request.setCargos(List.of(10L));

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));
        when(bancaRepository.findById(1L)).thenReturn(Optional.of(banca));
        when(cargoRepository.findById(10L)).thenReturn(Optional.of(cargo));
        when(concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(1L, 1L, 2024, 6)).thenReturn(false);

        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> {
            Concurso c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        concursoService.create(request);
        verify(concursoRepository).save(argThat(c -> c.getDataProva().equals(dataProva)));
    }

    @Test
    void testUpdate_WithDataProva() {
        Long id = 1L;
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Cargo cargo = new Cargo();
        cargo.setId(10L);

        Concurso existing = new Concurso(inst, banca, 2023, 1);
        existing.setId(id);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(existing);
        cc.setCargo(cargo);
        cc.setId(100L);
        existing.addConcursoCargo(cc);

        java.time.LocalDateTime newDataProva = java.time.LocalDateTime.of(2024, 9, 10, 14, 0);

        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setAno(2023);
        req.setMes(1);
        req.setDataProva(newDataProva);
        req.setCargos(List.of(10L));

        when(concursoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));

        concursoService.update(id, req);
        verify(concursoRepository).save(argThat(c -> c.getDataProva().equals(newDataProva)));
    }

    @Test
    void testGetConcursoDetailById_PopulatesProvaCargoIds() {
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Concurso concurso = new Concurso(inst, banca, 2023, 1);
        concurso.setId(1L);

        Cargo cargo = new Cargo();
        cargo.setId(10L);
        
        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);
        cc.setConcurso(concurso);
        concurso.getConcursoCargos().add(cc);

        Prova prova = new Prova();
        prova.setId(50L);
        prova.setNome("Prova Objetiva");
        prova.setConcurso(concurso);
        prova.getCargos().add(cc);
        concurso.getProvas().add(prova);

        when(concursoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(concurso));

        ConcursoDetailDto result = concursoService.getConcursoDetailById(1L, null);
        
        assertNotNull(result);
        assertNotNull(result.getProvas());
        assertEquals(1, result.getProvas().size());
        
        com.studora.dto.prova.ProvaDetailDto provaDto = result.getProvas().get(0);
        assertEquals(50L, provaDto.getId());
        assertNotNull(provaDto.getCargoIds());
        assertTrue(provaDto.getCargoIds().contains(10L), "ProvaDetailDto should contain the ID of the linked Cargo");
    }

    @Test
    void testToggleFinalizado_Success() {
        Long id = 1L;
        Concurso concurso = new Concurso();
        concurso.setId(id);
        concurso.setFinalizado(false);

        when(concursoRepository.findById(id)).thenReturn(Optional.of(concurso));
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));

        concursoService.toggleFinalizado(id);
        assertTrue(concurso.isFinalizado());
        verify(concursoRepository).save(concurso);

        concursoService.toggleFinalizado(id);
        assertFalse(concurso.isFinalizado());
    }

    @Test
    void testUpdate_DeepSyncProvasAndSecoes() {
        // Setup
        Long concursoId = 1L;
        Concurso existing = new Concurso();
        existing.setId(concursoId);
        existing.setAno(2023);
        existing.setMes(6);
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        existing.setInstituicao(inst);
        existing.setBanca(banca);

        // Existing Prova
        Prova existingProva = new Prova();
        existingProva.setId(10L);
        existingProva.setNome("Original Prova");
        existingProva.setConcurso(existing);
        existing.getProvas().add(existingProva);

        // Existing Secao
        ProvaSecao existingSecao = new ProvaSecao();
        existingSecao.setId(100L);
        existingSecao.setNome("Original Secao");
        existingSecao.setOrdem(1);
        existingSecao.setProva(existingProva);
        existingProva.getSecoes().add(existingSecao);

        // Request
        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setCargos(Collections.emptyList());

        // 1. Update existing prova, add new secao, remove existing secao
        ProvaUpdateRequest pReq1 = new ProvaUpdateRequest();
        pReq1.setId(10L);
        pReq1.setNome("Updated Prova");

        ProvaSecaoUpdateRequest sReqNew = new ProvaSecaoUpdateRequest();
        sReqNew.setNome("New Secao");
        sReqNew.setOrdem(1);
        pReq1.setSecoes(List.of(sReqNew)); // This effectively removes existingSecao (ID 100)

        // 2. Add entirely new prova
        ProvaUpdateRequest pReqNew = new ProvaUpdateRequest();
        pReqNew.setNome("New Prova");

        req.setProvas(List.of(pReq1, pReqNew));

        when(concursoRepository.findByIdWithDetails(concursoId)).thenReturn(Optional.of(existing));
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));

        // Execute
        concursoService.update(concursoId, req);

        // Verify
        assertEquals(2, existing.getProvas().size());

        // Check updated prova
        Prova p1 = existing.getProvas().stream().filter(p -> p.getId() != null && p.getId().equals(10L)).findFirst()
                .orElseThrow();
        assertEquals("Updated Prova", p1.getNome());
        assertEquals(1, p1.getSecoes().size());
        assertEquals("New Secao", p1.getSecoes().iterator().next().getNome());
    }

    @Test
    void testUpdate_Validation_SubtemaOneSecaoPerProva() {
        // Setup
        Long concursoId = 1L;
        Concurso existing = new Concurso();
        existing.setId(concursoId);
        existing.setAno(2023);
        existing.setMes(6);
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        existing.setInstituicao(inst);
        existing.setBanca(banca);

        // Existing Prova
        Prova existingProva = new Prova();
        existingProva.setId(10L);
        existingProva.setNome("Prova");
        existingProva.setConcurso(existing);
        existing.getProvas().add(existingProva);

        // Subtema
        Subtema subtema = new Subtema();
        subtema.setId(100L);
        subtema.setNome("Subtema 1");

        // Secao 1
        ProvaSecao secao1 = new ProvaSecao();
        secao1.setId(200L);
        secao1.setNome("Secao 1");
        secao1.setProva(existingProva);
        secao1.getSubtemas().add(subtema);
        existingProva.getSecoes().add(secao1);

        // Secao 2 - same prova, same subtema (invalid)
        ProvaSecao secao2 = new ProvaSecao();
        secao2.setId(201L);
        secao2.setNome("Secao 2");
        secao2.setProva(existingProva);
        secao2.getSubtemas().add(subtema);
        existingProva.getSecoes().add(secao2);

        // Request - try to update with duplicate subtema
        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setCargos(Collections.emptyList());

        ProvaUpdateRequest pReq = new ProvaUpdateRequest();
        pReq.setId(10L);
        pReq.setNome("Prova");

        ProvaSecaoUpdateRequest sReq1 = new ProvaSecaoUpdateRequest();
        sReq1.setId(200L);
        sReq1.setNome("Secao 1");
        sReq1.setOrdem(1);
        sReq1.setSubtemaIds(List.of(100L));

        ProvaSecaoUpdateRequest sReq2 = new ProvaSecaoUpdateRequest();
        sReq2.setId(201L);
        sReq2.setNome("Secao 2");
        sReq2.setOrdem(2);
        sReq2.setSubtemaIds(List.of(100L)); // Same subtema as secao1

        pReq.setSecoes(List.of(sReq1, sReq2));
        req.setProvas(List.of(pReq));

        when(concursoRepository.findByIdWithDetails(concursoId)).thenReturn(Optional.of(existing));
        when(subtemaRepository.findAllById(anyList())).thenReturn(List.of(subtema));

        // Execute & Verify
assertThrows(ValidationException.class, () -> concursoService.update(concursoId, req));
    }
}
