package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.studora.dto.CargoDto;
import com.studora.entity.Cargo;
import com.studora.entity.NivelCargo;
import com.studora.exception.ConflictException;
import com.studora.repository.CargoRepository;
import com.studora.service.CargoService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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
    void testSaveAndGet() {
        Cargo cargo = new Cargo();
        cargo.setId(1L);
        cargo.setNome("Juiz");
        cargo.setNivel(NivelCargo.SUPERIOR);
        cargo.setArea("Judiciário");

        CargoDto cargoDto = new CargoDto();
        cargoDto.setNome("Juiz");
        cargoDto.setNivel(NivelCargo.SUPERIOR);
        cargoDto.setArea("Judiciário");

        when(cargoRepository.save(any(Cargo.class))).thenAnswer(i -> {
            Cargo c = i.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));

        CargoDto saved = cargoService.save(cargoDto);
        assertNotNull(saved);
        assertEquals(1L, saved.getId());

        CargoDto found = cargoService.findById(1L);
        assertEquals("Juiz", found.getNome());
    }

    @Test
    void testSave_CaseInsensitiveDuplicate() {
        // Test that case-insensitive duplicates are caught
        CargoDto dto = new CargoDto();
        dto.setId(1L);
        dto.setNome("analista de sistemas"); // lowercase
        dto.setNivel(NivelCargo.SUPERIOR);
        dto.setArea("tecnologia"); // lowercase

        Cargo existingCargo = new Cargo();
        existingCargo.setId(2L);
        existingCargo.setNome("ANALISTA DE SISTEMAS"); // uppercase
        existingCargo.setNivel(NivelCargo.SUPERIOR);
        existingCargo.setArea("TECNOLOGIA"); // uppercase

        when(cargoRepository.findByNomeAndNivelAndArea(eq("analista de sistemas"), eq(NivelCargo.SUPERIOR), eq("tecnologia")))
            .thenReturn(Optional.of(existingCargo));

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            cargoService.save(dto);
        });

        assertTrue(exception.getMessage().contains("Já existe um cargo com o nome 'analista de sistemas'"));
    }
}
