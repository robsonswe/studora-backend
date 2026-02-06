package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.cargo.CargoDetailDto;
import com.studora.dto.request.CargoCreateRequest;
import com.studora.entity.Cargo;
import com.studora.entity.NivelCargo;
import com.studora.repository.CargoRepository;
import com.studora.service.CargoService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CargoServiceTest {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private com.studora.repository.ConcursoCargoRepository concursoCargoRepository;

    private CargoService cargoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        com.studora.mapper.CargoMapper realMapper = org.mapstruct.factory.Mappers.getMapper(com.studora.mapper.CargoMapper.class);
        cargoService = new CargoService(cargoRepository, realMapper, concursoCargoRepository);
    }

    @Test
    void testFindById() {
        Cargo cargo = new Cargo();
        cargo.setId(1L);
        cargo.setNome("Analista");
        cargo.setNivel(NivelCargo.SUPERIOR);

        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));

        CargoDetailDto result = cargoService.getCargoDetailById(1L);
        assertNotNull(result);
        assertEquals("Analista", result.getNome());
    }

    @Test
    void testCreate_Success() {
        CargoCreateRequest request = new CargoCreateRequest();
        request.setNome("Novo Cargo");
        request.setNivel(NivelCargo.SUPERIOR);
        request.setArea("TI");

        when(cargoRepository.findByNomeAndNivelAndArea("Novo Cargo", NivelCargo.SUPERIOR, "TI")).thenReturn(Optional.empty());
        when(cargoRepository.save(any(Cargo.class))).thenAnswer(i -> {
            Cargo c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        CargoDetailDto result = cargoService.create(request);
        assertEquals(1L, result.getId());
        assertEquals("Novo Cargo", result.getNome());
    }

    @Test
    void testCreate_Conflict_Duplicate() {
        CargoCreateRequest request = new CargoCreateRequest();
        request.setNome("analista");
        request.setNivel(NivelCargo.SUPERIOR);
        request.setArea("tecnologia");

        Cargo existing = new Cargo();
        existing.setId(2L);
        existing.setNome("ANALISTA");
        existing.setNivel(NivelCargo.SUPERIOR);
        existing.setArea("TECNOLOGIA");

        when(cargoRepository.findByNomeAndNivelAndArea(any(), any(), any())).thenReturn(Optional.of(existing));

        assertThrows(com.studora.exception.ConflictException.class, () -> {
            cargoService.create(request);
        });
    }

    @Test
    void testDelete_FailsWithAssociatedExams() {
        when(cargoRepository.existsById(1L)).thenReturn(true);
        when(concursoCargoRepository.existsByCargoId(1L)).thenReturn(true);

        com.studora.exception.ValidationException exception = assertThrows(
            com.studora.exception.ValidationException.class, 
            () -> cargoService.delete(1L)
        );

        assertTrue(exception.getMessage().contains("possui concursos associados"));
    }
}