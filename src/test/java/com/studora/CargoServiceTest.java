package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.CargoDto;
import com.studora.entity.Cargo;
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

    @InjectMocks
    private CargoService cargoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveAndGet() {
        Cargo cargo = new Cargo();
        cargo.setId(1L);
        cargo.setNome("Juiz");

        when(cargoRepository.save(any(Cargo.class))).thenReturn(cargo);
        when(cargoRepository.findById(1L)).thenReturn(Optional.of(cargo));

        CargoDto saved = cargoService.save(new CargoDto());
        assertNotNull(saved);

        CargoDto found = cargoService.findById(1L);
        assertEquals("Juiz", found.getNome());
    }
}
