package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import com.studora.dto.request.TemaCreateRequest;
import com.studora.entity.Disciplina;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.TemaRepository;
import com.studora.util.TestUtil;
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
class AccentNormalizationSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Test
    void testSearchTemaWithAccents() throws Exception {
        // 1. Create a disciplina
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("Matemática");
        disciplina = disciplinaRepository.save(disciplina);

        // 2. Create a tema with accent
        TemaCreateRequest request = new TemaCreateRequest();
        request.setNome("Cálculo financeiro");
        request.setDisciplinaId(disciplina.getId());

        mockMvc.perform(post("/api/v1/temas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isCreated());

        // 3. Search using normalized name (no accent) - SHOULD WORK
        mockMvc.perform(get("/api/v1/temas")
                .param("nome", "Calculo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome", is("Cálculo financeiro")));

        // 4. Search using accented name - SHOULD WORK
        mockMvc.perform(get("/api/v1/temas")
                .param("nome", "Cálculo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome", is("Cálculo financeiro")));
    }

    @Test
    void testSearchTemaWithoutAccentsAgainstAccentedDB() throws Exception {
        // 1. Create a disciplina
        Disciplina disciplina = new Disciplina();
        disciplina.setNome("Direito");
        disciplina = disciplinaRepository.save(disciplina);

        // 2. Create a tema without accent in DB
        TemaCreateRequest request = new TemaCreateRequest();
        request.setNome("Calculo");
        request.setDisciplinaId(disciplina.getId());

        mockMvc.perform(post("/api/v1/temas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isCreated());

        // 3. Search using accented name - SHOULD WORK
        mockMvc.perform(get("/api/v1/temas")
                .param("nome", "Cálculo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome", is("Calculo")));
    }
}
