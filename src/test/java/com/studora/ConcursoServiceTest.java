package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import com.studora.dto.request.ProvaUpdateRequest;
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
import com.studora.repository.SubtemaRepository;
import com.studora.service.ConcursoService;
import com.studora.service.ProvaService;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class ConcursoServiceTest {

    @Mock private ConcursoRepository concursoRepository;
    @Mock private InstituicaoRepository instituicaoRepository;
    @Mock private BancaRepository bancaRepository;
    @Mock private CargoRepository cargoRepository;
    @Mock private ConcursoCargoRepository concursoCargoRepository;
    @Mock private SubtemaRepository subtemaRepository;
    @Mock private ProvaRepository provaRepository;
    @Mock private ConcursoMapper concursoMapper;
    @Mock private ProvaMapper provaMapper;
    @Mock private EntityManager entityManager;
    @Mock private ProvaService provaService;

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

        assertThrows(ValidationException.class, () -> concursoService.update(concursoId, req));
    }
}
