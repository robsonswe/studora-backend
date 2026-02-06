package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.entity.Banca;
import com.studora.entity.Concurso;
import com.studora.entity.Instituicao;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Cargo;
import com.studora.exception.ValidationException;
import com.studora.exception.ResourceNotFoundException;
import com.studora.repository.BancaRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.InstituicaoRepository;
import com.studora.repository.CargoRepository;
import com.studora.repository.QuestaoCargoRepository;
import com.studora.service.ConcursoService;
import com.studora.mapper.ConcursoMapper;
import com.studora.mapper.ConcursoCargoMapper;
import com.studora.mapper.InstituicaoMapper;
import com.studora.mapper.BancaMapper;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class ConcursoServiceTest {

    @Mock private ConcursoRepository concursoRepository;
    @Mock private InstituicaoRepository instituicaoRepository;
    @Mock private BancaRepository bancaRepository;
    @Mock private CargoRepository cargoRepository;
    @Mock private ConcursoCargoRepository concursoCargoRepository;
    @Mock private QuestaoCargoRepository questaoCargoRepository;

    private ConcursoService concursoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ConcursoMapper realMapper = org.mapstruct.factory.Mappers.getMapper(ConcursoMapper.class);
        ConcursoCargoMapper ccMapper = org.mapstruct.factory.Mappers.getMapper(ConcursoCargoMapper.class);
        InstituicaoMapper instMapper = org.mapstruct.factory.Mappers.getMapper(InstituicaoMapper.class);
        BancaMapper bancaMapper = org.mapstruct.factory.Mappers.getMapper(BancaMapper.class);
        
        ReflectionTestUtils.setField(realMapper, "instituicaoMapper", instMapper);
        ReflectionTestUtils.setField(realMapper, "bancaMapper", bancaMapper);
        ReflectionTestUtils.setField(realMapper, "concursoCargoMapper", ccMapper);
        
        concursoService = new ConcursoService(
            concursoRepository, 
            instituicaoRepository, 
            bancaRepository, 
            cargoRepository, 
            concursoCargoRepository, 
            questaoCargoRepository,
            realMapper, 
            ccMapper
        );
    }

    @Test
    void testFindById() {
        Instituicao inst = new Instituicao(); inst.setId(1L);
        Banca banca = new Banca(); banca.setId(1L);
        Concurso concurso = new Concurso(inst, banca, 2023, 1);
        concurso.setId(1L);

        when(concursoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(concurso));

        ConcursoDetailDto result = concursoService.getConcursoDetailById(1L);
        assertNotNull(result);
        assertEquals(2023, result.getAno());
    }

    @Test
    void testFindById_NotFound() {
        when(concursoRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> concursoService.getConcursoDetailById(1L));
    }

    @Test
    void testFindAll() {
        Instituicao inst = new Instituicao(); inst.setId(1L);
        Banca banca = new Banca(); banca.setId(1L);
        Concurso c1 = new Concurso(inst, banca, 2023, 1);
        Concurso c2 = new Concurso(inst, banca, 2024, 2);
        
        Page<Concurso> page = new PageImpl<>(Arrays.asList(c1, c2));
        when(concursoRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<ConcursoSummaryDto> result = concursoService.findAll(Pageable.unpaged());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testFindAll_Empty() {
        when(concursoRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));
        Page<ConcursoSummaryDto> result = concursoService.findAll(Pageable.unpaged());
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreate_Success() {
        Instituicao inst = new Instituicao(); inst.setId(1L);
        Banca banca = new Banca(); banca.setId(1L);
        
        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(1L);
        request.setBancaId(1L);
        request.setAno(2023);
        request.setMes(1);

        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(inst));
        when(bancaRepository.findById(1L)).thenReturn(Optional.of(banca));
        when(concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(1L, 1L, 2023, 1)).thenReturn(false);
        
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> {
            Concurso c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        ConcursoDetailDto result = concursoService.create(request);
        assertEquals(1L, result.getId());
        assertEquals(2023, result.getAno());
    }

    @Test
    void testCreate_DuplicateConflict() {
        ConcursoCreateRequest req = new ConcursoCreateRequest();
        req.setInstituicaoId(1L);
        req.setBancaId(1L);
        req.setAno(2023);
        req.setMes(1);

        when(concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(1L, 1L, 2023, 1)).thenReturn(true);

        assertThrows(com.studora.exception.ConflictException.class, () -> {
            concursoService.create(req);
        });
    }

    @Test
    void testUpdate_Success() {
        Long id = 1L;
        Instituicao inst = new Instituicao(); inst.setId(1L);
        Banca banca = new Banca(); banca.setId(1L);
        Concurso existing = new Concurso(inst, banca, 2023, 1);
        existing.setId(id);

        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setAno(2024);

        when(concursoRepository.findById(id)).thenReturn(Optional.of(existing));
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));

        ConcursoDetailDto result = concursoService.update(id, req);
        assertEquals(2024, result.getAno());
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
    void testAddCargo_Success() {
        Long id = 1L;
        Concurso concurso = new Concurso(); concurso.setId(id);
        Cargo cargo = new Cargo(); cargo.setId(2L);
        
        com.studora.dto.request.ConcursoCargoCreateRequest req = new com.studora.dto.request.ConcursoCargoCreateRequest();
        req.setCargoId(2L);

        when(concursoCargoRepository.existsByConcursoIdAndCargoId(id, 2L)).thenReturn(false);
        when(concursoRepository.findById(id)).thenReturn(Optional.of(concurso));
        when(cargoRepository.findById(2L)).thenReturn(Optional.of(cargo));
        when(concursoCargoRepository.save(any(ConcursoCargo.class))).thenAnswer(i -> {
            ConcursoCargo cc = i.getArgument(0);
            cc.setId(10L);
            return cc;
        });

        com.studora.dto.concurso.ConcursoCargoDto result = concursoService.addCargoToConcurso(id, req);
        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void testAddCargo_AlreadyExists() {
        Long id = 1L;
        when(concursoCargoRepository.existsByConcursoIdAndCargoId(id, 2L)).thenReturn(true);
        com.studora.dto.request.ConcursoCargoCreateRequest req = new com.studora.dto.request.ConcursoCargoCreateRequest();
        req.setCargoId(2L);
        assertThrows(com.studora.exception.ConflictException.class, () -> concursoService.addCargoToConcurso(id, req));
    }

    @Test
    void testAddCargo_ConcursoNotFound() {
        when(concursoCargoRepository.existsByConcursoIdAndCargoId(1L, 2L)).thenReturn(false);
        when(concursoRepository.findById(1L)).thenReturn(Optional.empty());
        com.studora.dto.request.ConcursoCargoCreateRequest req = new com.studora.dto.request.ConcursoCargoCreateRequest();
        req.setCargoId(2L);
        assertThrows(ResourceNotFoundException.class, () -> concursoService.addCargoToConcurso(1L, req));
    }

    @Test
    void testAddCargo_CargoNotFound() {
        when(concursoCargoRepository.existsByConcursoIdAndCargoId(1L, 2L)).thenReturn(false);
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(new Concurso()));
        when(cargoRepository.findById(2L)).thenReturn(Optional.empty());
        com.studora.dto.request.ConcursoCargoCreateRequest req = new com.studora.dto.request.ConcursoCargoCreateRequest();
        req.setCargoId(2L);
        assertThrows(ResourceNotFoundException.class, () -> concursoService.addCargoToConcurso(1L, req));
    }

    @Test
    void testRemoveCargo_Success() {
        Long concursoId = 1L;
        Long cargoId = 2L;
        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(10L);

        when(concursoCargoRepository.findByConcursoIdAndCargoId(concursoId, cargoId)).thenReturn(Arrays.asList(cc));
        when(questaoCargoRepository.findByConcursoCargoId(10L)).thenReturn(Collections.emptyList());
        when(concursoCargoRepository.countByConcursoId(concursoId)).thenReturn(2L);

        concursoService.removeCargoFromConcurso(concursoId, cargoId);
        verify(concursoCargoRepository).delete(cc);
    }

    @Test
    void testRemoveCargo_FailsIfHasQuestions() {
        Long concursoId = 1L;
        Long cargoId = 2L;
        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(10L);

        when(concursoCargoRepository.findByConcursoIdAndCargoId(concursoId, cargoId)).thenReturn(Arrays.asList(cc));
        when(questaoCargoRepository.findByConcursoCargoId(10L)).thenReturn(Arrays.asList(new com.studora.entity.QuestaoCargo()));

        assertThrows(ValidationException.class, () -> concursoService.removeCargoFromConcurso(concursoId, cargoId));
    }

    @Test
    void testRemoveLastCargo_Fails() {
        Long concursoId = 1L;
        Long cargoId = 2L;
        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(10L);

        when(concursoCargoRepository.findByConcursoIdAndCargoId(concursoId, cargoId)).thenReturn(Arrays.asList(cc));
        when(questaoCargoRepository.findByConcursoCargoId(10L)).thenReturn(Collections.emptyList());
        when(concursoCargoRepository.countByConcursoId(concursoId)).thenReturn(1L);

        assertThrows(ValidationException.class, () -> concursoService.removeCargoFromConcurso(concursoId, cargoId));
    }
}
