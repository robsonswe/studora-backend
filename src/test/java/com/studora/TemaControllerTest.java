package com.studora;

import com.studora.dto.TemaDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    private Disciplina disciplina;

    @BeforeEach
    void setUp() {
        temaRepository.deleteAll();
        disciplinaRepository.deleteAll();
        disciplina = disciplinaRepository.save(new Disciplina("Direito Constitucional"));
    }

    @Test
    void testCreateTema() throws Exception {
        TemaDto temaDto = new TemaDto();
        temaDto.setNome("Controle de Constitucionalidade");
        temaDto.setDisciplinaId(disciplina.getId());

        mockMvc.perform(post("/api/temas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(temaDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Controle de Constitucionalidade"));
    }

    @Test
    void testGetTemaById() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Controle de Constitucionalidade");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        mockMvc.perform(get("/api/temas/{id}", tema.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Controle de Constitucionalidade"));
    }

    @Test
    void testGetTemaById_NotFound() throws Exception {
        mockMvc.perform(get("/api/temas/{id}", 999L))
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

        mockMvc.perform(get("/api/temas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void testUpdateTema() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Old Name");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        TemaDto updatedDto = new TemaDto();
        updatedDto.setNome("New Name");
        updatedDto.setDisciplinaId(disciplina.getId());

        mockMvc.perform(put("/api/temas/{id}", tema.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("New Name"));
    }

    @Test
    void testDeleteTema() throws Exception {
        Tema tema = new Tema();
        tema.setNome("Tema to Delete");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        mockMvc.perform(delete("/api/temas/{id}", tema.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/temas/{id}", tema.getId()))
                .andExpect(status().isNotFound());
    }
}
