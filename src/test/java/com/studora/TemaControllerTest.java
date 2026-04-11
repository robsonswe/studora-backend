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

    @BeforeEach
    void setUp() {
        temaRepository.deleteAll();
        disciplinaRepository.deleteAll();
    }

    @Test
    void testGetTemaById_FullMetrics() throws Exception {
        Disciplina d = disciplinaRepository.save(new Disciplina("D1"));
        Tema t = temaRepository.save(new Tema(d, "T1"));

        mockMvc
            .perform(get("/api/v1/temas/{id}", t.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("T1"))
            .andExpect(jsonPath("$.questaoStats").exists())
            .andExpect(jsonPath("$.questaoStats.total").exists())
            // Leanerization: Nested disciplina must not have stats
            .andExpect(jsonPath("$.disciplina.questaoStats").doesNotExist());
    }

    @Test
    void testGetAllTemas_MetricsTiers() throws Exception {
        Disciplina d = disciplinaRepository.save(new Disciplina("D1"));
        temaRepository.save(new Tema(d, "T1"));

        // Lean (default): no stats
        mockMvc
            .perform(get("/api/v1/temas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats").doesNotExist());

        // Summary: only total
        mockMvc
            .perform(get("/api/v1/temas").param("metrics", "summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats.total").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porNivel").doesNotExist());

        // Full: all breakdowns
        mockMvc
            .perform(get("/api/v1/temas").param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats.total").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porNivel").exists());
    }

    @Test
    void testGetAllTemas_DefaultSorting() throws Exception {
        Disciplina d = disciplinaRepository.save(new Disciplina("D1"));
        Tema t1 = new Tema(); t1.setNome("Tema B"); t1.setDisciplina(d);
        Tema t2 = new Tema(); t2.setNome("Tema A"); t2.setDisciplina(d);
        
        temaRepository.save(t1);
        temaRepository.save(t2);

        // Sort: name ASC
        mockMvc
            .perform(get("/api/v1/temas").param("direction", "ASC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("Tema A"))
            .andExpect(jsonPath("$.content[1].nome").value("Tema B"));
    }

    @Test
    void testCreateTema() throws Exception {
        Disciplina d = disciplinaRepository.save(new Disciplina("D1"));
        
        TemaCreateRequest request = new TemaCreateRequest();
        request.setNome("New Tema");
        request.setDisciplinaId(d.getId());

        mockMvc
            .perform(
                post("/api/v1/temas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testUpdateTema() throws Exception {
        Disciplina d = disciplinaRepository.save(new Disciplina("D1"));
        Tema tema = new Tema();
        tema.setNome("OldName");
        tema.setDisciplina(d);
        tema = temaRepository.save(tema);

        TemaUpdateRequest request = new TemaUpdateRequest();
        request.setNome("NewName");
        request.setDisciplinaId(d.getId());

        mockMvc
            .perform(
                put("/api/v1/temas/{id}", tema.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testDeleteTema() throws Exception {
        Disciplina d = disciplinaRepository.save(new Disciplina("D1"));
        Tema tema = new Tema();
        tema.setNome("ToDelete");
        tema.setDisciplina(d);
        tema = temaRepository.save(tema);

        mockMvc
            .perform(delete("/api/v1/temas/{id}", tema.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    void testCreateTema_Conflict_DuplicateNameInSameDisciplina() throws Exception {
        Disciplina d = disciplinaRepository.save(new Disciplina("D1"));
        Tema t1 = new Tema();
        t1.setNome("TEMA");
        t1.setDisciplina(d);
        temaRepository.save(t1);

        TemaCreateRequest request = new TemaCreateRequest();
        request.setNome("TEMA");
        request.setDisciplinaId(d.getId());

        mockMvc
            .perform(
                post("/api/v1/temas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Já existe um tema com o nome 'TEMA' na disciplina com ID: " + d.getId()));
    }

    @Test
    void testGetAllTemas_FilterByDisciplinaIds() throws Exception {
        Disciplina d1 = disciplinaRepository.save(new Disciplina("D1"));
        Disciplina d2 = disciplinaRepository.save(new Disciplina("D2"));
        
        temaRepository.save(new Tema(d1, "Tema D1"));
        temaRepository.save(new Tema(d2, "Tema D2"));

        mockMvc
            .perform(get("/api/v1/temas").param("disciplinaIds", d1.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].nome").value("Tema D1"));

        mockMvc
            .perform(get("/api/v1/temas").param("disciplinaIds", d1.getId() + "," + d2.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));
    }
}
