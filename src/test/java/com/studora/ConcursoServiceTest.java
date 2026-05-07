package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.concurso.ConcursoCargoSummaryDto;
import com.studora.dto.concurso.ConcursoSecaoDto;
import com.studora.dto.concurso.ConcursoCargoSubtemaDto;
import com.studora.dto.prova.ProvaDetailDto;
import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.dto.request.ProvaCreateRequest;
import com.studora.dto.request.ProvaUpdateRequest;
import com.studora.dto.request.ProvaSecaoCreateRequest;
import com.studora.dto.request.ProvaSecaoUpdateRequest;
import com.studora.entity.Banca;
import com.studora.entity.Cargo;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Instituicao;
import com.studora.entity.Prova;
import com.studora.entity.ProvaSecao;
import com.studora.entity.SecaoCargo;
import com.studora.entity.Subtema;
import com.studora.exception.ResourceNotFoundException;
import com.studora.exception.ValidationException;
import com.studora.mapper.ConcursoMapper;
import com.studora.mapper.ProvaMapper;
import com.studora.repository.BancaRepository;
import com.studora.repository.CargoRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.InstituicaoRepository;
import com.studora.repository.ProvaRepository;
import com.studora.repository.SecaoCargoRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.EstudoSubtemaRepository;
import com.studora.service.ConcursoService;
import com.studora.service.ProvaService;
import com.studora.service.StatsAssembler;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class ConcursoServiceTest {

    @Mock private ConcursoRepository concursoRepository;
    @Mock private InstituicaoRepository instituicaoRepository;
    @Mock private BancaRepository bancaRepository;
    @Mock private CargoRepository cargoRepository;
    @Mock private ConcursoCargoRepository concursoCargoRepository;
    @Mock private SecaoCargoRepository secaoCargoRepository;
    @Mock private SubtemaRepository subtemaRepository;
    @Mock private EstudoSubtemaRepository estudoSubtemaRepository;
    @Mock private ProvaRepository provaRepository;
    @Mock private ConcursoMapper concursoMapper;
    @Mock private ProvaMapper provaMapper;
    @Mock private EntityManager entityManager;
    @Mock private ProvaService provaService;
    @Mock private StatsAssembler statsAssembler;

    @InjectMocks
    private ConcursoService concursoService;

    @Test
    void testCreate_Success() {
        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(1L);
        request.setBancaId(1L);
        request.setAno(2023);
        request.setMes(1);
        request.setCargos(List.of(10L));

        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Cargo cargo = new Cargo();
        cargo.setId(10L);

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));
        when(bancaRepository.findById(1L)).thenReturn(Optional.of(banca));
        when(cargoRepository.findById(10L)).thenReturn(Optional.of(cargo));
        when(concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(1L, 1L, 2023, 1)).thenReturn(false);

        Concurso savedConcurso = new Concurso(inst, banca, 2023, 1);
        savedConcurso.setId(100L);
        savedConcurso.setEdital("Edital 01/2023");
        when(concursoMapper.toEntity(any(ConcursoCreateRequest.class))).thenReturn(savedConcurso);
        when(concursoRepository.save(any(Concurso.class))).thenReturn(savedConcurso);

        Long id = concursoService.create(request);

        assertEquals(100L, id);
        verify(concursoRepository).save(any(Concurso.class));
    }

    @Test
    void testCreate_Duplicate_ThrowsConflict() {
        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(1L);
        request.setBancaId(1L);
        request.setAno(2023);
        request.setMes(1);

        when(concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(1L, 1L, 2023, 1)).thenReturn(true);

        assertThrows(com.studora.exception.ConflictException.class, () -> concursoService.create(request));
    }

    @Test
    void testUpdate_CargoRemovalValidation_FailsIfUsedInProva() {
        Long id = 1L;
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Cargo cargo1 = new Cargo();
        cargo1.setId(10L);
        cargo1.setNome("Cargo 1");

        Concurso existing = new Concurso(inst, banca, 2023, 1);
        existing.setId(id);

        ConcursoCargo cc1 = new ConcursoCargo();
        cc1.setConcurso(existing);
        cc1.setCargo(cargo1);
        cc1.setId(100L);
        existing.addConcursoCargo(cc1);

        Prova prova = new Prova();
        prova.setConcursoCargo(cc1);
        existing.getProvas().add(prova);

        // Update: Replace with empty (removes Cargo 1)
        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setCargos(List.of());

        when(concursoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));

        assertThrows(ValidationException.class, () -> concursoService.update(id, req));
    }

    @Test
    void testDelete_Success() {
        Long id = 1L;
        when(concursoRepository.existsById(id)).thenReturn(true);
        doNothing().when(concursoRepository).deleteById(id);

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

        Concurso savedConcurso = new Concurso(inst, banca, 2024, 6);
        savedConcurso.setId(1L);
        savedConcurso.setEdital("Edital 01/2024");
        when(concursoMapper.toEntity(any(ConcursoCreateRequest.class))).thenReturn(savedConcurso);

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
    void testGetConcursoDetailById_PopulatesTopicosWithAssuntos() {
        Instituicao inst = new Instituicao();
        inst.setId(1L);
        Banca banca = new Banca();
        banca.setId(1L);
        Cargo cargo = new Cargo();
        cargo.setId(10L);
        
        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);
        
        Concurso concurso = new Concurso(inst, banca, 2023, 1);
        concurso.setId(1L);
        cc.setConcurso(concurso);
        concurso.getConcursoCargos().add(cc);

        when(concursoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(concurso));
        
        // Mocking the structure we expect back from the service/mapper
        ConcursoDetailDto detailDto = new ConcursoDetailDto();
        ConcursoCargoSummaryDto cargoDto = new ConcursoCargoSummaryDto();
        
        ConcursoSecaoDto secaoDto = new ConcursoSecaoDto();
        secaoDto.setNome("Seção 1");
        
        ConcursoCargoSubtemaDto subDto = new ConcursoCargoSubtemaDto();
        subDto.setNome("Subtema 1");
        secaoDto.setAssuntos(java.util.List.of(subDto));
        
        cargoDto.setTopicos(java.util.List.of(secaoDto));
        detailDto.setCargos(java.util.List.of(cargoDto));
        
        when(concursoMapper.toDetailDto(any())).thenReturn(detailDto);

        ConcursoDetailDto result = concursoService.getConcursoDetailById(1L, null);
        
        assertNotNull(result);
        assertNotNull(result.getCargos());
        assertEquals(1, result.getCargos().size());
        assertNotNull(result.getCargos().get(0).getTopicos());
        assertEquals(1, result.getCargos().get(0).getTopicos().size());
        assertEquals("Seção 1", result.getCargos().get(0).getTopicos().get(0).getNome());
        assertEquals(1, result.getCargos().get(0).getTopicos().get(0).getAssuntos().size());
        assertEquals("Subtema 1", result.getCargos().get(0).getTopicos().get(0).getAssuntos().get(0).getNome());
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

        // Cargo
        Cargo cargo = new Cargo();
        cargo.setId(10L);
        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);
        cc.setConcurso(existing);
        existing.addConcursoCargo(cc);

        // Existing Prova
        Prova existingProva = new Prova();
        existingProva.setId(10L);
        existingProva.setNome("Original Prova");
        existingProva.setConcurso(existing);
        existingProva.setConcursoCargo(cc);
        existing.getProvas().add(existingProva);

        // Existing Secao
        SecaoCargo scDef = new SecaoCargo();
        scDef.setId(500L);
        scDef.setNome("Original Secao");
        scDef.setConcursoCargo(cc);
        cc.getSecaoCargos().add(scDef);

        ProvaSecao existingSecao = new ProvaSecao();
        existingSecao.setId(100L);
        existingSecao.setNome("Original Secao");
        existingSecao.setOrdem(1);
        existingSecao.setProva(existingProva);
        existingSecao.setSecaoCargo(scDef);
        existingProva.getSecoes().add(existingSecao);

        // Request
        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setCargos(List.of(10L));

        // 1. Update existing prova, add new secao, remove existing secao
        ProvaUpdateRequest pReq1 = new ProvaUpdateRequest();
        pReq1.setId(10L);
        pReq1.setNome("Updated Prova");
        pReq1.setCargoId(10L);

        ProvaSecaoUpdateRequest sReqNew = new ProvaSecaoUpdateRequest();
        sReqNew.setNome("New Secao");
        sReqNew.setOrdem(1);
        pReq1.setSecoes(List.of(sReqNew)); // This effectively removes existingSecao (ID 100)

        // 2. Add entirely new prova
        ProvaUpdateRequest pReqNew = new ProvaUpdateRequest();
        pReqNew.setNome("New Prova");
        pReqNew.setCargoId(10L);

        req.setProvas(List.of(pReq1, pReqNew));

        when(concursoRepository.findByIdWithDetails(concursoId)).thenReturn(Optional.of(existing));
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));
        when(secaoCargoRepository.findByConcursoCargoIdAndNomeIgnoreCase(anyLong(), anyString())).thenReturn(Optional.empty());
        when(secaoCargoRepository.save(any()))
            .thenAnswer(invocation -> {
                cc.getSecaoCargos().add((com.studora.entity.SecaoCargo) invocation.getArgument(0));
                return invocation.getArgument(0);
            });
        when(secaoCargoRepository.findAllByConcursoCargoId(anyLong())).thenAnswer(i -> cc.getSecaoCargos().stream().toList());

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
        
        // Definition for "New Secao" should have been created
        assertTrue(cc.getSecaoCargos().stream().anyMatch(sc -> sc.getNome().equals("New Secao")));
    }

    @Test
    void testUpdate_Validation_SubtemaOneSecaoDefinitionPerCargo() {
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

        Cargo cargo = new Cargo();
        cargo.setId(10L);
        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);
        cc.setConcurso(existing);
        existing.addConcursoCargo(cc);

        // Existing Prova
        Prova existingProva = new Prova();
        existingProva.setId(10L);
        existingProva.setNome("Prova");
        existingProva.setConcurso(existing);
        existingProva.setConcursoCargo(cc);
        existing.getProvas().add(existingProva);

        // Subtema
        Subtema subtema = new Subtema();
        subtema.setId(100L);
        subtema.setNome("Subtema 1");

        // Request - try to update with two sections sharing the same subtema (definition level)
        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setCargos(List.of(10L));

        ProvaUpdateRequest pReq = new ProvaUpdateRequest();
        pReq.setId(10L);
        pReq.setNome("Prova");
        pReq.setCargoId(10L);

        ProvaSecaoUpdateRequest sReq1 = new ProvaSecaoUpdateRequest();
        sReq1.setNome("Secao 1");
        sReq1.setOrdem(1);
        sReq1.setSubtemaIds(List.of(100L));

        ProvaSecaoUpdateRequest sReq2 = new ProvaSecaoUpdateRequest();
        sReq2.setNome("Secao 2");
        sReq2.setOrdem(2);
        sReq2.setSubtemaIds(List.of(100L)); // Same subtema as secao1

        pReq.setSecoes(List.of(sReq1, sReq2));
        req.setProvas(List.of(pReq));

        when(concursoRepository.findByIdWithDetails(concursoId)).thenReturn(Optional.of(existing));
        when(subtemaRepository.findAllById(any())).thenReturn(List.of(subtema));
        when(secaoCargoRepository.findByConcursoCargoIdAndNomeIgnoreCase(anyLong(), anyString())).thenReturn(Optional.empty());
        when(secaoCargoRepository.save(any()))
            .thenAnswer(invocation -> {
                Object sc = invocation.getArgument(0);
                cc.getSecaoCargos().add((com.studora.entity.SecaoCargo)sc);
                return sc;
            });
        when(secaoCargoRepository.findAllByConcursoCargoId(anyLong())).thenAnswer(i -> cc.getSecaoCargos().stream().toList());

        assertThrows(ValidationException.class, () -> concursoService.update(concursoId, req));
    }

    @Test
    void testUpdate_SyncsOrdemAndNumQuestoesToSecaoCargo() {
        // Setup
        Long concursoId = 1L;
        Concurso existing = new Concurso();
        existing.setId(concursoId);
        existing.setAno(2023);
        existing.setMes(6);
        Instituicao inst = new Instituicao(); inst.setId(1L);
        Banca banca = new Banca(); banca.setId(1L);
        existing.setInstituicao(inst);
        existing.setBanca(banca);

        Cargo cargo = new Cargo(); cargo.setId(10L);
        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);
        cc.setConcurso(existing);
        existing.addConcursoCargo(cc);

        Prova existingProva = new Prova();
        existingProva.setId(10L);
        existingProva.setNome("Prova 1");
        existingProva.setConcurso(existing);
        existingProva.setConcursoCargo(cc);
        existing.getProvas().add(existingProva);

        SecaoCargo scDef = new SecaoCargo();
        scDef.setId(500L);
        scDef.setNome("Conhecimentos Gerais");
        scDef.setConcursoCargo(cc);
        scDef.setOrdem(10); // Old value
        scDef.setNumQuestoes(10); // Old value
        cc.getSecaoCargos().add(scDef);

        ProvaSecao ps = new ProvaSecao();
        ps.setId(1000L);
        ps.setNome("Conhecimentos Gerais");
        ps.setOrdem(10);
        ps.setNumQuestoes(10);
        ps.setProva(existingProva);
        ps.setSecaoCargo(scDef);
        existingProva.getSecoes().add(ps);

        // Request to update
        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setCargos(List.of(10L));

        ProvaUpdateRequest pReq = new ProvaUpdateRequest();
        pReq.setId(10L);
        pReq.setNome("Prova 1");
        pReq.setCargoId(10L);

        ProvaSecaoUpdateRequest sReq = new ProvaSecaoUpdateRequest();
        sReq.setId(1000L);
        sReq.setNome("Conhecimentos Gerais");
        sReq.setOrdem(0); // New value
        sReq.setNumQuestoes(20); // New value
        pReq.setSecoes(List.of(sReq));

        req.setProvas(List.of(pReq));

        when(concursoRepository.findByIdWithDetails(concursoId)).thenReturn(Optional.of(existing));
        when(secaoCargoRepository.findByConcursoCargoIdAndNomeIgnoreCase(100L, "Conhecimentos Gerais"))
                .thenReturn(Optional.of(scDef));
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));

        // Execute
        concursoService.update(concursoId, req);

        // Verify ProvaSecao
        assertEquals(0, ps.getOrdem());
        assertEquals(20, ps.getNumQuestoes());

        // Verify SecaoCargo (This was failing before the fix)
        assertEquals(0, scDef.getOrdem());
        assertEquals(20, scDef.getNumQuestoes());
    }

    @Test
    void testCreate_SetsDefaultNumQuestoesTo1_WhenNullOrZero() {
        // Setup
        Long instId = 1L;
        Long bancaId = 1L;
        Integer ano = 2024;

        Instituicao inst = new Instituicao();
        inst.setId(instId);
        Banca banca = new Banca();
        banca.setId(bancaId);

        Cargo cargo = new Cargo();
        cargo.setId(1L);

        // Request with secao without numQuestoes
        ConcursoCreateRequest req = new ConcursoCreateRequest();
        req.setInstituicaoId(instId);
        req.setBancaId(bancaId);
        req.setAno(ano);
        req.setMes(6);
        req.setCargos(List.of(1L));

        ProvaCreateRequest pReq = new ProvaCreateRequest();
        pReq.setNome("Prova Teste");
        pReq.setCargoId(1L);

        ProvaSecaoCreateRequest sReq = new ProvaSecaoCreateRequest();
        sReq.setNome("Seção 1");
        sReq.setOrdem(1);
        // numQuestoes not set - should default to 1
        pReq.setSecoes(List.of(sReq));

        ProvaSecaoCreateRequest sReq2 = new ProvaSecaoCreateRequest();
        sReq2.setNome("Seção 2");
        sReq2.setOrdem(2);
        sReq2.setNumQuestoes(0); // explicitly set to 0 - should default to 1
        pReq.setSecoes(List.of(sReq, sReq2));

        req.setProvas(List.of(pReq));

        // Execute
        when(instituicaoRepository.findById(instId)).thenReturn(Optional.of(inst));
        when(bancaRepository.findById(bancaId)).thenReturn(Optional.of(banca));
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));

