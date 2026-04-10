package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.DisciplinaCreateRequest;
import com.studora.dto.request.DisciplinaUpdateRequest;
import com.studora.entity.Disciplina;
import com.studora.repository.DisciplinaRepository;
import com.studora.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
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

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear the disciplina-stats cache to avoid stale data from previous tests
        if (cacheManager != null) {
            var cache = cacheManager.getCache("disciplina-stats");
            if (cache != null) {
                cache.clear();
            }
        }
    }

    @Test
    void testCreateDisciplina() throws Exception {
        DisciplinaCreateRequest disciplinaCreateRequest = new DisciplinaCreateRequest();
        disciplinaCreateRequest.setNome("Direito Test");

        mockMvc
            .perform(
                post("/api/v1/disciplinas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(disciplinaCreateRequest))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testGetDisciplinaById() throws Exception {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("Direito Get Test");
        disciplina = disciplinaRepository.save(disciplina);

        mockMvc
            .perform(get("/api/v1/disciplinas/{id}", disciplina.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Direito Get Test"))
            .andExpect(jsonPath("$.questaoStats").exists());
    }

    @Test
    void testGetDisciplinaById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/v1/disciplinas/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllDisciplinas() throws Exception {
        disciplinaRepository.save(new Disciplina("Direito All 1"));
        disciplinaRepository.save(new Disciplina("Direito All 2"));

        mockMvc
            .perform(get("/api/v1/disciplinas").param("metrics", "summary"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                )
            )
            .andExpect(jsonPath("$.content[0].questaoStats").exists());
    }

    @Test
    void testGetAllDisciplinas_DefaultSorting() throws Exception {
        disciplinaRepository.save(new Disciplina("Direito B"));
        disciplinaRepository.save(new Disciplina("Direito A"));

        // Default sort: nome ASC, id DESC
        mockMvc
            .perform(get("/api/v1/disciplinas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("Direito A"))
            .andExpect(jsonPath("$.content[1].nome").value("Direito B"));
    }

    @Test
    void testGetAllDisciplinas_MetricsTiers() throws Exception {
        Disciplina disciplina = disciplinaRepository.save(new Disciplina("Direito Tiers"));

        // Lean (default): only structural fields, questaoStats omitted
        mockMvc
            .perform(get("/api/v1/disciplinas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").isNumber())
            .andExpect(jsonPath("$.content[0].nome").value("Direito Tiers"))
            .andExpect(jsonPath("$.content[0].questaoStats").doesNotExist());

        // Summary: questaoStats with basic metrics
        mockMvc
            .perform(get("/api/v1/disciplinas").param("metrics", "summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.total").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porBanca").doesNotExist());

        // Full: questaoStats with all breakdowns
        mockMvc
            .perform(get("/api/v1/disciplinas").param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].questaoStats").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.total").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porNivel").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porBanca").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porInstituicao").exists())
            .andExpect(jsonPath("$.content[0].questaoStats.porCargo").exists());
    }

    @Test
    void testGetAllDisciplinas_CustomSortingByDirection() throws Exception {
        disciplinaRepository.save(new Disciplina("Direito B"));
        disciplinaRepository.save(new Disciplina("Direito A"));

        // Sort: nome DESC
        mockMvc
            .perform(get("/api/v1/disciplinas").param("direction", "DESC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].nome").value("Direito B"))
            .andExpect(jsonPath("$.content[1].nome").value("Direito A"));
    }

    @Test
    void testUpdateDisciplina() throws Exception {
        Disciplina disciplina = new Disciplina("Old Name");
        disciplina = disciplinaRepository.save(disciplina);

        DisciplinaUpdateRequest updateRequest = new DisciplinaUpdateRequest();
        updateRequest.setNome("New Name");

        mockMvc
            .perform(
                put("/api/v1/disciplinas/{id}", disciplina.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(updateRequest))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testDeleteDisciplina() throws Exception {
        Disciplina disciplina = new Disciplina("Disciplina to Delete");
        disciplina = disciplinaRepository.save(disciplina);

        mockMvc
            .perform(delete("/api/v1/disciplinas/{id}", disciplina.getId()))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/api/v1/disciplinas/{id}", disciplina.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreateDisciplina_Conflict_DuplicateName() throws Exception {
        // Create first disciplina
        Disciplina disciplina1 = new Disciplina("Direito Administrativo");
        disciplinaRepository.save(disciplina1);

        // Try to create another disciplina with the same name
        DisciplinaCreateRequest request = new DisciplinaCreateRequest();
        request.setNome("Direito Administrativo");

        mockMvc
            .perform(
                post("/api/v1/disciplinas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Conflito"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Já existe uma disciplina com o nome 'Direito Administrativo'"));
    }

    @Test
    void testGetDisciplinaCompleto() throws Exception {
        Disciplina disciplina = new Disciplina("Disciplina Completa");
        disciplina = disciplinaRepository.save(disciplina);

        // Lean: request lean explicitly to override default 'full' in controller
        mockMvc
            .perform(get("/api/v1/disciplinas/{id}/completo", disciplina.getId()).param("metrics", "lean"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(disciplina.getId()))
            .andExpect(jsonPath("$.nome").value("Disciplina Completa"))
            .andExpect(jsonPath("$.questaoStats").doesNotExist());

        // Full: metrics populated with recursive stats
        mockMvc
            .perform(get("/api/v1/disciplinas/{id}/completo", disciplina.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.questaoStats").exists());
    }
}
