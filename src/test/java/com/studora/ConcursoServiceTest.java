package com.studora;

import com.studora.dto.ConcursoCargoDto;
import com.studora.dto.ConcursoDto;
import com.studora.entity.Banca;
import com.studora.entity.Cargo;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Instituicao;
import com.studora.repository.BancaRepository;
import com.studora.repository.CargoRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.InstituicaoRepository;
import com.studora.service.ConcursoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private com.studora.repository.QuestaoCargoRepository questaoCargoRepository;

    private ConcursoService concursoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Use real mappers
        com.studora.mapper.ConcursoMapper realConcursoMapper = org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.ConcursoMapper.class);
        com.studora.mapper.ConcursoCargoMapper realConcursoCargoMapper = org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.ConcursoCargoMapper.class);

        concursoService = new ConcursoService(
            concursoRepository,
            instituicaoRepository,
            bancaRepository,
            cargoRepository,
            concursoCargoRepository,
            questaoCargoRepository,
            realConcursoMapper,
            realConcursoCargoMapper
        );
    }

    @Test
    void testGetAllConcursos() {
        // Arrange
        Instituicao instituicao1 = new Instituicao();
        instituicao1.setId(1L);
        instituicao1.setNome("Instituição 1");

        Banca banca1 = new Banca();
        banca1.setId(1L);
        banca1.setNome("Banca 1");

        Concurso concurso1 = new Concurso(instituicao1, banca1, 2023, 1);
        concurso1.setId(1L);

        Instituicao instituicao2 = new Instituicao();
        instituicao2.setId(2L);
        instituicao2.setNome("Instituição 2");

        Banca banca2 = new Banca();
        banca2.setId(2L);
        banca2.setNome("Banca 2");

        Concurso concurso2 = new Concurso(instituicao2, banca2, 2024, 2);
        concurso2.setId(2L);

        ConcursoDto dto1 = new ConcursoDto();
        dto1.setId(1L);
        dto1.setInstituicaoId(1L);
        dto1.setBancaId(1L);
        dto1.setAno(2023);
        dto1.setMes(1);

        ConcursoDto dto2 = new ConcursoDto();
        dto2.setId(2L);
        dto2.setInstituicaoId(2L);
        dto2.setBancaId(2L);
        dto2.setAno(2024);
        dto2.setMes(2);

        Page<Concurso> page = new PageImpl<>(Arrays.asList(concurso1, concurso2));
        when(concursoRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        Page<ConcursoDto> result = concursoService.findAll(Pageable.unpaged());

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getInstituicaoId());
        verify(concursoRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testGetAllConcursos_Empty() {
        // Arrange
        Page<Concurso> page = new PageImpl<>(Collections.emptyList());
        when(concursoRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        Page<ConcursoDto> result = concursoService.findAll(Pageable.unpaged());

        // Assert
        assertTrue(result.isEmpty());
        verify(concursoRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testGetConcursoById_Success() {
        // Arrange
        Instituicao instituicao = new Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição 1");

        Banca banca = new Banca();
        banca.setId(1L);
        banca.setNome("Banca 1");

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso.setId(1L);

        ConcursoDto dto = new ConcursoDto();
        dto.setId(1L);
        dto.setInstituicaoId(1L);
        dto.setBancaId(1L);
        dto.setAno(2023);
        dto.setMes(1);

        when(concursoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(concurso));

        // Act
        ConcursoDto result = concursoService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getInstituicaoId());
        verify(concursoRepository, times(1)).findByIdWithDetails(1L);
    }

    @Test
    void testGetConcursoById_NotFound() {
        // Arrange
        when(concursoRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoService.findById(1L);
        });

        verify(concursoRepository, times(1)).findByIdWithDetails(1L);
    }

    @Test
    void testCreateConcurso() {
        // Arrange
        ConcursoDto concursoDto = new ConcursoDto();
        concursoDto.setInstituicaoId(1L);
        concursoDto.setBancaId(1L);
        concursoDto.setAno(2023);
        concursoDto.setMes(1);

        Instituicao instituicao = new Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição 1");

        Banca banca = new Banca();
        banca.setId(1L);
        banca.setNome("Banca 1");

        Concurso entity = new Concurso();
        entity.setInstituicao(instituicao);
        entity.setBanca(banca);
        entity.setAno(2023);
        entity.setMes(1);

        Concurso savedConcurso = new Concurso(instituicao, banca, 2023, 1);
        savedConcurso.setId(1L);

        ConcursoDto resultDto = new ConcursoDto();
        resultDto.setId(1L);
        resultDto.setInstituicaoId(1L);
        resultDto.setBancaId(1L);
        resultDto.setAno(2023);
        resultDto.setMes(1);

        // Mock the repository calls that happen inside the service
        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(instituicao));
        when(bancaRepository.findById(1L)).thenReturn(Optional.of(banca));
        when(concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(false);
        // Repository save should return an entity with ID (simulated)
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> {
            Concurso c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        // Act
        ConcursoDto result = concursoService.save(concursoDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(concursoRepository, times(1)).save(any(Concurso.class));
    }

    @Test
    void testCreateConcurso_NullDto() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            concursoService.save(null);
        });
    }

    @Test
    void testCreateConcurso_DuplicateConflict() {
        // Arrange
        ConcursoDto dto = new ConcursoDto();
        dto.setInstituicaoId(1L);
        dto.setBancaId(1L);
        dto.setAno(2023);
        dto.setMes(1);

        when(concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(1L, 1L, 2023, 1)).thenReturn(true);

        // Act & Assert
        com.studora.exception.ConflictException exception = assertThrows(com.studora.exception.ConflictException.class, () -> {
            concursoService.save(dto);
        });

        assertTrue(exception.getMessage().contains("Já existe um concurso cadastrado"));
        verify(concursoRepository, never()).save(any(Concurso.class));
    }

    @Test
    void testUpdateConcurso_Success() {
        // Arrange
        Long concursoId = 1L;

        ConcursoDto concursoDto = new ConcursoDto();
        concursoDto.setId(concursoId);
        concursoDto.setInstituicaoId(2L);
        concursoDto.setBancaId(2L);
        concursoDto.setAno(2024);
        concursoDto.setMes(2);

        Instituicao instituicao = new Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição 1");

        Banca banca = new Banca();
        banca.setId(1L);
        banca.setNome("Banca 1");

        Concurso existingConcurso = new Concurso(instituicao, banca, 2023, 1);
        existingConcurso.setId(concursoId);

        // Mock the repository calls that happen inside the service
        Instituicao updatedInstituicao = new Instituicao();
        updatedInstituicao.setId(2L);
        updatedInstituicao.setNome("Instituição 2");

        Banca updatedBanca = new Banca();
        updatedBanca.setId(2L);
        updatedBanca.setNome("Banca 2");

        ConcursoDto resultDto = new ConcursoDto();
        resultDto.setId(concursoId);
        resultDto.setInstituicaoId(2L);
        resultDto.setBancaId(2L);
        resultDto.setAno(2024);
        resultDto.setMes(2);

        when(instituicaoRepository.findById(2L)).thenReturn(Optional.of(updatedInstituicao));
        when(bancaRepository.findById(2L)).thenReturn(Optional.of(updatedBanca));

        when(concursoRepository.findById(concursoId)).thenReturn(Optional.of(existingConcurso));
        when(concursoRepository.existsByInstituicaoIdAndBancaIdAndAnoAndMes(anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(false);
        // Save returns the same instance (updated)
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ConcursoDto updatedDto = concursoService.save(concursoDto);

        // Assert
        assertNotNull(updatedDto);
        assertEquals(concursoDto.getInstituicaoId(), updatedDto.getInstituicaoId());
        verify(concursoRepository, atLeastOnce()).findById(concursoId);
        verify(concursoRepository, times(1)).save(any(Concurso.class));
    }

    @Test
    void testUpdateConcurso_NotFound() {
        // Arrange
        Long concursoId = 1L;

        ConcursoDto concursoDto = new ConcursoDto();
        concursoDto.setId(concursoId);
        concursoDto.setInstituicaoId(2L);
        concursoDto.setBancaId(2L);
        concursoDto.setAno(2024);

        // Mock the repository calls that happen inside the service before the findById check
        Instituicao instituicao = new Instituicao();
        instituicao.setId(2L);
        instituicao.setNome("Instituição 2");

        Banca banca = new Banca();
        banca.setId(2L);
        banca.setNome("Banca 2");

        when(instituicaoRepository.findById(2L)).thenReturn(Optional.of(instituicao));
        when(bancaRepository.findById(2L)).thenReturn(Optional.of(banca));

        when(concursoRepository.findById(concursoId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoDto.setId(concursoId);
            concursoService.save(concursoDto);
        });

        verify(concursoRepository, times(1)).findById(concursoId);
        verify(concursoRepository, never()).save(any(Concurso.class));
    }

    @Test
    void testDeleteConcurso_Success() {
        // Arrange
        Long concursoId = 1L;

        when(concursoRepository.existsById(concursoId)).thenReturn(true);
        doNothing().when(concursoRepository).deleteById(concursoId);

        // Act
        concursoService.deleteById(concursoId);

        // Assert
        verify(concursoRepository, times(1)).existsById(concursoId);
        verify(concursoRepository, times(1)).deleteById(concursoId);
    }

    @Test
    void testDeleteConcurso_NotFound() {
        // Arrange
        Long concursoId = 1L;

        when(concursoRepository.existsById(concursoId)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoService.deleteById(concursoId);
        });

        verify(concursoRepository, times(1)).existsById(concursoId);
        verify(concursoRepository, never()).deleteById(anyLong());
    }

    @Test
    void testAddCargoToConcurso_Success() {
        // Arrange
        ConcursoCargoDto concursoCargoDto = new ConcursoCargoDto();
        concursoCargoDto.setConcursoId(1L);
        concursoCargoDto.setCargoId(2L);

        Concurso concurso = new Concurso();
        concurso.setId(1L);
        concurso.setMes(1);

        Cargo cargo = new Cargo();
        cargo.setId(2L);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(5L);
        concursoCargo.setConcurso(concurso);
        concursoCargo.setCargo(cargo);

        ConcursoCargoDto resultDto = new ConcursoCargoDto();
        resultDto.setId(5L);
        resultDto.setConcursoId(1L);
        resultDto.setCargoId(2L);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(cargoRepository.findById(2L)).thenReturn(Optional.of(cargo));
        when(concursoCargoRepository.findByConcursoIdAndCargoId(1L, 2L)).thenReturn(List.of()); // No existing association
        when(concursoCargoRepository.save(any(ConcursoCargo.class))).thenAnswer(i -> {
            ConcursoCargo cc = i.getArgument(0);
            cc.setId(5L);
            return cc;
        });

        // Act
        ConcursoCargoDto result = concursoService.addCargoToConcurso(concursoCargoDto);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(1L, result.getConcursoId());
        assertEquals(2L, result.getCargoId());
        verify(concursoRepository, times(1)).findById(1L);
        verify(cargoRepository, times(1)).findById(2L);
        verify(concursoCargoRepository, times(1)).findByConcursoIdAndCargoId(1L, 2L);
        verify(concursoCargoRepository, times(1)).save(any(ConcursoCargo.class));
    }

    @Test
    void testAddCargoToConcurso_AlreadyExists() {
        // Arrange
        ConcursoCargoDto concursoCargoDto = new ConcursoCargoDto();
        concursoCargoDto.setConcursoId(1L);
        concursoCargoDto.setCargoId(2L);

        Concurso concurso = new Concurso();
        concurso.setId(1L);
        concurso.setMes(1);

        Cargo cargo = new Cargo();
        cargo.setId(2L);

        ConcursoCargo existingConcursoCargo = new ConcursoCargo();
        existingConcursoCargo.setId(5L);
        existingConcursoCargo.setConcurso(concurso);
        existingConcursoCargo.setCargo(cargo);

        when(concursoCargoRepository.findByConcursoIdAndCargoId(1L, 2L)).thenReturn(Arrays.asList(existingConcursoCargo));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoService.addCargoToConcurso(concursoCargoDto);
        });

        verify(concursoCargoRepository, times(1)).findByConcursoIdAndCargoId(1L, 2L);
        verify(concursoCargoRepository, never()).save(any(ConcursoCargo.class)); // Should not save if already exists
    }

    @Test
    void testAddCargoToConcurso_NonExistentConcurso() {
        // Arrange
        ConcursoCargoDto concursoCargoDto = new ConcursoCargoDto();
        concursoCargoDto.setConcursoId(999L); // Non-existent concurso ID
        concursoCargoDto.setCargoId(2L);

        when(concursoCargoRepository.findByConcursoIdAndCargoId(999L, 2L)).thenReturn(List.of()); // No existing association
        when(concursoRepository.findById(999L)).thenReturn(Optional.empty()); // Concurso does not exist

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoService.addCargoToConcurso(concursoCargoDto);
        });

        verify(concursoCargoRepository, times(1)).findByConcursoIdAndCargoId(999L, 2L);
        verify(concursoRepository, times(1)).findById(999L);
        verify(cargoRepository, never()).findById(anyLong()); // Should not check cargo if concurso doesn't exist
        verify(concursoCargoRepository, never()).save(any(ConcursoCargo.class)); // Should not save
    }

    @Test
    void testAddCargoToConcurso_NonExistentCargo() {
        // Arrange
        ConcursoCargoDto concursoCargoDto = new ConcursoCargoDto();
        concursoCargoDto.setConcursoId(1L);
        concursoCargoDto.setCargoId(999L); // Non-existent cargo ID

        Concurso concurso = new Concurso();
        concurso.setId(1L);

        when(concursoCargoRepository.findByConcursoIdAndCargoId(1L, 999L)).thenReturn(List.of()); // No existing association
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso)); // Concurso exists
        when(cargoRepository.findById(999L)).thenReturn(Optional.empty()); // Cargo does not exist

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoService.addCargoToConcurso(concursoCargoDto);
        });

        verify(concursoCargoRepository, times(1)).findByConcursoIdAndCargoId(1L, 999L);
        verify(concursoRepository, times(1)).findById(1L);
        verify(cargoRepository, times(1)).findById(999L);
        verify(concursoCargoRepository, never()).save(any(ConcursoCargo.class)); // Should not save
    }

    @Test
    void testRemoveLastCargoFromConcurso_FailsValidation() {
        // Arrange
        Long concursoId = 1L;
        Long cargoId = 2L;

        ConcursoCargo existingAssociation = new ConcursoCargo();
        existingAssociation.setConcurso(new Concurso());
        existingAssociation.getConcurso().setId(concursoId);
        existingAssociation.setCargo(new Cargo());
        existingAssociation.getCargo().setId(cargoId);

        when(concursoCargoRepository.findByConcursoIdAndCargoId(concursoId, cargoId))
            .thenReturn(Arrays.asList(existingAssociation));
        when(concursoCargoRepository.findByConcursoId(concursoId))
            .thenReturn(Arrays.asList(existingAssociation)); // Only one association exists
        // Mock new validation to pass (no affected questions)
        when(questaoCargoRepository.findByConcursoCargoId(anyLong())).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoService.removeCargoFromConcurso(concursoId, cargoId);
        });

        verify(concursoCargoRepository, times(1)).findByConcursoIdAndCargoId(concursoId, cargoId);
        verify(concursoCargoRepository, times(1)).findByConcursoId(concursoId);
        verify(concursoCargoRepository, never()).deleteAll(anyList()); // Should not delete if it would leave no associations
    }
}