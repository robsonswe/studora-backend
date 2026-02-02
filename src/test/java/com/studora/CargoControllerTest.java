package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.CargoDto;
import com.studora.dto.request.CargoCreateRequest;
import com.studora.entity.Cargo;
import com.studora.entity.NivelCargo;
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
        request.setNivel(NivelCargo.SUPERIOR);
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
            .andExpect(jsonPath("$.content[0].nome").exists());
    }

    @Test
    void testGetById() throws Exception {
        Cargo cargo = new Cargo();
        cargo.setNome("Tecnico");
        cargo.setNivel(NivelCargo.MEDIO);
        cargo.setArea("TI");
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
        cargo.setNivel(NivelCargo.FUNDAMENTAL);
        cargo.setArea("TI");
        cargo = cargoRepository.save(cargo);

        mockMvc
            .perform(delete("/api/cargos/{id}", cargo.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    void testGetAllCargos_DefaultSorting() throws Exception {
        // Create cargos with same name but different area
        Cargo c1 = new Cargo(); c1.setNome("Analista"); c1.setArea("TI"); c1.setNivel(NivelCargo.SUPERIOR);
        Cargo c2 = new Cargo(); c2.setNome("Analista"); c2.setArea("Administrativa"); c2.setNivel(NivelCargo.SUPERIOR);
        Cargo c3 = new Cargo(); c3.setNome("Tecnico"); c3.setArea("Judiciaria"); c3.setNivel(NivelCargo.MEDIO);
        
        cargoRepository.save(c2);
        cargoRepository.save(c1);
        cargoRepository.save(c3);

        // Default sort should be nome ASC, then area ASC
        // Expected order: Analista Administrativa, Analista TI, Tecnico Judiciaria
        mockMvc
            .perform(get("/api/cargos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].area").value("Administrativa"))
            .andExpect(jsonPath("$.content[1].area").value("TI"))
            .andExpect(jsonPath("$.content[2].nome").value("Tecnico"));
    }

    @Test
    void testGetAllCargos_CustomSortingByArea() throws Exception {
        // Create cargos with different areas
        Cargo c1 = new Cargo(); c1.setNome("Z-Cargo"); c1.setArea("TI"); c1.setNivel(NivelCargo.SUPERIOR);
        Cargo c2 = new Cargo(); c2.setNome("A-Cargo"); c2.setArea("Administrativa"); c2.setNivel(NivelCargo.SUPERIOR);
        
        cargoRepository.save(c1);
        cargoRepository.save(c2);

        // Sort by area DESC
        // Expected: TI, then Administrativa
        mockMvc
            .perform(get("/api/cargos").param("sort", "area").param("direction", "DESC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].area").value("TI"))
            .andExpect(jsonPath("$.content[1].area").value("Administrativa"));
    }

    @Test
    void testCreateCargo_Conflict_DuplicateName() throws Exception {
        // Create first cargo
        Cargo cargo1 = new Cargo();
        cargo1.setNome("Analista");
        cargo1.setNivel(NivelCargo.SUPERIOR);
        cargo1.setArea("TI");
        cargoRepository.save(cargo1);

        // Try to create another cargo with the same name, nivel, and area
        CargoCreateRequest request = new CargoCreateRequest();
        request.setNome("Analista");
        request.setNivel(NivelCargo.SUPERIOR);
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
            .andExpect(jsonPath("$.detail").value("Já existe um cargo com o nome 'Analista', nível 'SUPERIOR' e área 'TI'"));
    }

    @Test
    void testCreateCargo_Conflict_CaseInsensitiveDuplicate() throws Exception {
        // Create first cargo with uppercase values
        Cargo cargo1 = new Cargo();
        cargo1.setNome("ANALISTA DE SISTEMAS");
        cargo1.setNivel(NivelCargo.SUPERIOR);
        cargo1.setArea("TECNOLOGIA DA INFORMACAO");
        cargoRepository.save(cargo1);

        // Try to create another cargo with the same values in lowercase (should be detected as duplicate)
        CargoCreateRequest request = new CargoCreateRequest();
        request.setNome("analista de sistemas");
        request.setNivel(NivelCargo.SUPERIOR);
        request.setArea("tecnologia da informacao");

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
            .andExpect(jsonPath("$.detail").value("Já existe um cargo com o nome 'analista de sistemas', nível 'SUPERIOR' e área 'tecnologia da informacao'"));
    }

    @Test
    void testGetAllAreas() throws Exception {
        // Create cargos with different areas
        Cargo c1 = new Cargo(); c1.setNome("C1"); c1.setArea("TI"); c1.setNivel(NivelCargo.SUPERIOR);
        Cargo c2 = new Cargo(); c2.setNome("C2"); c2.setArea("Administrativa"); c2.setNivel(NivelCargo.SUPERIOR);
        Cargo c3 = new Cargo(); c3.setNome("C3"); c3.setArea("TI"); c3.setNivel(NivelCargo.SUPERIOR); // Duplicate area
        
        cargoRepository.save(c1);
        cargoRepository.save(c2);
        cargoRepository.save(c3);

        // Should return unique areas
        mockMvc
            .perform(get("/api/cargos/areas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsInAnyOrder("TI", "Administrativa")));
    }

    @Test
    void testGetAllAreas_WithSearch() throws Exception {
        // Create cargos with different areas
        Cargo c1 = new Cargo(); c1.setNome("C1"); c1.setArea("Tecnologia"); c1.setNivel(NivelCargo.SUPERIOR);
        Cargo c2 = new Cargo(); c2.setNome("C2"); c2.setArea("Administrativa"); c2.setNivel(NivelCargo.SUPERIOR);
        
        cargoRepository.save(c1);
        cargoRepository.save(c2);

        // Search for 'tec'
        mockMvc
            .perform(get("/api/cargos/areas").param("search", "tec"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0]").value("Tecnologia"));
    }
}