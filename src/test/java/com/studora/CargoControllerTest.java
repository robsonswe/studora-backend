package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.CargoDto;
import com.studora.dto.request.CargoCreateRequest;
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
        CargoCreateRequest request = new CargoCreateRequest();
        request.setNome("Analista");
        request.setNivel("Superior");
        request.setArea("TI");

        mockMvc
            .perform(
                post("/api/cargos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Analista"));

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
            .andExpect(status().isNoContent());
    }

    @Test
    void testCreateCargo_Conflict_DuplicateName() throws Exception {
        // Create first cargo
        Cargo cargo1 = new Cargo();
        cargo1.setNome("Analista");
        cargo1.setNivel("Superior");
        cargo1.setArea("TI");
        cargoRepository.save(cargo1);

        // Try to create another cargo with the same name, nivel, and area
        CargoCreateRequest request = new CargoCreateRequest();
        request.setNome("Analista");
        request.setNivel("Superior");
        request.setArea("TI");

        mockMvc
            .perform(
                post("/api/cargos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Conflito"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Já existe um cargo com o nome 'Analista', nível 'Superior' e área 'TI'"));
    }
}