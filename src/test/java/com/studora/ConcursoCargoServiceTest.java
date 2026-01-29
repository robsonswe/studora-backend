package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.ConcursoCargoDto;
import com.studora.entity.Cargo;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.repository.CargoRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.service.ConcursoCargoService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ConcursoCargoServiceTest {

    @Mock
    private ConcursoCargoRepository concursoCargoRepository;

    @Mock
    private ConcursoRepository concursoRepository;

    @Mock
    private CargoRepository cargoRepository;

    @InjectMocks
    private ConcursoCargoService concursoCargoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        ConcursoCargoDto dto = new ConcursoCargoDto();
        dto.setConcursoId(1L);
        dto.setCargoId(2L);

        when(concursoRepository.findById(1L)).thenReturn(
            Optional.of(new Concurso())
        );
        when(cargoRepository.findById(2L)).thenReturn(Optional.of(new Cargo()));

        ConcursoCargo saved = new ConcursoCargo();
        saved.setId(10L);
        saved.setConcurso(new Concurso());
        saved.getConcurso().setId(1L);
        saved.setCargo(new Cargo());
        saved.getCargo().setId(2L);

        when(concursoCargoRepository.save(any(ConcursoCargo.class))).thenReturn(
            saved
        );

        ConcursoCargoDto result = concursoCargoService.save(dto);
        assertEquals(10L, result.getId());
    }
}
