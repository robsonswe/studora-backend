package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.InstituicaoDto;
import com.studora.dto.request.InstituicaoCreateRequest;
import com.studora.dto.request.InstituicaoUpdateRequest;
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
        InstituicaoCreateRequest request = new InstituicaoCreateRequest();
        request.setNome("USP");
        request.setArea("Estadual");

        mockMvc
            .perform(
                post("/api/instituicoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("USP"))
            .andExpect(jsonPath("$.area").value("Estadual"));
    }

    @Test
    void testGetInstituicao() throws Exception {
        Instituicao inst = new Instituicao();
        inst.setNome("UNICAMP");
        inst.setArea("Estadual");
        inst = instituicaoRepository.save(inst);

        mockMvc
            .perform(get("/api/instituicoes/{id}", inst.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("UNICAMP"))
            .andExpect(jsonPath("$.area").value("Estadual"));
    }

    @Test
    void testGetAll() throws Exception {
        Instituicao inst = new Instituicao();
        inst.setNome("UFRJ");
        inst.setArea("Educação");
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

    @Test
    void testUpdateInstituicao() throws Exception {
        // First create an institution
        Instituicao inst = new Instituicao();
        inst.setNome("Instituição Original");
        inst.setArea("Educação");
        inst = instituicaoRepository.save(inst);

        // Create update request
        InstituicaoUpdateRequest request = new InstituicaoUpdateRequest();
        request.setNome("Instituição Atualizada");
        request.setArea("Educação Superior");

        mockMvc
            .perform(
                put("/api/instituicoes/{id}", inst.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Instituição Atualizada"))
            .andExpect(jsonPath("$.area").value("Educação Superior"));
    }

    @Test
    void testCreateInstituicao_Conflict_DuplicateName() throws Exception {
        // Create first instituicao
        Instituicao inst1 = new Instituicao();
        inst1.setNome("Universidade Federal do Rio de Janeiro");
        inst1.setArea("Educação");
        instituicaoRepository.save(inst1);

        // Try to create another instituicao with the same name
        InstituicaoCreateRequest request = new InstituicaoCreateRequest();
        request.setNome("Universidade Federal do Rio de Janeiro");
        request.setArea("Educação");

        mockMvc
            .perform(
                post("/api/instituicoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Conflito"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Já existe uma instituição com o nome 'Universidade Federal do Rio de Janeiro'"));
    }

    @Test
    void testCreateInstituicao_Conflict_DuplicateName_CaseInsensitive() throws Exception {
        // Create first instituicao
        Instituicao inst1 = new Instituicao();
        inst1.setNome("Banco Central");
        inst1.setArea("Financeira");
        instituicaoRepository.save(inst1);

        // Try to create another instituicao with the same name but different case
        InstituicaoCreateRequest request = new InstituicaoCreateRequest();
        request.setNome("banco central");
        request.setArea("Financeira");

        mockMvc
            .perform(
                post("/api/instituicoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Já existe uma instituição com o nome 'banco central'"));
    }
}