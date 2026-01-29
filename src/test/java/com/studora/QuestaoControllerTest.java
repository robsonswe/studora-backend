package com.studora;

import com.studora.dto.QuestaoDto;
import com.studora.entity.Concurso;
import com.studora.entity.Questao;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.QuestaoRepository;
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
class QuestaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    private Concurso concurso;

    @BeforeEach
    void setUp() {
        questaoRepository.deleteAll();
        concursoRepository.deleteAll();
        concurso = concursoRepository.save(new Concurso("Concurso 1", "Banca 1", 2023, "Cargo 1", "Nível 1", "Área 1"));
    }

    @Test
    void testCreateQuestao() throws Exception {
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital do Brasil?");
        questaoDto.setConcursoId(concurso.getId());

        mockMvc.perform(post("/api/questoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(questaoDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enunciado").value("Qual a capital do Brasil?"));
    }

    @Test
    void testGetQuestaoById() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Qual a capital do Brasil?");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        mockMvc.perform(get("/api/questoes/{id}", questao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Qual a capital do Brasil?"));
    }

    @Test
    void testGetQuestaoById_NotFound() throws Exception {
        mockMvc.perform(get("/api/questoes/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllQuestoes() throws Exception {
        Questao questao1 = new Questao();
        questao1.setEnunciado("Enunciado 1");
        questao1.setConcurso(concurso);
        questaoRepository.save(questao1);

        Questao questao2 = new Questao();
        questao2.setEnunciado("Enunciado 2");
        questao2.setConcurso(concurso);
        questaoRepository.save(questao2);

        mockMvc.perform(get("/api/questoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void testUpdateQuestao() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Old Enunciado");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        QuestaoDto updatedDto = new QuestaoDto();
        updatedDto.setEnunciado("New Enunciado");
        updatedDto.setConcursoId(concurso.getId());


        mockMvc.perform(put("/api/questoes/{id}", questao.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("New Enunciado"));
    }

    @Test
    void testDeleteQuestao() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Questao to Delete");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        mockMvc.perform(delete("/api/questoes/{id}", questao.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/questoes/{id}", questao.getId()))
                .andExpect(status().isNotFound());
    }
}
