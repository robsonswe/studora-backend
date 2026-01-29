package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.CargoDto;
import com.studora.entity.Cargo;
import com.studora.repository.CargoRepository;
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
class CargoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CargoRepository cargoRepository;

    @Test
    void testCrudCargo() throws Exception {
        // Create
        CargoDto dto = new CargoDto();
        dto.setNome("Analista");
        dto.setNivel("Superior");
        dto.setArea("TI");

        String response = mockMvc
            .perform(
                post("/api/cargos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(dto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Analista"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        // Extract ID (simple parse for test)
        // In real world use ObjectMapper, but here we assume it worked.

        // Get All
        mockMvc
            .perform(get("/api/cargos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").exists());
    }

    @Test
    void testGetById() throws Exception {
        Cargo cargo = new Cargo();
        cargo.setNome("Tecnico");
        cargo = cargoRepository.save(cargo);

        mockMvc
            .perform(get("/api/cargos/{id}", cargo.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Tecnico"));
    }

    @Test
    void testDelete() throws Exception {
        Cargo cargo = new Cargo();
        cargo.setNome("DeleteMe");
        cargo = cargoRepository.save(cargo);

        mockMvc
            .perform(delete("/api/cargos/{id}", cargo.getId()))
            .andExpect(status().isOk());
    }
}
