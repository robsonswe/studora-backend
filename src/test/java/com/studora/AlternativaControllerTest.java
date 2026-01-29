package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.AlternativaDto;
import com.studora.entity.Alternativa;
import com.studora.entity.Banca;
import com.studora.entity.Concurso;
import com.studora.entity.Instituicao;
import com.studora.entity.Questao;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.BancaRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.InstituicaoRepository;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AlternativaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private BancaRepository bancaRepository;

    private Questao questao;

    @BeforeEach
    void setUp() {
        // Create and save Instituicao and Banca first
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Alt Test");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Alt Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = concursoRepository.save(
            new Concurso(instituicao, banca, 2023)
        );

        Questao newQuestao = new Questao();
        newQuestao.setEnunciado("Enunciado da Questão Alt Test");
        newQuestao.setConcurso(concurso);
        questao = questaoRepository.save(newQuestao);
    }

    @Test
    void testCreateAlternativa() throws Exception {
        AlternativaDto alternativaDto = new AlternativaDto();
        alternativaDto.setOrdem(1);
        alternativaDto.setTexto("Texto da Alternativa");
        alternativaDto.setCorreta(true);
        alternativaDto.setQuestaoId(questao.getId());

        mockMvc
            .perform(
                post("/api/alternativas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(alternativaDto))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.texto").value("Texto da Alternativa"));
    }

    @Test
    void testGetAlternativaById() throws Exception {
        Alternativa alternativa = new Alternativa();
        alternativa.setOrdem(1);
        alternativa.setTexto("Texto da Alternativa");
        alternativa.setCorreta(true);
        alternativa.setQuestao(questao);
        alternativa = alternativaRepository.save(alternativa);

        mockMvc
            .perform(get("/api/alternativas/{id}", alternativa.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.texto").value("Texto da Alternativa"));
    }

    @Test
    void testGetAlternativaById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/alternativas/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAlternativasByQuestaoId() throws Exception {
        Alternativa alt = new Alternativa();
        alt.setOrdem(1);
        alt.setTexto("Alt Questao");
        alt.setCorreta(false);
        alt.setQuestao(questao);
        alternativaRepository.save(alt);

        mockMvc
            .perform(
                get("/api/alternativas/questao/{questaoId}", questao.getId())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].texto").value("Alt Questao"));
    }

    @Test
    void testGetAlternativasCorretasByQuestaoId() throws Exception {
        Alternativa altCorrect = new Alternativa();
        altCorrect.setOrdem(1);
        altCorrect.setTexto("Correct");
        altCorrect.setCorreta(true);
        altCorrect.setQuestao(questao);
        alternativaRepository.save(altCorrect);

        Alternativa altWrong = new Alternativa();
        altWrong.setOrdem(2);
        altWrong.setTexto("Wrong");
        altWrong.setCorreta(false);
        altWrong.setQuestao(questao);
        alternativaRepository.save(altWrong);

        mockMvc
            .perform(
                get(
                    "/api/alternativas/questao/{questaoId}/corretas",
                    questao.getId()
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].texto").value("Correct"));
    }

    @Test
    void testGetAllAlternativas() throws Exception {
        Alternativa alternativa1 = new Alternativa();
        alternativa1.setOrdem(1);
        alternativa1.setCorreta(true);
        alternativa1.setTexto("Alternativa 1");
        alternativa1.setQuestao(questao);
        alternativaRepository.save(alternativa1);

        Alternativa alternativa2 = new Alternativa();
        alternativa2.setOrdem(2);
        alternativa2.setCorreta(false);
        alternativa2.setTexto("Alternativa 2");
        alternativa2.setQuestao(questao);
        alternativaRepository.save(alternativa2);

        mockMvc
            .perform(get("/api/alternativas"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                )
            );
    }

    @Test
    void testUpdateAlternativa() throws Exception {
        Alternativa alternativa = new Alternativa();
        alternativa.setOrdem(1);
        alternativa.setCorreta(false);
        alternativa.setTexto("Old Texto");
        alternativa.setQuestao(questao);
        alternativa = alternativaRepository.save(alternativa);

        AlternativaDto updatedDto = new AlternativaDto();
        updatedDto.setOrdem(1);
        updatedDto.setCorreta(true);
        updatedDto.setTexto("New Texto");
        updatedDto.setQuestaoId(questao.getId());

        mockMvc
            .perform(
                put("/api/alternativas/{id}", alternativa.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(updatedDto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.texto").value("New Texto"));
    }

    @Test
    void testDeleteAlternativa() throws Exception {
        Alternativa alternativa = new Alternativa();
        alternativa.setOrdem(1);
        alternativa.setCorreta(false);
        alternativa.setTexto("Alternativa to Delete");
        alternativa.setQuestao(questao);
        alternativa = alternativaRepository.save(alternativa);

        mockMvc
            .perform(delete("/api/alternativas/{id}", alternativa.getId()))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/api/alternativas/{id}", alternativa.getId()))
            .andExpect(status().isNotFound());
    }
}
