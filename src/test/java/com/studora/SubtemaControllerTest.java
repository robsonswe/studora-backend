package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.SubtemaDto;
import com.studora.dto.request.SubtemaCreateRequest;
import com.studora.dto.request.SubtemaUpdateRequest;
import com.studora.entity.Disciplina;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
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
class SubtemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubtemaRepository subtemaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    private Tema tema;

    @BeforeEach
    void setUp() {
        Disciplina disciplina = disciplinaRepository.save(
            new Disciplina("Direito Subtema Test")
        );
        Tema newTema = new Tema();
        newTema.setNome("Tema Subtema Test");
        newTema.setDisciplina(disciplina);
        tema = temaRepository.save(newTema);
    }

    @Test
    void testCreateSubtema() throws Exception {
        SubtemaCreateRequest subtemaCreateRequest = new SubtemaCreateRequest();
        subtemaCreateRequest.setNome("Espécies de Controle");
        subtemaCreateRequest.setTemaId(tema.getId());

        mockMvc
            .perform(
                post("/api/subtemas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(subtemaCreateRequest))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Espécies de Controle"));
    }

    @Test
    void testGetSubtemaById() throws Exception {
        Subtema subtema = new Subtema();
        subtema.setNome("Espécies de Controle");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        mockMvc
            .perform(get("/api/subtemas/{id}", subtema.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Espécies de Controle"));
    }

    @Test
    void testGetSubtemaById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/subtemas/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllSubtemas() throws Exception {
        Subtema subtema1 = new Subtema();
        subtema1.setNome("Subtema 1");
        subtema1.setTema(tema);
        subtemaRepository.save(subtema1);

        Subtema subtema2 = new Subtema();
        subtema2.setNome("Subtema 2");
        subtema2.setTema(tema);
        subtemaRepository.save(subtema2);

        mockMvc
            .perform(get("/api/subtemas"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                )
            );
    }

    @Test
    void testUpdateSubtema() throws Exception {
        Subtema subtema = new Subtema();
        subtema.setNome("Old Name");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        SubtemaUpdateRequest updateRequest = new SubtemaUpdateRequest();
        updateRequest.setNome("New Name");
        updateRequest.setTemaId(tema.getId());

        mockMvc
            .perform(
                put("/api/subtemas/{id}", subtema.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(updateRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("New Name"));
    }

    @Test
    void testDeleteSubtema() throws Exception {
        Subtema subtema = new Subtema();
        subtema.setNome("Subtema to Delete");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        mockMvc
            .perform(delete("/api/subtemas/{id}", subtema.getId()))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/api/subtemas/{id}", subtema.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreateSubtema_Conflict_DuplicateName_CaseInsensitive() throws Exception {
        // Create first subtema
        Subtema sub1 = new Subtema();
        sub1.setNome("Controle Prévio");
        sub1.setTema(tema);
        subtemaRepository.save(sub1);

        // Try to create another subtema with the same name but different case
        SubtemaCreateRequest request = new SubtemaCreateRequest();
        request.setNome("controle prévio");
        request.setTemaId(tema.getId());

        mockMvc
            .perform(
                post("/api/subtemas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Já existe um subtema com o nome 'controle prévio' no tema com ID: " + tema.getId()));
    }
}