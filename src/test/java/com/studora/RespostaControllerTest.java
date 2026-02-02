package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.RespostaDto;
import com.studora.dto.request.RespostaCreateRequest;
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
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Resposta Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = concursoRepository.save(
            new Concurso(instituicao, banca, 2023, 1)
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
    void testGetRespostaByQuestaoId_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/respostas/questao/{questaoId}", 99999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreateResposta() throws Exception {
        RespostaCreateRequest respostaCreateRequest = new RespostaCreateRequest();
        respostaCreateRequest.setQuestaoId(questao.getId());
        respostaCreateRequest.setAlternativaId(alternativa.getId());

        mockMvc
            .perform(
                post("/api/respostas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(respostaCreateRequest))
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

        // Create a second question to avoid unique constraint violation on questao_id
        Questao questao2 = new Questao();
        questao2.setEnunciado("Enunciado 2");
        questao2.setConcurso(questao.getConcurso());
        questao2 = questaoRepository.save(questao2);

        Alternativa alt2 = new Alternativa();
        alt2.setOrdem(1);
        alt2.setCorreta(true);
        alt2.setTexto("Alt 2");
        alt2.setQuestao(questao2);
        alt2 = alternativaRepository.save(alt2);

        Resposta resposta2 = new Resposta();
        resposta2.setQuestao(questao2);
        resposta2.setAlternativaEscolhida(alt2);
        respostaRepository.save(resposta2);

        mockMvc
            .perform(get("/api/respostas"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                )
            );
    }

    @Test
    void testGetAllRespostas_DefaultSorting() throws Exception {
        // Clear and setup specific data
        respostaRepository.deleteAll();
        
        // Create 2 questions and answers with different timestamps
        Questao q1 = new Questao(); q1.setEnunciado("Q1"); q1.setConcurso(questao.getConcurso()); q1 = questaoRepository.save(q1);
        Alternativa a1 = new Alternativa(); a1.setOrdem(1); a1.setTexto("A1"); a1.setQuestao(q1); a1.setCorreta(true); a1 = alternativaRepository.save(a1);
        Resposta r1 = new Resposta(q1, a1); 
        r1 = respostaRepository.save(r1);

        // Sleep to ensure different createdAt (if necessary for the test logic, but normally order is enough)
        Thread.sleep(10);

        Questao q2 = new Questao(); q2.setEnunciado("Q2"); q2.setConcurso(questao.getConcurso()); q2 = questaoRepository.save(q2);
        Alternativa a2 = new Alternativa(); a2.setOrdem(1); a2.setTexto("A2"); a2.setQuestao(q2); a2.setCorreta(true); a2 = alternativaRepository.save(a2);
        Resposta r2 = new Resposta(q2, a2); 
        r2 = respostaRepository.save(r2);

        // Default sort: createdAt DESC
        // Expected: r2 (newest), then r1
        mockMvc
            .perform(get("/api/respostas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(r2.getId()))
            .andExpect(jsonPath("$.content[1].id").value(r1.getId()));
    }

    @Test
    void testGetRespostaByQuestaoId() throws Exception {
        Resposta resposta = new Resposta();
        resposta.setQuestao(questao);
        resposta.setAlternativaEscolhida(alternativa);
        resposta = respostaRepository.save(resposta);

        mockMvc
            .perform(get("/api/respostas/questao/{questaoId}", questao.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").value(resposta.getId()));
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

    @Test
    void testCreateResposta_UnprocessableEntity_AnswerToAnnulledQuestion() throws Exception {
        // Create an annulled question
        Questao annulledQuestao = new Questao();
        annulledQuestao.setEnunciado("Questão Anulada");
        annulledQuestao.setAnulada(true); // Set as annulled
        annulledQuestao.setConcurso(questao.getConcurso()); // Use the same concurso as the existing question
        annulledQuestao = questaoRepository.save(annulledQuestao);

        // Create an alternative for the annulled question
        Alternativa alternativaAnulada = new Alternativa();
        alternativaAnulada.setOrdem(1);
        alternativaAnulada.setCorreta(true);
        alternativaAnulada.setTexto("Alternativa da Questão Anulada");
        alternativaAnulada.setQuestao(annulledQuestao);
        alternativaAnulada = alternativaRepository.save(alternativaAnulada);

        // Try to create a resposta for the annulled question
        RespostaCreateRequest respostaCreateRequest = new RespostaCreateRequest();
        respostaCreateRequest.setQuestaoId(annulledQuestao.getId());
        respostaCreateRequest.setAlternativaId(alternativaAnulada.getId());

        mockMvc
            .perform(
                post("/api/respostas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(respostaCreateRequest))
            )
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Entidade não processável"))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.detail").value("Não é possível responder a uma questão anulada"));
    }
}