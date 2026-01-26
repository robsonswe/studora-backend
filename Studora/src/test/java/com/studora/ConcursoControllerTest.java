package com.studora;

import com.studora.dto.ConcursoDto;
import com.studora.entity.Concurso;
import com.studora.repository.ConcursoRepository;
import com.studora.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConcursoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConcursoRepository concursoRepository;

    @BeforeEach
    void setUp() {
        concursoRepository.deleteAll();
    }

    @Test
    void testCreateConcurso() throws Exception {
        ConcursoDto concursoDto = new ConcursoDto("Concurso 1", "Banca 1", 2023, "Cargo 1", "Nível 1", "Área 1");

        mockMvc.perform(post("/api/concursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(concursoDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Concurso 1"));
    }

    @Test
    void testGetConcursoById() throws Exception {
        Concurso concurso = new Concurso("Concurso 1", "Banca 1", 2023, "Cargo 1", "Nível 1", "Área 1");
        concurso = concursoRepository.save(concurso);

        mockMvc.perform(get("/api/concursos/{id}", concurso.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Concurso 1"));
    }

    @Test
    void testGetConcursoById_NotFound() throws Exception {
        mockMvc.perform(get("/api/concursos/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllConcursos() throws Exception {
        concursoRepository.save(new Concurso("Concurso 1", "Banca 1", 2023, "Cargo 1", "Nível 1", "Área 1"));
        concursoRepository.save(new Concurso("Concurso 2", "Banca 2", 2024, "Cargo 2", "Nível 2", "Área 2"));

        mockMvc.perform(get("/api/concursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void testUpdateConcurso() throws Exception {
        Concurso concurso = new Concurso("Old Name", "Old Banca", 2022, "Old Cargo", "Old Nivel", "Old Area");
        concurso = concursoRepository.save(concurso);

        ConcursoDto updatedDto = new ConcursoDto("New Name", "New Banca", 2023, "New Cargo", "New Nivel", "New Area");

        mockMvc.perform(put("/api/concursos/{id}", concurso.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("New Name"));
    }

    @Test
    void testDeleteConcurso() throws Exception {
        Concurso concurso = new Concurso("Concurso to Delete", "Banca", 2023, "Cargo", "Nivel", "Area");
        concurso = concursoRepository.save(concurso);

        mockMvc.perform(delete("/api/concursos/{id}", concurso.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/concursos/{id}", concurso.getId()))
                .andExpect(status().isNotFound());
    }
}
