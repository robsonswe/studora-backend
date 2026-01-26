package com.studora;

import com.studora.dto.SubtemaDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
        subtemaRepository.deleteAll();
        temaRepository.deleteAll();
        disciplinaRepository.deleteAll();

        Disciplina disciplina = disciplinaRepository.save(new Disciplina("Direito Constitucional"));
        Tema newTema = new Tema();
        newTema.setNome("Controle de Constitucionalidade");
        newTema.setDisciplina(disciplina);
        tema = temaRepository.save(newTema);
    }

    @Test
    void testCreateSubtema() throws Exception {
        SubtemaDto subtemaDto = new SubtemaDto();
        subtemaDto.setNome("Espécies de Controle");
        subtemaDto.setTemaId(tema.getId());

        mockMvc.perform(post("/api/subtemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(subtemaDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Espécies de Controle"));
    }

    @Test
    void testGetSubtemaById() throws Exception {
        Subtema subtema = new Subtema();
        subtema.setNome("Espécies de Controle");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        mockMvc.perform(get("/api/subtemas/{id}", subtema.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Espécies de Controle"));
    }

    @Test
    void testGetSubtemaById_NotFound() throws Exception {
        mockMvc.perform(get("/api/subtemas/{id}", 999L))
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

        mockMvc.perform(get("/api/subtemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void testUpdateSubtema() throws Exception {
        Subtema subtema = new Subtema();
        subtema.setNome("Old Name");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        SubtemaDto updatedDto = new SubtemaDto();
        updatedDto.setNome("New Name");
        updatedDto.setTemaId(tema.getId());

        mockMvc.perform(put("/api/subtemas/{id}", subtema.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("New Name"));
    }

    @Test
    void testDeleteSubtema() throws Exception {
        Subtema subtema = new Subtema();
        subtema.setNome("Subtema to Delete");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        mockMvc.perform(delete("/api/subtemas/{id}", subtema.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/subtemas/{id}", subtema.getId()))
                .andExpect(status().isNotFound());
    }
}
