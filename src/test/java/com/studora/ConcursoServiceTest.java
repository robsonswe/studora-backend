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

    @InjectMocks
    private ConcursoService concursoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        Concurso concurso1 = new Concurso(instituicao1, banca1, 2023);
        concurso1.setId(1L);

        Instituicao instituicao2 = new Instituicao();
        instituicao2.setId(2L);
        instituicao2.setNome("Instituição 2");

        Banca banca2 = new Banca();
        banca2.setId(2L);
        banca2.setNome("Banca 2");

        Concurso concurso2 = new Concurso(instituicao2, banca2, 2024);
        concurso2.setId(2L);

        when(concursoRepository.findAll()).thenReturn(Arrays.asList(concurso1, concurso2));

        // Act
        List<ConcursoDto> result = concursoService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getInstituicaoId());
        assertEquals(1L, result.get(0).getBancaId());
        assertEquals(2023, result.get(0).getAno());
        assertEquals(2L, result.get(1).getInstituicaoId());
        assertEquals(2L, result.get(1).getBancaId());
        assertEquals(2024, result.get(1).getAno());

        verify(concursoRepository, times(1)).findAll();
    }

    @Test
    void testGetAllConcursos_Empty() {
        // Arrange
        when(concursoRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ConcursoDto> result = concursoService.findAll();

        // Assert
        assertTrue(result.isEmpty());
        verify(concursoRepository, times(1)).findAll();
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

        Concurso concurso = new Concurso(instituicao, banca, 2023);
        concurso.setId(1L);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));

        // Act
        ConcursoDto result = concursoService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getInstituicaoId());
        assertEquals(1L, result.getBancaId());
        assertEquals(2023, result.getAno());
        verify(concursoRepository, times(1)).findById(1L);
    }

    @Test
    void testGetConcursoById_NotFound() {
        // Arrange
        when(concursoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            concursoService.findById(1L);
        });

        verify(concursoRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateConcurso() {
        // Arrange
        ConcursoDto concursoDto = new ConcursoDto();
        concursoDto.setInstituicaoId(1L);
        concursoDto.setBancaId(1L);
        concursoDto.setAno(2023);

        Instituicao instituicao = new Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição 1");

        Banca banca = new Banca();
        banca.setId(1L);
        banca.setNome("Banca 1");

        // Mock the repository calls that happen inside the service
        when(instituicaoRepository.findById(1L)).thenReturn(Optional.of(instituicao));
        when(bancaRepository.findById(1L)).thenReturn(Optional.of(banca));

        Concurso savedConcurso = new Concurso(instituicao, banca, 2023);
        savedConcurso.setId(1L);

        when(concursoRepository.save(any(Concurso.class))).thenReturn(savedConcurso);

        // Act
        ConcursoDto result = concursoService.save(concursoDto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getInstituicaoId());
        assertEquals(1L, result.getBancaId());
        assertEquals(2023, result.getAno());
        verify(concursoRepository, times(1)).save(any(Concurso.class));
        verify(instituicaoRepository, times(1)).findById(1L);
        verify(bancaRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateConcurso_NullDto() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            concursoService.save(null);
        });
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

        Instituicao instituicao = new Instituicao();
        instituicao.setId(1L);
        instituicao.setNome("Instituição 1");

        Banca banca = new Banca();
        banca.setId(1L);
        banca.setNome("Banca 1");

        Concurso existingConcurso = new Concurso(instituicao, banca, 2023);
        existingConcurso.setId(concursoId);

        // Mock the repository calls that happen inside the service
        Instituicao updatedInstituicao = new Instituicao();
        updatedInstituicao.setId(2L);
        updatedInstituicao.setNome("Instituição 2");

        Banca updatedBanca = new Banca();
        updatedBanca.setId(2L);
        updatedBanca.setNome("Banca 2");

        when(instituicaoRepository.findById(2L)).thenReturn(Optional.of(updatedInstituicao));
        when(bancaRepository.findById(2L)).thenReturn(Optional.of(updatedBanca));

        when(concursoRepository.findById(concursoId)).thenReturn(Optional.of(existingConcurso));
        when(concursoRepository.save(any(Concurso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ConcursoDto updatedDto = concursoService.save(concursoDto);

        // Assert
        assertNotNull(updatedDto);
        assertEquals(concursoDto.getInstituicaoId(), updatedDto.getInstituicaoId());
        assertEquals(concursoDto.getBancaId(), updatedDto.getBancaId());
        assertEquals(concursoDto.getAno(), updatedDto.getAno());

        verify(concursoRepository, times(1)).findById(concursoId);
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

        Cargo cargo = new Cargo();
        cargo.setId(2L);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setId(5L);
        concursoCargo.setConcurso(concurso);
        concursoCargo.setCargo(cargo);

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(concurso));
        when(cargoRepository.findById(2L)).thenReturn(Optional.of(cargo));
        when(concursoCargoRepository.findByConcursoIdAndCargoId(1L, 2L)).thenReturn(List.of()); // No existing association
        when(concursoCargoRepository.save(any(ConcursoCargo.class))).thenReturn(concursoCargo);

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