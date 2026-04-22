package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @BeforeEach
    void setUp() {
        subtemaRepository.deleteAll();
        temaRepository.deleteAll();
        disciplinaRepository.deleteAll();
    }

    @Test
    void testGetSubtemaById_FullMetrics() throws Exception {
        Disciplina d = disciplinaRepository.save(new Disciplina("D1"));
        Tema t = temaRepository.save(new Tema(d, "T1"));
        Subtema s = subtemaRepository.save(new Subtema(t, "S1"));

        mockMvc
            .perform(get("/api/v1/subtemas/{id}", s.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("S1"))
            .andExpect(jsonPath("$.questaoStats").exists())
            .andExpect(jsonPath("$.questaoStats.total").exists())
            // Nested objects
            .andExpect(jsonPath("$.tema.id").value(t.getId()))
            .andExpect(jsonPath("$.tema.nome").value("T1"))
            .andExpect(jsonPath("$.disciplina.id").value(d.getId()))
            .andExpect(jsonPath("$.disciplina.nome").value("D1"))
            // Leanerization: Nested objects must not have stats
            .andExpect(jsonPath("$.tema.questaoStats").doesNotExist());
    }

    @Test
    void testGetAllSubtemas_MetricsTiers() throws Exception {
        Disciplina d = disciplinaRepository.save(new Disciplina("D1"));
        Tema t = temaRepository.save(new Tema(d, "T1"));
        subtemaRepository.save(new Subtema(t, "S1"));

        // Lean (default): no stats
        mockMvc
            .perform(get("/api/v1/subtemas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats").doesNotExist());

        // Summary: only total
        mockMvc
            .perform(get("/api/v1/subtemas").param("metrics", "summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats.total").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porNivel").doesNotExist());

        // Full: all breakdowns
        mockMvc
            .perform(get("/api/v1/subtemas").param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats.total").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porNivel").exists());
    }

    @Test
    void testGetAllSubtemas_DefaultSorting() throws Exception {
        // Create another tema to test sorting
        Tema tema2 = new Tema();
        tema2.setNome("Tema 2");
        tema2.setDisciplina(disciplinaRepository.save(new Disciplina("D2")));
        temaRepository.save(tema2);

        Subtema s1 = new Subtema(); s1.setNome("Subtema B"); s1.setTema(tema2);
        Subtema s2 = new Subtema(); s2.setNome("Subtema A"); s2.setTema(tema2);
        
        subtemaRepository.save(s1);
        subtemaRepository.save(s2);

        // Sort: nome ASC
        mockMvc
            .perform(get("/api/v1/subtemas").param("direction", "ASC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("Subtema A"))
            .andExpect(jsonPath("$.content[1].nome").value("Subtema B"));
    }

    @Test
    void testCreateSubtema() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Tema Test");
        tema.setDisciplina(disciplinaRepository.save(new Disciplina("D1")));
        tema = temaRepository.save(tema);

        SubtemaCreateRequest request = new SubtemaCreateRequest();
        request.setNome("New Subtema");
        request.setTemaId(tema.getId());

        mockMvc
            .perform(
                post("/api/v1/subtemas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testUpdateSubtema() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Tema Test");
        tema.setDisciplina(disciplinaRepository.save(new Disciplina("D1")));
        tema = temaRepository.save(tema);

        Subtema subtema = new Subtema();
        subtema.setNome("OldName");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        SubtemaUpdateRequest request = new SubtemaUpdateRequest();
        request.setNome("NewName");
        request.setTemaId(tema.getId());

        mockMvc
            .perform(
                put("/api/v1/subtemas/{id}", subtema.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testDeleteSubtema() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Tema Test");
        tema.setDisciplina(disciplinaRepository.save(new Disciplina("D1")));
        tema = temaRepository.save(tema);

        Subtema subtema = new Subtema();
        subtema.setNome("ToDelete");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        mockMvc
            .perform(delete("/api/v1/subtemas/{id}", subtema.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    void testCreateSubtema_Conflict_DuplicateNameInSameTema() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Tema Test");
        tema.setDisciplina(disciplinaRepository.save(new Disciplina("D1")));
        tema = temaRepository.save(tema);

        Subtema sub1 = new Subtema();
        sub1.setNome("SUBTEMA");
        sub1.setTema(tema);
        subtemaRepository.save(sub1);

        SubtemaCreateRequest request = new SubtemaCreateRequest();
        request.setNome("SUBTEMA");
        request.setTemaId(tema.getId());

        mockMvc
            .perform(
                post("/api/v1/subtemas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Já existe um subtema com o nome 'SUBTEMA' no tema com ID: " + tema.getId()));
    }

    @Test
    void testGetAllSubtemas_FilterScenarios() throws Exception {
        Disciplina d1 = disciplinaRepository.save(new Disciplina("D1"));
        Disciplina d2 = disciplinaRepository.save(new Disciplina("D2"));
        Tema t1a = temaRepository.save(new Tema(d1, "T1a"));
        Tema t1b = temaRepository.save(new Tema(d1, "T1b"));
        Tema t2a = temaRepository.save(new Tema(d2, "T2a"));
        
        subtemaRepository.save(new Subtema(t1a, "S1a1"));
        subtemaRepository.save(new Subtema(t1a, "S1a2"));
        subtemaRepository.save(new Subtema(t1b, "S1b1"));
        subtemaRepository.save(new Subtema(t2a, "S2a1"));

        // 1. One Disciplina AND One Tema (matches)
        mockMvc
            .perform(get("/api/v1/subtemas")
                .param("disciplinaIds", d1.getId().toString())
                .param("temaIds", t1a.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[*].nome").value(org.hamcrest.Matchers.containsInAnyOrder("S1a1", "S1a2")));

        // 2. One Disciplina AND Multiple Temas
        mockMvc
            .perform(get("/api/v1/subtemas")
                .param("disciplinaIds", d1.getId().toString())
                .param("temaIds", t1a.getId().toString() + "," + t1b.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.content[*].nome").value(org.hamcrest.Matchers.containsInAnyOrder("S1a1", "S1a2", "S1b1")));

        // 3. One Disciplina AND One Tema (mismatch) -> AND should result in empty
        mockMvc
            .perform(get("/api/v1/subtemas")
                .param("disciplinaIds", d1.getId().toString())
                .param("temaIds", t2a.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0));

        // 4. Multiple Disciplinas OR Tema (Current OR behavior)
        // D1, D2 OR T1a -> should return everything since T1a is inside D1
        mockMvc
            .perform(get("/api/v1/subtemas")
                .param("disciplinaIds", d1.getId().toString() + "," + d2.getId().toString())
                .param("temaIds", t1a.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(4));

        // 5. Only Disciplinas (OR)
        mockMvc
            .perform(get("/api/v1/subtemas")
                .param("disciplinaIds", d1.getId().toString() + "," + d2.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(4));

        // 6. Only Temas (OR)
        mockMvc
            .perform(get("/api/v1/subtemas")
                .param("temaIds", t1a.getId().toString() + "," + t2a.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.content[*].nome").value(org.hamcrest.Matchers.containsInAnyOrder("S1a1", "S1a2", "S2a1")));
    }
}
