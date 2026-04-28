package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.InstituicaoCreateRequest;
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
    void testGetAllInstituicoes() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Polícia Federal");
        instituicao.setArea("Policial");
        instituicaoRepository.save(instituicao);

        mockMvc
            .perform(get("/api/v1/instituicoes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("Polícia Federal"))
            .andExpect(jsonPath("$.pageNumber").value(0));
    }

    @Test
    void testGetAllInstituicoes_WithSigla() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Universidade Federal do Rio de Janeiro");
        instituicao.setArea("Educação");
        instituicao.setSigla("UFRJ");
        instituicaoRepository.save(instituicao);

        mockMvc
            .perform(get("/api/v1/instituicoes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("Universidade Federal do Rio de Janeiro"))
            .andExpect(jsonPath("$.content[0].area").value("Educação"))
            .andExpect(jsonPath("$.content[0].sigla").value("UFRJ"));
    }

    @Test
    void testGetInstituicaoById() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Polícia Federal");
        instituicao.setArea("Policial");
        instituicao = instituicaoRepository.save(instituicao);

        mockMvc
            .perform(get("/api/v1/instituicoes/{id}", instituicao.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Polícia Federal"))
            .andExpect(jsonPath("$.area").value("Policial"))
            .andExpect(jsonPath("$.questaoStats").exists())
            .andExpect(jsonPath("$.questaoStats.total").exists())
            .andExpect(jsonPath("$.questaoStats.porNivel").exists())
            .andExpect(jsonPath("$.questaoStats.porBanca").exists())
            .andExpect(jsonPath("$.questaoStats.porCargo").exists())
            .andExpect(jsonPath("$.questaoStats.porAreaCargo").exists());
    }

    @Test
    void testGetAllInstituicoes_MetricsTiers() throws Exception {
        Instituicao inst = new Instituicao(); inst.setNome("Inst Stats Test"); inst.setArea("Policial");
        instituicaoRepository.save(inst);

        // Lean (default): no questaoStats
        mockMvc
            .perform(get("/api/v1/instituicoes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats").doesNotExist());

        // Summary: only total
        mockMvc
            .perform(get("/api/v1/instituicoes").param("metrics", "summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats.total").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porNivel").doesNotExist());

        // Full: all breakdowns
        mockMvc
            .perform(get("/api/v1/instituicoes").param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats.total").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porNivel").exists());
    }

    @Test
    void testGetAllInstituicoes_SearchBySigla() throws Exception {
        // Create instituicao with sigla
        Instituicao inst1 = new Instituicao();
        inst1.setNome("Universidade Federal do Rio de Janeiro");
        inst1.setArea("Educação");
        inst1.setSigla("UFRJ");
        instituicaoRepository.save(inst1);

        // Create instituicao without sigla
        Instituicao inst2 = new Instituicao();
        inst2.setNome("Polícia Federal");
        inst2.setArea("Policial");
        instituicaoRepository.save(inst2);

        // Search by sigla should find the first instituicao
        mockMvc
            .perform(get("/api/v1/instituicoes").param("nome", "UFRJ"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].sigla").value("UFRJ"));

        // Search by partial sigla should also work
        mockMvc
            .perform(get("/api/v1/instituicoes").param("nome", "UFR"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].sigla").value("UFRJ"));
    }

    @Test
    void testGetAllInstituicoes_SearchByNomeStillWorks() throws Exception {
        // Create instituicao with sigla
        Instituicao inst1 = new Instituicao();
        inst1.setNome("Universidade Federal do Rio de Janeiro");
        inst1.setArea("Educação");
        inst1.setSigla("UFRJ");
        instituicaoRepository.save(inst1);

        // Create instituicao without sigla
        Instituicao inst2 = new Instituicao();
        inst2.setNome("Polícia Federal");
        inst2.setArea("Policial");
        instituicaoRepository.save(inst2);

        // Search by nome should still work
        mockMvc
            .perform(get("/api/v1/instituicoes").param("nome", "Polícia"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].nome").value("Polícia Federal"));
    }

    @Test
    void testCreateInstituicao() throws Exception {
        InstituicaoCreateRequest request = new InstituicaoCreateRequest();
        request.setNome("PF");
        request.setArea("Policial");

        mockMvc
            .perform(
                post("/api/v1/instituicoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testCreateInstituicao_WithSigla() throws Exception {
        InstituicaoCreateRequest request = new InstituicaoCreateRequest();
        request.setNome("Universidade Federal do Rio de Janeiro");
        request.setArea("Educação");
        request.setSigla("UFRJ");

        mockMvc
            .perform(
                post("/api/v1/instituicoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testUpdateInstituicao() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("OldName");
        instituicao.setArea("OldArea");
        instituicao = instituicaoRepository.save(instituicao);

        com.studora.dto.request.InstituicaoUpdateRequest request = new com.studora.dto.request.InstituicaoUpdateRequest();
        request.setNome("NewName");
        request.setArea("NewArea");

        mockMvc
            .perform(
                put("/api/v1/instituicoes/{id}", instituicao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testUpdateInstituicao_WithSigla() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Universidade de São Paulo");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        com.studora.dto.request.InstituicaoUpdateRequest request = new com.studora.dto.request.InstituicaoUpdateRequest();
        request.setNome("USP");
        request.setArea("Educação");
        request.setSigla("USP");

        mockMvc
            .perform(
                put("/api/v1/instituicoes/{id}", instituicao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testDeleteInstituicao() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("ToDelete");
        instituicao.setArea("ToDeleteArea");
        instituicao = instituicaoRepository.save(instituicao);

        mockMvc
            .perform(delete("/api/v1/instituicoes/{id}", instituicao.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    void testCreateInstituicao_Conflict_DuplicateName() throws Exception {
        // Create first instituicao
        Instituicao inst1 = new Instituicao();
        inst1.setNome("Polícia Federal");
        inst1.setArea("Policial");
        instituicaoRepository.save(inst1);

        // Try to create another instituicao with the same name
        InstituicaoCreateRequest request = new InstituicaoCreateRequest();
        request.setNome("Polícia Federal");
        request.setArea("Judiciária"); // Different area shouldn't matter if name is key

        mockMvc
            .perform(
                post("/api/v1/instituicoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Conflito"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Já existe uma instituição com o nome 'Polícia Federal'"));
    }

    @Test
    void testCreateInstituicao_Conflict_CaseInsensitiveDuplicate() throws Exception {
        // Create first instituicao with uppercase name
        Instituicao inst1 = new Instituicao();
        inst1.setNome("POLICIA FEDERAL");
        inst1.setArea("Policial");
        instituicaoRepository.save(inst1);

        // Try to create another instituicao with the same name in lowercase (should be detected as duplicate)
        InstituicaoCreateRequest request = new InstituicaoCreateRequest();
        request.setNome("policia federal");
        request.setArea("Policial");

        mockMvc
            .perform(
                post("/api/v1/instituicoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Conflito"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Já existe uma instituição com o nome 'policia federal'"));
    }

    @Test
    void testGetAllAreas() throws Exception {
        // Create institutions with different areas
        Instituicao i1 = new Instituicao(); i1.setNome("I1"); i1.setArea("Policial");
        Instituicao i2 = new Instituicao(); i2.setNome("I2"); i2.setArea("Judiciária");
        Instituicao i3 = new Instituicao(); i3.setNome("I3"); i3.setArea("Policial"); // Duplicate area
        
        instituicaoRepository.save(i1);
        instituicaoRepository.save(i2);
        instituicaoRepository.save(i3);

        // Should return unique areas
        mockMvc
            .perform(get("/api/v1/instituicoes/areas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsInAnyOrder("Policial", "Judiciária")));
    }

    @Test
    void testGetAllAreas_WithSearch() throws Exception {
        // Create institutions with different areas
        Instituicao i1 = new Instituicao(); i1.setNome("I1"); i1.setArea("Policial");
        Instituicao i2 = new Instituicao(); i2.setNome("I2"); i2.setArea("Judiciária");
        
        instituicaoRepository.save(i1);
        instituicaoRepository.save(i2);

        // Search for 'jud'
        mockMvc
            .perform(get("/api/v1/instituicoes/areas").param("search", "jud"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0]").value("Judiciária"));
    }
}
