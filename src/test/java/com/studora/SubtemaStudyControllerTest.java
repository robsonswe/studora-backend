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
    private Tema tema;
    private Disciplina disciplina;

    @BeforeEach
    void setUp() {
        estudoSubtemaRepository.deleteAll();
        subtemaRepository.deleteAll();
        temaRepository.deleteAll();
        disciplinaRepository.deleteAll();

        disciplina = new Disciplina();
        disciplina.setNome("Direito Estudo");
        disciplina = disciplinaRepository.save(disciplina);

        tema = new Tema();
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
            .perform(get("/api/v1/subtemas/{id}", subtema.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalEstudos").value(0));

        // Add a study session
        mockMvc
            .perform(post("/api/v1/subtemas/{id}/estudos", subtema.getId()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.message").exists());

        // Add another study session
        mockMvc
            .perform(post("/api/v1/subtemas/{id}/estudos", subtema.getId()))
            .andExpect(status().isCreated());

        // Check total count and details
        mockMvc
            .perform(get("/api/v1/subtemas/{id}", subtema.getId()).param("metrics", "full"))
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
            .perform(get("/api/v1/subtemas/{id}", subtema.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalEstudos").value(0));
    }

    @Test
    void testStudySessionUpdatesTemaStats() throws Exception {
        // Before study: tema has 1 subtema
        mockMvc
            .perform(get("/api/v1/temas/{id}", tema.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subtemas.length()").value(1));

        // Add a study session
        mockMvc
            .perform(post("/api/v1/subtemas/{id}/estudos", subtema.getId()))
            .andExpect(status().isCreated());

        // After study: tema's subtema should reflect the new study
        mockMvc
            .perform(get("/api/v1/temas/{id}", tema.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subtemas[0].totalEstudos").isNotEmpty())
            .andExpect(jsonPath("$.subtemas[0].ultimoEstudo").exists());
    }

    @Test
    void testStudySessionUpdatesDisciplinaStats() throws Exception {
        Long disciplinaId = disciplina.getId();

        // Before study: disciplina completo has questaoStats
        mockMvc
            .perform(get("/api/v1/disciplinas/{disciplinaId}/completo", disciplinaId).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.questaoStats").exists())
            .andExpect(jsonPath("$.temas[0].id").value(tema.getId()));

        // Add a study session
        mockMvc
            .perform(post("/api/v1/subtemas/{id}/estudos", subtema.getId()))
            .andExpect(status().isCreated());

        // After study: disciplina completo still returns structure with temas and subtemas
        mockMvc
            .perform(get("/api/v1/disciplinas/{disciplinaId}/completo", disciplinaId).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.temas[0].id").value(tema.getId()))
            .andExpect(jsonPath("$.temas[0].subtemas[0].id").value(subtema.getId()))
            .andExpect(jsonPath("$.questaoStats").exists());
    }

    @Test
    void testSubtemaDetailHasEnrichedNestedTema() throws Exception {
        // Add a study session
        mockMvc
            .perform(post("/api/v1/subtemas/{id}/estudos", subtema.getId()))
            .andExpect(status().isCreated());

        // Check that subtema detail has study metrics and nested tema reference
        mockMvc
            .perform(get("/api/v1/subtemas/{id}", subtema.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalEstudos").value(1))
            .andExpect(jsonPath("$.ultimoEstudo").exists())
            .andExpect(jsonPath("$.tema.id").value(tema.getId()))
            .andExpect(jsonPath("$.tema.nome").value(tema.getNome()));
    }

    @Test
    void testTemaDetailHasEnrichedNestedDisciplina() throws Exception {
        // Add a study session
        mockMvc
            .perform(post("/api/v1/subtemas/{id}/estudos", subtema.getId()))
            .andExpect(status().isCreated());

        // Check that tema detail has questaoStats and nested disciplina reference
        mockMvc
            .perform(get("/api/v1/temas/{id}", tema.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.questaoStats").exists())
            .andExpect(jsonPath("$.disciplina.id").value(disciplina.getId()))
            .andExpect(jsonPath("$.disciplina.nome").value(disciplina.getNome()))
            .andExpect(jsonPath("$.subtemas[0].totalEstudos").value(1))
            .andExpect(jsonPath("$.subtemas[0].ultimoEstudo").exists());
    }
}
