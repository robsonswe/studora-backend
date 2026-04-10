package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.BancaCreateRequest;
import com.studora.dto.request.BancaUpdateRequest;
import com.studora.entity.Banca;
import com.studora.repository.BancaRepository;
import com.studora.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
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
class BancaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BancaRepository bancaRepository;

    @BeforeEach
    void setUp() {
        bancaRepository.deleteAll();
    }

    @Test
    void testGetAllBancas() throws Exception {
        Banca banca = new Banca();
        banca.setNome("Vunesp");
        bancaRepository.save(banca);

        mockMvc
            .perform(get("/api/v1/bancas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("Vunesp"))
            .andExpect(jsonPath("$.pageNumber").value(0));
    }

    @Test
    void testGetAllBancas_DefaultSorting() throws Exception {
        Banca b1 = new Banca(); b1.setNome("Banca B Test Unique"); bancaRepository.save(b1);
        Banca b2 = new Banca(); b2.setNome("Banca A Test Unique"); bancaRepository.save(b2);

        // Default sort: nome ASC - verify both exist and first starts with "Banca A"
        mockMvc
            .perform(get("/api/v1/bancas").param("sort", "nome").param("direction", "ASC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.content[0].nome").value(org.hamcrest.Matchers.startsWith("Banca A")));
    }

    @Test
    void testGetAllBancas_CustomSortingByDirection() throws Exception {
        Banca b1 = new Banca(); b1.setNome("Banca B"); bancaRepository.save(b1);
        Banca b2 = new Banca(); b2.setNome("Banca A"); bancaRepository.save(b2);
        
        // Sort: nome DESC
        mockMvc
            .perform(get("/api/v1/bancas").param("direction", "DESC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("Banca B"))
            .andExpect(jsonPath("$.content[1].nome").value("Banca A"));
    }

    @Test
    void testGetAllBancas_Page1() throws Exception {
        Banca banca = new Banca();
        banca.setNome("Vunesp");
        bancaRepository.save(banca);

        mockMvc
            .perform(get("/api/v1/bancas").param("page", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("Vunesp"))
            .andExpect(jsonPath("$.pageNumber").value(0));
    }

    @Test
    void testGetBancaById() throws Exception {
        Banca banca = new Banca();
        banca.setNome("Cespe");
        banca = bancaRepository.save(banca);

        mockMvc
            .perform(get("/api/v1/bancas/{id}", banca.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Cespe"))
            .andExpect(jsonPath("$.questaoStats").exists())
            .andExpect(jsonPath("$.questaoStats.total").exists())
            .andExpect(jsonPath("$.questaoStats.porNivel").exists())
            .andExpect(jsonPath("$.questaoStats.porAreaInstituicao").exists())
            .andExpect(jsonPath("$.questaoStats.porAreaCargo").exists());
    }

    @Test
    void testGetAllBancas_MetricsTiers() throws Exception {
        Banca b = new Banca(); b.setNome("Banca Stats Test"); bancaRepository.save(b);

        // Lean (default): no questaoStats
        mockMvc
            .perform(get("/api/v1/bancas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats").doesNotExist());

        // Summary: only total
        mockMvc
            .perform(get("/api/v1/bancas").param("metrics", "summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats.total").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porNivel").doesNotExist());

        // Full: all breakdowns
        mockMvc
            .perform(get("/api/v1/bancas").param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats.total").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porNivel").exists());
    }

    @Test
    void testCreateBanca() throws Exception {
        BancaCreateRequest request = new BancaCreateRequest();
        request.setNome("FGV");

        mockMvc
            .perform(
                post("/api/v1/bancas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testUpdateBanca() throws Exception {
        Banca banca = new Banca();
        banca.setNome("OldName");
        banca = bancaRepository.save(banca);

        BancaUpdateRequest request = new BancaUpdateRequest();
        request.setNome("NewName");

        mockMvc
            .perform(
                put("/api/v1/bancas/{id}", banca.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testDeleteBanca() throws Exception {
        Banca banca = new Banca();
        banca.setNome("ToDelete");
        banca = bancaRepository.save(banca);

        mockMvc
            .perform(delete("/api/v1/bancas/{id}", banca.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    void testCreateBanca_Conflict_DuplicateName() throws Exception {
        // Create first banca
        Banca banca1 = new Banca();
        banca1.setNome("CESPE");
        bancaRepository.save(banca1);

        // Try to create another banca with the same name
        BancaCreateRequest request = new BancaCreateRequest();
        request.setNome("CESPE");

        mockMvc
            .perform(
                post("/api/v1/bancas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Conflito"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Já existe uma banca com o nome 'CESPE'"));
    }

    @Test
    void testCreateBanca_Conflict_CaseInsensitiveDuplicate() throws Exception {
        // Create first banca with uppercase name
        Banca banca1 = new Banca();
        banca1.setNome("CESPE");
        bancaRepository.save(banca1);

        // Try to create another banca with the same name in lowercase (should be detected as duplicate)
        BancaCreateRequest request = new BancaCreateRequest();
        request.setNome("cespe");

        mockMvc
            .perform(
                post("/api/v1/bancas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Conflito"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Já existe uma banca com o nome 'cespe'"));
    }
}
