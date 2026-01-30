package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.InstituicaoDto;
import com.studora.entity.Instituicao;
import com.studora.repository.InstituicaoRepository;
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
class InstituicaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Test
    void testCreateInstituicao() throws Exception {
        InstituicaoDto dto = new InstituicaoDto();
        dto.setNome("USP");

        mockMvc
            .perform(
                post("/api/instituicoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(dto))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("USP"));
    }

    @Test
    void testGetInstituicao() throws Exception {
        Instituicao inst = new Instituicao();
        inst.setNome("UNICAMP");
        inst = instituicaoRepository.save(inst);

        mockMvc
            .perform(get("/api/instituicoes/{id}", inst.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("UNICAMP"));
    }

    @Test
    void testGetAll() throws Exception {
        Instituicao inst = new Instituicao();
        inst.setNome("UFRJ");
        instituicaoRepository.save(inst);

        mockMvc
            .perform(get("/api/instituicoes"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(1)
                )
            );
    }
}