// Mock the mapper to return a valid empty concurso
        Concurso savedConcurso = new Concurso();
        savedConcurso.setInstituicao(inst);
        savedConcurso.setBanca(banca);
        savedConcurso.setAno(ano);
        savedConcurso.setMes(6);
        // Initialize empty collections
        savedConcurso.setProvas(new java.util.LinkedHashSet<>());
savedConcurso.setConcursoCargos(new java.util.LinkedHashSet<>());
        when(concursoMapper.toEntity(any(ConcursoCreateRequest.class))).thenReturn(savedConcurso);
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(invocation -> {
            Concurso saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Long id = concursoService.create(req);

        // Verify
        assertNotNull(id);
        // Get the saved concurso from the mock
        ArgumentCaptor<Concurso> captor = ArgumentCaptor.forClass(Concurso.class);
        verify(concursoRepository).save(captor.capture());
        Concurso saved = captor.getValue();

        // Find the prova
        Prova prova = saved.getProvas().stream().findFirst().orElseThrow();

        // Find secao 1 (null value should default to 1)
        ProvaSecao secao1 = prova.getSecoes().stream()
                .filter(s -> s.getNome().equals("Seção 1"))
                .findFirst().orElseThrow();
        assertEquals(1, secao1.getNumQuestoes(), "Seção 1 with null numQuestoes should default to 1");

        // Find secao 2 (0 value should default to 1)
        ProvaSecao secao2 = prova.getSecoes().stream()
                .filter(s -> s.getNome().equals("Seção 2"))
                .findFirst().orElseThrow();
        assertEquals(1, secao2.getNumQuestoes(), "Seção 2 with 0 numQuestoes should default to 1");

        // Verify SecaoCargo also has default
        assertNotNull(secao1.getSecaoCargo());
        assertEquals(1, secao1.getSecaoCargo().getNumQuestoes());
    }
}
