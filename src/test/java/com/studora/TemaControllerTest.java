package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.TemaCreateRequest;
import com.studora.dto.request.TemaUpdateRequest;
import com.studora.entity.Disciplina;
import com.studora.entity.Tema;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.TemaRepository;
import com.studora.util.TestUtil;
import com.studora.entity.Subtema;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.EstudoSubtemaRepository;
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
class TemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private SubtemaRepository subtemaRepository;

    @Autowired
    private EstudoSubtemaRepository estudoSubtemaRepository;

    private Disciplina disciplina;

    @BeforeEach
    void setUp() {
        disciplina = disciplinaRepository.save(
            new Disciplina("Direito Tema Test")
        );
    }

    @Test
    void testCreateTema() throws Exception {
        TemaCreateRequest temaCreateRequest = new TemaCreateRequest();
        temaCreateRequest.setNome("Controle de Constitucionalidade");
        temaCreateRequest.setDisciplinaId(disciplina.getId());

        mockMvc
            .perform(
                post("/api/v1/temas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(temaCreateRequest))
            )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.nome").value("Controle de Constitucionalidade")
            );
    }

    @Test
    void testGetTemaById() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Controle de Constitucionalidade");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        mockMvc
            .perform(get("/api/v1/temas/{id}", tema.getId()))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.nome").value("Controle de Constitucionalidade")
            )
            .andExpect(jsonPath("$.totalEstudos").value(0))
            .andExpect(jsonPath("$.totalSubtemas").value(0))
            .andExpect(jsonPath("$.subtemasEstudados").value(0))
            .andExpect(jsonPath("$.subtemas").isArray());
    }

    @Test
    void testGetTemaById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/v1/temas/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllTemas() throws Exception {
        Tema tema1 = new Tema();
        tema1.setNome("Tema 1");
        tema1.setDisciplina(disciplina);
        temaRepository.save(tema1);

        Tema tema2 = new Tema();
        tema2.setNome("Tema 2");
        tema2.setDisciplina(disciplina);
        temaRepository.save(tema2);

        mockMvc
            .perform(get("/api/v1/temas"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                )
            )
            .andExpect(jsonPath("$.content[0].totalEstudos").exists())
            .andExpect(jsonPath("$.content[0].totalSubtemas").exists())
            .andExpect(jsonPath("$.content[0].subtemasEstudados").exists());
    }

    @Test
    void testGetTemasByDisciplina() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Tema de Disciplina");
        tema.setDisciplina(disciplina);
        temaRepository.save(tema);

        mockMvc
            .perform(get("/api/v1/temas/disciplina/{disciplinaId}", disciplina.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$[0].nome").value("Tema de Disciplina"))
            .andExpect(jsonPath("$[0].totalEstudos").exists())
            .andExpect(jsonPath("$[0].totalSubtemas").exists())
            .andExpect(jsonPath("$[0].subtemasEstudados").exists());
    }

    @Test
    void testGetAllTemas_DefaultSorting() throws Exception {
        // Create another disciplina to test disciplinaId sorting
        Disciplina disc2 = new Disciplina();
        disc2.setNome("Disciplina 2");
        disc2 = disciplinaRepository.save(disc2);

        Tema t1 = new Tema(); t1.setNome("B-Tema"); t1.setDisciplina(disciplina); temaRepository.save(t1);
        Tema t2 = new Tema(); t2.setNome("A-Tema"); t2.setDisciplina(disc2); temaRepository.save(t2);
        Tema t3 = new Tema(); t3.setNome("A-Tema"); t3.setDisciplina(disciplina); temaRepository.save(t3);

        // Default sort: nome ASC, disciplina.id ASC
        // Expected: 1. A-Tema (disciplina), 2. A-Tema (disc2), 3. B-Tema (disciplina)
        mockMvc
            .perform(get("/api/v1/temas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("A-Tema"))
            .andExpect(jsonPath("$.content[0].disciplinaId").value(disciplina.getId()))
            .andExpect(jsonPath("$.content[1].disciplinaId").value(disc2.getId()))
            .andExpect(jsonPath("$.content[2].nome").value("B-Tema"));
    }

    @Test
    void testUpdateTema() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Old Name");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        TemaUpdateRequest updateRequest = new TemaUpdateRequest();
        updateRequest.setNome("New Name");
        updateRequest.setDisciplinaId(disciplina.getId());

        mockMvc
            .perform(
                put("/api/v1/temas/{id}", tema.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(updateRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("New Name"));
    }

    @Test
    void testDeleteTema() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Tema to Delete");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        mockMvc
            .perform(delete("/api/v1/temas/{id}", tema.getId()))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/api/v1/temas/{id}", tema.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreateTema_Conflict_DuplicateName_CaseInsensitive() throws Exception {
        // Create first tema
        Tema tema1 = new Tema();
        tema1.setNome("Atos Administrativos");
        tema1.setDisciplina(disciplina);
        temaRepository.save(tema1);

        // Try to create another tema with the same name but different case
        TemaCreateRequest request = new TemaCreateRequest();
        request.setNome("atos administrativos");
        request.setDisciplinaId(disciplina.getId());

        mockMvc
            .perform(
                post("/api/v1/temas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Já existe um tema com o nome 'atos administrativos' na disciplina com ID: " + disciplina.getId()));
    }

    @Test
    void testTemaDetailHasEnrichedNestedDisciplina() throws Exception {
        // Create a tema with a subtema and a study session
        Tema tema = new Tema();
        tema.setNome("Tema Enriched Test");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        Subtema subtema = new Subtema();
        subtema.setNome("Subtema Enriched");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        // Add a study session
        com.studora.entity.EstudoSubtema estudo = new com.studora.entity.EstudoSubtema(subtema);
        estudoSubtemaRepository.save(estudo);

        // Check that nested disciplina has enriched stats
        mockMvc
            .perform(get("/api/v1/temas/{id}", tema.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalEstudos").value(1))
            .andExpect(jsonPath("$.disciplina.id").value(disciplina.getId()))
            .andExpect(jsonPath("$.disciplina.nome").value(disciplina.getNome()))
            .andExpect(jsonPath("$.disciplina.totalEstudos").value(1))
            .andExpect(jsonPath("$.disciplina.totalTemas").value(1))
            .andExpect(jsonPath("$.disciplina.totalSubtemas").value(1))
            .andExpect(jsonPath("$.disciplina.subtemasEstudados").value(1))
            .andExpect(jsonPath("$.disciplina.temasEstudados").value(1));
    }
}
