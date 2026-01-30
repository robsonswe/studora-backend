package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DisciplinaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @BeforeEach
    void setUp() {
        // No deleteAll needed with @Transactional
    }

    @Test
    void testCreateDisciplina() throws Exception {
        DisciplinaDto disciplinaDto = new DisciplinaDto();
        disciplinaDto.setNome("Direito Test");

        mockMvc
            .perform(
                post("/api/disciplinas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(disciplinaDto))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Direito Test"));
    }

    @Test
    void testGetDisciplinaById() throws Exception {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("Direito Get Test");
        disciplina = disciplinaRepository.save(disciplina);

        mockMvc
            .perform(get("/api/disciplinas/{id}", disciplina.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Direito Get Test"));
    }

    @Test
    void testGetDisciplinaById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/disciplinas/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllDisciplinas() throws Exception {
        disciplinaRepository.save(new Disciplina("Direito All 1"));
        disciplinaRepository.save(new Disciplina("Direito All 2"));

        mockMvc
            .perform(get("/api/disciplinas"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                )
            );
    }

    @Test
    void testUpdateDisciplina() throws Exception {
        Disciplina disciplina = new Disciplina("Old Name");
        disciplina = disciplinaRepository.save(disciplina);

        DisciplinaDto updatedDto = new DisciplinaDto();
        updatedDto.setNome("New Name");

        mockMvc
            .perform(
                put("/api/disciplinas/{id}", disciplina.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(updatedDto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("New Name"));
    }

    @Test
    void testDeleteDisciplina() throws Exception {
        Disciplina disciplina = new Disciplina("Disciplina to Delete");
        disciplina = disciplinaRepository.save(disciplina);

        mockMvc
            .perform(delete("/api/disciplinas/{id}", disciplina.getId()))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/api/disciplinas/{id}", disciplina.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreateDisciplina_Conflict_DuplicateName() throws Exception {
        // Create first disciplina
        Disciplina disciplina1 = new Disciplina("Direito Administrativo");
        disciplinaRepository.save(disciplina1);

        // Try to create another disciplina with the same name
        DisciplinaDto disciplinaDto = new DisciplinaDto();
        disciplinaDto.setNome("Direito Administrativo");

        mockMvc
            .perform(
                post("/api/disciplinas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(disciplinaDto))
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Conflito"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Já existe uma disciplina com o nome 'Direito Administrativo'"));
    }
}