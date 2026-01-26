package com.studora;

import com.studora.dto.DisciplinaDto;
import com.studora.entity.Disciplina;
import com.studora.repository.DisciplinaRepository;
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
class DisciplinaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @BeforeEach
    void setUp() {
        disciplinaRepository.deleteAll();
    }

    @Test
    void testCreateDisciplina() throws Exception {
        DisciplinaDto disciplinaDto = new DisciplinaDto();
        disciplinaDto.setNome("Direito Constitucional");

        mockMvc.perform(post("/api/disciplinas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(disciplinaDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Direito Constitucional"));
    }

    @Test
    void testGetDisciplinaById() throws Exception {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("Direito Constitucional");
        disciplina = disciplinaRepository.save(disciplina);

        mockMvc.perform(get("/api/disciplinas/{id}", disciplina.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Direito Constitucional"));
    }

    @Test
    void testGetDisciplinaById_NotFound() throws Exception {
        mockMvc.perform(get("/api/disciplinas/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllDisciplinas() throws Exception {
        disciplinaRepository.save(new Disciplina("Direito Constitucional"));
        disciplinaRepository.save(new Disciplina("Direito Administrativo"));

        mockMvc.perform(get("/api/disciplinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void testUpdateDisciplina() throws Exception {
        Disciplina disciplina = new Disciplina("Old Name");
        disciplina = disciplinaRepository.save(disciplina);

        DisciplinaDto updatedDto = new DisciplinaDto();
        updatedDto.setNome("New Name");

        mockMvc.perform(put("/api/disciplinas/{id}", disciplina.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("New Name"));
    }

    @Test
    void testDeleteDisciplina() throws Exception {
        Disciplina disciplina = new Disciplina("Disciplina to Delete");
        disciplina = disciplinaRepository.save(disciplina);

        mockMvc.perform(delete("/api/disciplinas/{id}", disciplina.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/disciplinas/{id}", disciplina.getId()))
                .andExpect(status().isNotFound());
    }
}
