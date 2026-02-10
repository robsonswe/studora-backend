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
        InstituicaoMapper instMapper = org.mapstruct.factory.Mappers.getMapper(InstituicaoMapper.class);
        BancaMapper bancaMapper = org.mapstruct.factory.Mappers.getMapper(BancaMapper.class);
        com.studora.mapper.CargoMapper cargoMapper = org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.CargoMapper.class);
        
        ReflectionTestUtils.setField(realMapper, "instituicaoMapper", instMapper);
        ReflectionTestUtils.setField(realMapper, "bancaMapper", bancaMapper);
        
        concursoService = new ConcursoService(
            concursoRepository, 
            instituicaoRepository, 
            bancaRepository, 
            cargoRepository, 
            concursoCargoRepository, 
            questaoCargoRepository,
            realMapper
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
        when(concursoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<ConcursoSummaryDto> result = concursoService.findAll(new com.studora.dto.concurso.ConcursoFilter(), Pageable.unpaged());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testCreate_Success() {
        Instituicao inst = new Instituicao(); inst.setId(1L);
        Banca banca = new Banca(); banca.setId(1L);
        Cargo cargo = new Cargo(); cargo.setId(10L);
        
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

        ConcursoDetailDto result = concursoService.create(request);
        assertEquals(1L, result.getId());
        assertEquals(2023, result.getAno());
        assertEquals(1, result.getCargos().size());
        assertEquals(10L, result.getCargos().get(0).getId());
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
        Instituicao inst = new Instituicao(); inst.setId(1L);
        Banca banca = new Banca(); banca.setId(1L);
        Cargo cargo = new Cargo(); cargo.setId(10L);
        
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

        ConcursoDetailDto result = concursoService.update(id, req);
        assertEquals(2024, result.getAno());
        assertEquals(1, result.getCargos().size());
    }

    @Test
    void testUpdate_AddAndRemoveCargos() {
        Long id = 1L;
        Instituicao inst = new Instituicao(); inst.setId(1L);
        Banca banca = new Banca(); banca.setId(1L);
        
        Cargo cargo1 = new Cargo(); cargo1.setId(10L);
        Cargo cargo2 = new Cargo(); cargo2.setId(20L);
        
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
        when(questaoCargoRepository.findByConcursoCargoId(100L)).thenReturn(Collections.emptyList()); // Not used
        
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));

        ConcursoDetailDto result = concursoService.update(id, req);
        
        assertEquals(1, result.getCargos().size());
        assertEquals(20L, result.getCargos().get(0).getId());
    }
    
    @Test
    void testUpdate_RemoveCargo_FailIfUsed() {
        Long id = 1L;
        Instituicao inst = new Instituicao(); inst.setId(1L);
        Banca banca = new Banca(); banca.setId(1L);
        Cargo cargo1 = new Cargo(); cargo1.setId(10L);
        
        Concurso existing = new Concurso(inst, banca, 2023, 1);
        existing.setId(id);
        
        ConcursoCargo cc1 = new ConcursoCargo();
        cc1.setConcurso(existing);
        cc1.setCargo(cargo1);
        cc1.setId(100L);
        existing.addConcursoCargo(cc1);

        // Update: Remove Cargo 1
        ConcursoUpdateRequest req = new ConcursoUpdateRequest();
        req.setCargos(List.of(20L)); // Try to replace with 20L

        when(concursoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(questaoCargoRepository.findByConcursoCargoId(100L)).thenReturn(List.of(new com.studora.entity.QuestaoCargo())); // Used!
        
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
}
