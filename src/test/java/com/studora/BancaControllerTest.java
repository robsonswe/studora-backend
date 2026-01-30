package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.BancaDto;
import com.studora.entity.Banca;
import com.studora.repository.BancaRepository;
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
class BancaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BancaRepository bancaRepository;

    @Test
    void testGetAllBancas() throws Exception {
        Banca banca = new Banca();
        banca.setNome("Vunesp");
        bancaRepository.save(banca);

        mockMvc
            .perform(get("/api/bancas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("Vunesp"));
    }

    @Test
    void testGetBancaById() throws Exception {
        Banca banca = new Banca();
        banca.setNome("Cespe");
        banca = bancaRepository.save(banca);

        mockMvc
            .perform(get("/api/bancas/{id}", banca.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Cespe"));
    }

    @Test
    void testCreateBanca() throws Exception {
        BancaDto dto = new BancaDto();
        dto.setNome("FGV");

        mockMvc
            .perform(
                post("/api/bancas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(dto))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("FGV"));
    }

    @Test
    void testUpdateBanca() throws Exception {
        Banca banca = new Banca();
        banca.setNome("OldName");
        banca = bancaRepository.save(banca);

        BancaDto dto = new BancaDto();
        dto.setNome("NewName");

        mockMvc
            .perform(
                put("/api/bancas/{id}", banca.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(dto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("NewName"));
    }

    @Test
    void testDeleteBanca() throws Exception {
        Banca banca = new Banca();
        banca.setNome("ToDelete");
        banca = bancaRepository.save(banca);

        mockMvc
            .perform(delete("/api/bancas/{id}", banca.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    void testCreateBanca_Conflict_DuplicateName() throws Exception {
        // Create first banca
        Banca banca1 = new Banca();
        banca1.setNome("CESPE");
        bancaRepository.save(banca1);

        // Try to create another banca with the same name
        BancaDto dto = new BancaDto();
        dto.setNome("CESPE");

        mockMvc
            .perform(
                post("/api/bancas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(dto))
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Conflito"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Já existe uma banca com o nome 'CESPE'"));
    }
}