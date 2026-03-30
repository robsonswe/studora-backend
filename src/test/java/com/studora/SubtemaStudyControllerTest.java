package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.entity.Disciplina;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.repository.EstudoSubtemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubtemaStudyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private SubtemaRepository subtemaRepository;

    @Autowired
    private EstudoSubtemaRepository estudoSubtemaRepository;

    private Subtema subtema;

    @BeforeEach
    void setUp() {
        estudoSubtemaRepository.deleteAll();
        subtemaRepository.deleteAll();
        temaRepository.deleteAll();
        disciplinaRepository.deleteAll();

        Disciplina disciplina = new Disciplina();
        disciplina.setNome("Direito Estudo");
        disciplina = disciplinaRepository.save(disciplina);

        Tema tema = new Tema();
        tema.setNome("Tema Estudo");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        subtema = new Subtema();
        subtema.setNome("Subtema Estudo");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);
    }

    @Test
    void testAddAndGetStudySessions() throws Exception {
        // Initially no study sessions
        mockMvc
            .perform(get("/api/v1/subtemas/{id}", subtema.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalEstudos").value(0));

        // Add a study session
        mockMvc
            .perform(post("/api/v1/subtemas/{id}/estudos", subtema.getId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.subtemaId").value(subtema.getId()));

        // Add another study session
        mockMvc
            .perform(post("/api/v1/subtemas/{id}/estudos", subtema.getId()))
            .andExpect(status().isCreated());

        // Check total count and details
        mockMvc
            .perform(get("/api/v1/subtemas/{id}", subtema.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalEstudos").value(2))
            .andExpect(jsonPath("$.ultimoEstudo").exists());

        // List sessions
        mockMvc
            .perform(get("/api/v1/subtemas/{id}/estudos", subtema.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testDeleteStudySession() throws Exception {
        // Add a study session
        String response = mockMvc
            .perform(post("/api/v1/subtemas/{id}/estudos", subtema.getId()))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        
        Long estudoId = com.jayway.jsonpath.JsonPath.parse(response).read("$.id", Long.class);

        // Delete it
        mockMvc
            .perform(delete("/api/v1/subtemas/{subtemaId}/estudos/{estudoId}", subtema.getId(), estudoId))
            .andExpect(status().isNoContent());

        // Check total count is 0 again
        mockMvc
            .perform(get("/api/v1/subtemas/{id}", subtema.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalEstudos").value(0));
    }
}
