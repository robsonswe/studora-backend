package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.RespostaDto;
import com.studora.entity.*;
import com.studora.repository.*;
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
class RespostaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RespostaRepository respostaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private BancaRepository bancaRepository;

    private Questao questao;
    private Alternativa alternativa;

    @BeforeEach
    void setUp() {
        // Create and save Instituicao and Banca first
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Resposta Test");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Resposta Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = concursoRepository.save(
            new Concurso(instituicao, banca, 2023)
        );
        Questao newQuestao = new Questao();
        newQuestao.setEnunciado("Enunciado da Questão Resposta");
        newQuestao.setConcurso(concurso);
        questao = questaoRepository.save(newQuestao);

        Alternativa newAlternativa = new Alternativa();
        newAlternativa.setOrdem(1);
        newAlternativa.setCorreta(true);
        newAlternativa.setTexto("Texto da Alternativa Resposta");
        newAlternativa.setQuestao(questao);
        alternativa = alternativaRepository.save(newAlternativa);
    }

    @Test
    void testCreateResposta() throws Exception {
        RespostaDto respostaDto = new RespostaDto();
        respostaDto.setQuestaoId(questao.getId());
        respostaDto.setAlternativaId(alternativa.getId());

        mockMvc
            .perform(
                post("/api/respostas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(respostaDto))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.questaoId").value(questao.getId()));
    }

    @Test
    void testGetRespostaById() throws Exception {
        Resposta resposta = new Resposta();
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);
        resposta = respostaRepository.save(resposta);

        mockMvc
            .perform(get("/api/respostas/{id}", resposta.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(resposta.getId()));
    }

    @Test
    void testGetRespostaById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/respostas/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllRespostas() throws Exception {
        Resposta resposta1 = new Resposta();
        resposta1.setQuestao(questao);
        resposta1.setAlternativaEscolhida(alternativa);
        respostaRepository.save(resposta1);

        Resposta resposta2 = new Resposta();
        resposta2.setQuestao(questao);
        resposta2.setAlternativaEscolhida(alternativa);
        respostaRepository.save(resposta2);

        mockMvc
            .perform(get("/api/respostas"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                )
            );
    }

    @Test
    void testDeleteResposta() throws Exception {
        Resposta resposta = new Resposta();
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);
        resposta = respostaRepository.save(resposta);

        mockMvc
            .perform(delete("/api/respostas/{id}", resposta.getId()))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/api/respostas/{id}", resposta.getId()))
            .andExpect(status().isNotFound());
    }
}
