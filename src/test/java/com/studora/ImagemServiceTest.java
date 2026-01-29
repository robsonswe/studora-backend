package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.ImagemDto;
import com.studora.entity.Imagem;
import com.studora.repository.ImagemRepository;
import com.studora.service.ImagemService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ImagemServiceTest {

    @Mock
    private ImagemRepository imagemRepository;

    @InjectMocks
    private ImagemService imagemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate() {
        ImagemDto dto = new ImagemDto("url", "desc");
        Imagem saved = new Imagem("url", "desc");
        saved.setId(1L);

        when(imagemRepository.save(any(Imagem.class))).thenReturn(saved);

        ImagemDto result = imagemService.createImagem(dto);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetById_NotFound() {
        when(imagemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            imagemService.getImagemById(1L)
        );
    }
}
