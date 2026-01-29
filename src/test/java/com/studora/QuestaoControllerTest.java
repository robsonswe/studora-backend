package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.QuestaoDto;
import com.studora.entity.*;
import com.studora.repository.*;
import com.studora.util.TestUtil;
import java.util.Collections;
import java.util.List;
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
class QuestaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private BancaRepository bancaRepository;

    @Autowired
    private SubtemaRepository subtemaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    private Concurso concurso;
    private Subtema subtema;

    @BeforeEach
    void setUp() {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Questão Test");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Questão Test");
        banca = bancaRepository.save(banca);

        concurso = concursoRepository.save(
            new Concurso(instituicao, banca, 2023)
        );

        Disciplina disciplina = disciplinaRepository.save(
            new Disciplina("Disciplina Q Test")
        );
        Tema tema = temaRepository.save(new Tema(disciplina, "Tema Q Test"));
        subtema = subtemaRepository.save(new Subtema(tema, "Subtema Q Test"));
    }

    @Test
    void testCreateQuestao() throws Exception {
        QuestaoDto questaoDto = new QuestaoDto();
        questaoDto.setEnunciado("Qual a capital do Brasil?");
        questaoDto.setConcursoId(concurso.getId());
        questaoDto.setSubtemaIds(Collections.singletonList(subtema.getId()));

        mockMvc
            .perform(
                post("/api/questoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(questaoDto))
            )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.enunciado").value("Qual a capital do Brasil?")
            );
    }

    @Test
    void testGetQuestaoById() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Qual a capital do Brasil?");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        mockMvc
            .perform(get("/api/questoes/{id}", questao.getId()))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.enunciado").value("Qual a capital do Brasil?")
            );
    }

    @Test
    void testGetQuestaoById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/questoes/{id}", 99999L))
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

        mockMvc
            .perform(get("/api/questoes"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                )
            );
    }

    @Test
    void testGetQuestoesByConcursoId() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Questao Concurso");
        questao.setConcurso(concurso);
        questaoRepository.save(questao);

        mockMvc
            .perform(
                get("/api/questoes/concurso/{concursoId}", concurso.getId())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].enunciado").value("Questao Concurso"));
    }

    @Test
    void testGetQuestoesBySubtemaId() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Questao Subtema");
        questao.setConcurso(concurso);
        questao.setSubtemas(Collections.singletonList(subtema));
        questaoRepository.save(questao);

        mockMvc
            .perform(get("/api/questoes/subtema/{subtemaId}", subtema.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].enunciado").value("Questao Subtema"));
    }

    @Test
    void testGetQuestoesNaoAnuladas() throws Exception {
        Questao qValid = new Questao(concurso, "Valida");
        qValid.setAnulada(false);
        questaoRepository.save(qValid);

        Questao qAnulada = new Questao(concurso, "Anulada");
        qAnulada.setAnulada(true);
        questaoRepository.save(qAnulada);

        mockMvc
            .perform(get("/api/questoes/nao-anuladas"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(1)
                )
            )
            .andExpect(jsonPath("$[?(@.anulada == true)]").doesNotExist());
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
        updatedDto.setAnulada(true);

        mockMvc
            .perform(
                put("/api/questoes/{id}", questao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(updatedDto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enunciado").value("New Enunciado"))
            .andExpect(jsonPath("$.anulada").value(true));
    }

    @Test
    void testDeleteQuestao() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Questao to Delete");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        mockMvc
            .perform(delete("/api/questoes/{id}", questao.getId()))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/api/questoes/{id}", questao.getId()))
            .andExpect(status().isNotFound());
    }
}
