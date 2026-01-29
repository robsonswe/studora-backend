package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.ImagemDto;
import com.studora.entity.Imagem;
import com.studora.repository.ImagemRepository;
import com.studora.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ImagemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImagemRepository imagemRepository;

    @Test
    void testCreateImagem() throws Exception {
        ImagemDto dto = new ImagemDto();
        dto.setUrl("http://test.com/img.png");
        dto.setDescricao("Test Image");

        mockMvc
            .perform(
                post("/api/imagens")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(dto))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.url").value("http://test.com/img.png"));
    }

    @Test
    void testGetImagem() throws Exception {
        Imagem img = new Imagem("http://url.com", "Desc");
        img = imagemRepository.save(img);

        mockMvc
            .perform(get("/api/imagens/{id}", img.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.descricao").value("Desc"));
    }

    @Test
    void testUpdateImagem() throws Exception {
        Imagem img = new Imagem("http://old.com", "Old");
        img = imagemRepository.save(img);

        ImagemDto dto = new ImagemDto("http://new.com", "New");

        mockMvc
            .perform(
                put("/api/imagens/{id}", img.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(dto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.descricao").value("New"));
    }

    @Test
    void testDeleteImagem() throws Exception {
        Imagem img = new Imagem("http://del.com", "Del");
        img = imagemRepository.save(img);

        mockMvc
            .perform(delete("/api/imagens/{id}", img.getId()))
            .andExpect(status().isNoContent());
    }
}
