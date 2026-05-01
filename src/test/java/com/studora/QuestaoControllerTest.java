package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.AlternativaCreateRequest;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.entity.*;
import com.studora.entity.NivelCargo;
import com.studora.repository.*;
import com.studora.util.TestUtil;
import java.util.Arrays;
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

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private ConcursoCargoRepository concursoCargoRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private RespostaRepository respostaRepository;

    @Autowired
    private ProvaRepository provaRepository;

    @Autowired
    private ProvaSecaoRepository provaSecaoRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Concurso concurso;
    private Subtema subtema;
    private Cargo cargo;
    private ConcursoCargo concursoCargo;
    private ProvaSecao savedSecao;

    @BeforeEach
    void setUp() {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Questão Test");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Questão Test");
        banca = bancaRepository.save(banca);

        concurso = concursoRepository.save(
                new Concurso(instituicao, banca, 2023, 1));

        Disciplina disciplina = disciplinaRepository.save(
                new Disciplina("Disciplina Q Test"));
        Tema tema = temaRepository.save(new Tema(disciplina, "Tema Q Test"));
        subtema = subtemaRepository.save(new Subtema(tema, "Subtema Q Test"));

        // Create a cargo and associate it with the concurso
        cargo = new Cargo();
        cargo.setNome("Cargo Test");
        cargo.setNivel(NivelCargo.SUPERIOR);
        cargo.setArea("Área Test");
        cargo = cargoRepository.save(cargo);

        concursoCargo = new ConcursoCargo();
        concursoCargo.setConcurso(concurso);
        concursoCargo.setCargo(cargo);
        concursoCargo = concursoCargoRepository.save(concursoCargo);

        // Create Prova and Secao
        Prova prova = new Prova();
        prova.setConcurso(concurso);
        prova.setNome("Prova Q Test");
        prova.addCargo(concursoCargo);
        prova = provaRepository.save(prova);

        savedSecao = new ProvaSecao();
        savedSecao.setProva(prova);
        savedSecao.setNome("Seção Q Test");
        savedSecao.setOrdem(1);
        savedSecao = provaSecaoRepository.save(savedSecao);
    }

    @Test
    void testCreateQuestao() throws Exception {
        // Create alternativas using AlternativaCreateRequest
        AlternativaCreateRequest alt1 = new AlternativaCreateRequest();
        alt1.setOrdem(1);
        alt1.setTexto("Brasília");
        alt1.setCorreta(true);
        alt1.setJustificativa("Capital oficial do Brasil");

        AlternativaCreateRequest alt2 = new AlternativaCreateRequest();
        alt2.setOrdem(2);
        alt2.setTexto("São Paulo");
        alt2.setCorreta(false);
        alt2.setJustificativa("Maior cidade do Brasil");

        QuestaoCreateRequest questaoCreateRequest = new QuestaoCreateRequest();
        questaoCreateRequest.setEnunciado("Qual a capital do Brasil?");
        questaoCreateRequest.setSecoesIds(Collections.singletonList(savedSecao.getId()));
        questaoCreateRequest.setSubtemaIds(Collections.singletonList(subtema.getId()));
        // Add alternativas to comply with validation
        questaoCreateRequest.setAlternativas(Arrays.asList(alt1, alt2));

        // Store tema and disciplina for assertions
        Tema tema = subtema.getTema();
        Disciplina disciplina = tema.getDisciplina();

        // POST returns PostResponseDto with id and message
        String postResponse = mockMvc
                .perform(
                        post("/api/v1/questoes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.asJsonString(questaoCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.message").exists())
                .andReturn().getResponse().getContentAsString();

        Long createdId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(postResponse).get("id").asLong();

        // Verify entity details via GET (admin=true to expose full detail)
        mockMvc.perform(get("/api/v1/questoes/{id}", createdId).param("admin", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Qual a capital do Brasil?"))
                .andExpect(jsonPath("$.concurso.cargos[0].id").value(cargo.getId()))
                .andExpect(jsonPath("$.concurso.cargos[0].secoes[0].nome").value("Seção Q Test"))
                .andExpect(jsonPath("$.subtemas[0].id").value(subtema.getId()))
                .andExpect(jsonPath("$.subtemas[0].nome").value(subtema.getNome()))
                .andExpect(jsonPath("$.subtemas[0].tema.id").value(tema.getId()))
                .andExpect(jsonPath("$.subtemas[0].tema.nome").value(tema.getNome()))
                .andExpect(jsonPath("$.subtemas[0].disciplina.id").value(disciplina.getId()))
                .andExpect(jsonPath("$.subtemas[0].disciplina.nome").value(disciplina.getNome()));
    }

    @Test
    void testGetQuestaoById_HiddenByDefault() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Qual a capital do Brasil?");

        QuestaoProvaSecao qps = new QuestaoProvaSecao();
        qps.setProvaSecao(savedSecao);
        questao.addSecao(qps);

        questao = questaoRepository.save(questao);

        // Add some alternatives to the question
        com.studora.entity.Alternativa alt1 = new com.studora.entity.Alternativa();
        alt1.setQuestao(questao);
        alt1.setOrdem(1);
        alt1.setTexto("Brasília");
        alt1.setCorreta(true);
        alt1.setJustificativa("Capital do Brasil");
        alt1 = alternativaRepository.save(alt1);
        questao.getAlternativas().add(alt1);

        com.studora.entity.Alternativa alt2 = new com.studora.entity.Alternativa();
        alt2.setQuestao(questao);
        alt2.setOrdem(2);
        alt2.setTexto("São Paulo");
        alt2.setCorreta(false);
        alt2.setJustificativa("Maior cidade do Brasil");
        alt2 = alternativaRepository.save(alt2);
        questao.getAlternativas().add(alt2);

        mockMvc
                .perform(get("/api/v1/questoes/{id}", questao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Qual a capital do Brasil?"))
                .andExpect(jsonPath("$.alternativas.length()").value(2))
                // By default (no responses), gabarito IS HIDDEN
                .andExpect(jsonPath("$.alternativas[0].correta").doesNotExist())
                .andExpect(jsonPath("$.concurso.cargos[0].id").value(cargo.getId()));
    }

    @Test
    void testGetQuestaoById_VisibleIfRecent() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Visible Test");
        questao.setAutoral(true);
        questao = questaoRepository.save(questao);

        Alternativa alt = new Alternativa();
        alt.setQuestao(questao);
        alt.setOrdem(1);
        alt.setTexto("A");
        alt.setCorreta(true);
        alt = alternativaRepository.save(alt);

        Resposta resp = new Resposta(questao, alt);
        resp = respostaRepository.save(resp);

        String nowStr = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        entityManager.createNativeQuery("UPDATE resposta SET created_at = '" + nowStr + "' WHERE id = " + resp.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // Should show gabarito because of recent answer
        mockMvc
                .perform(get("/api/v1/questoes/" + questao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alternativas[0].correta").exists())
                .andExpect(jsonPath("$.alternativas[0].correta").value(true));
    }

    @Test
    void testGetQuestaoById_HiddenIfOld() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Hidden Test");
        questao.setAutoral(true);
        questao = questaoRepository.save(questao);

        Alternativa alt = new Alternativa();
        alt.setQuestao(questao);
        alt.setOrdem(1);
        alt.setTexto("A");
        alt.setCorreta(true);
        alt = alternativaRepository.save(alt);

        Resposta resp = new Resposta(questao, alt);
        resp = respostaRepository.save(resp);

        String oldDateStr = java.time.LocalDateTime.now().minusMonths(2)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        entityManager
                .createNativeQuery("UPDATE resposta SET created_at = '" + oldDateStr + "' WHERE id = " + resp.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // Should hide gabarito because answer is old
        mockMvc
                .perform(get("/api/v1/questoes/" + questao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alternativas[0].correta").doesNotExist());
    }

    @Test
    void testGetQuestaoById_VisibleIfAdmin() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Admin Test");
        questao.setAutoral(true);
        questao = questaoRepository.save(questao);

        com.studora.entity.Alternativa alt = new com.studora.entity.Alternativa();
        alt.setQuestao(questao);
        alt.setOrdem(1);
        alt.setTexto("A");
        alt.setCorreta(true);
        alt.setJustificativa("Justificativa Admin");
        alt = alternativaRepository.save(alt);
        questao.getAlternativas().add(alt);

        // Without admin: hidden (since no responses)
        mockMvc
                .perform(get("/api/v1/questoes/" + questao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alternativas[0].correta").doesNotExist());

        // With admin=true: visible
        mockMvc
                .perform(get("/api/v1/questoes/" + questao.getId()).param("admin", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alternativas[0].correta").exists())
                .andExpect(jsonPath("$.alternativas[0].correta").value(true))
                .andExpect(jsonPath("$.alternativas[0].justificativa").value("Justificativa Admin"));
    }

    @Test
    void testGetQuestaoById_SubtemasHaveNestedReferences() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Nested References Test");

        QuestaoProvaSecao qps = new QuestaoProvaSecao();
        qps.setProvaSecao(savedSecao);
        questao.addSecao(qps);

        questao.getSubtemas().add(subtema);
        questao = questaoRepository.save(questao);

        // Add alternatives (required for valid response)
        com.studora.entity.Alternativa alt1 = new com.studora.entity.Alternativa();
        alt1.setQuestao(questao);
        alt1.setOrdem(1);
        alt1.setTexto("A");
        alt1.setCorreta(true);
        alt1.setJustificativa("Correct");
        alt1 = alternativaRepository.save(alt1);
        questao.getAlternativas().add(alt1);

        Tema tema = subtema.getTema();
        Disciplina disciplina = tema.getDisciplina();

        mockMvc
                .perform(get("/api/v1/questoes/{id}", questao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtemas[0].id").value(subtema.getId()))
                .andExpect(jsonPath("$.subtemas[0].nome").value(subtema.getNome()))
                .andExpect(jsonPath("$.subtemas[0].tema.id").value(tema.getId()))
                .andExpect(jsonPath("$.subtemas[0].tema.nome").value(tema.getNome()))
                .andExpect(jsonPath("$.subtemas[0].disciplina.id").value(disciplina.getId()))
                .andExpect(jsonPath("$.subtemas[0].disciplina.nome").value(disciplina.getNome()));
    }

    @Test
    void testGetQuestaoById_NotFound() throws Exception {
        mockMvc
                .perform(get("/api/v1/questoes/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllQuestoes_WithVisibilityRules() throws Exception {
        // Questao 1: No responses -> Gabarito Hidden
        Questao q1 = new Questao();
        q1.setEnunciado("Q1 Enunciado");
        q1.setAutoral(true);
        q1 = questaoRepository.save(q1);
        Alternativa alt1 = new Alternativa();
        alt1.setQuestao(q1);
        alt1.setOrdem(1);
        alt1.setTexto("Alt 1");
        alt1.setCorreta(true);
        alternativaRepository.save(alt1);

        // Questao 2: Recent response -> Gabarito Visible
        Questao q2 = new Questao();
        q2.setEnunciado("Q2 Enunciado");
        q2.setAutoral(true);
        q2.setImageUrl("http://img.com/2.png");
        q2 = questaoRepository.save(q2);
        Alternativa alt2 = new Alternativa();
        alt2.setQuestao(q2);
        alt2.setOrdem(1);
        alt2.setTexto("Alt 2");
        alt2.setCorreta(true);
        alternativaRepository.save(alt2);

        Resposta resp = new Resposta(q2, alt2);
        resp = respostaRepository.save(resp);
        String nowStr = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        entityManager.createNativeQuery("UPDATE resposta SET created_at = '" + nowStr + "' WHERE id = " + resp.getId())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // Perform request
        mockMvc
                .perform(get("/api/v1/questoes").param("sort", "id").param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                // Q1: Hidden
                .andExpect(jsonPath("$.content[0].id").value(q1.getId()))
                .andExpect(jsonPath("$.content[0].autoral").value(true))
                .andExpect(jsonPath("$.content[0].alternativas[0].texto").value("Alt 1"))
                .andExpect(jsonPath("$.content[0].alternativas[0].correta").doesNotExist())
                // Q2: Visible
                .andExpect(jsonPath("$.content[1].id").value(q2.getId()))
                .andExpect(jsonPath("$.content[1].imageUrl").value("http://img.com/2.png"))
                .andExpect(jsonPath("$.content[1].alternativas[0].correta").value(true))
                .andExpect(jsonPath("$.content[1].respostas.length()").value(1));
    }

    @Test
    void testUpdateQuestao() throws Exception {
        // First create a question with secao association
        Questao questao = new Questao();
        questao.setEnunciado("Old Enunciado");

        QuestaoProvaSecao qps = new QuestaoProvaSecao();
        qps.setProvaSecao(savedSecao);
        questao.addSecao(qps);

        questao = questaoRepository.save(questao);

        // Create alternativas for the update request
        com.studora.dto.request.AlternativaUpdateRequest alt1 = new com.studora.dto.request.AlternativaUpdateRequest();
        alt1.setOrdem(1);
        alt1.setTexto("Updated Alternative 1");
        alt1.setCorreta(true);
        alt1.setJustificativa("Updated justification 1");

        com.studora.dto.request.AlternativaUpdateRequest alt2 = new com.studora.dto.request.AlternativaUpdateRequest();
        alt2.setOrdem(2);
        alt2.setTexto("Updated Alternative 2");
        alt2.setCorreta(false);
        alt2.setJustificativa("Updated justification 2");

        QuestaoUpdateRequest updatedRequest = new QuestaoUpdateRequest();
        updatedRequest.setEnunciado("New Enunciado");
        updatedRequest.setSecoesIds(Collections.singletonList(savedSecao.getId()));
        updatedRequest.setAnulada(true);
        updatedRequest.setSubtemaIds(Collections.singletonList(subtema.getId()));
        // Add new alternatives for the update
        updatedRequest.setAlternativas(Arrays.asList(alt1, alt2));

        // Add some initial alternatives to the question
        com.studora.entity.Alternativa initialAlt = new com.studora.entity.Alternativa();
        initialAlt.setQuestao(questao);
        initialAlt.setOrdem(1);
        initialAlt.setTexto("Initial Alternative");
        initialAlt.setCorreta(false);
        initialAlt.setJustificativa("Initial justification");
        initialAlt = alternativaRepository.save(initialAlt);
        questao.getAlternativas().add(initialAlt);

        mockMvc
                .perform(
                        put("/api/v1/questoes/{id}", questao.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(TestUtil.asJsonString(updatedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("New Enunciado"))
                .andExpect(jsonPath("$.anulada").value(true))
                .andExpect(jsonPath("$.alternativas.length()").value(2))
                .andExpect(jsonPath("$.concurso.cargos[0].id").value(cargo.getId()))
                .andExpect(jsonPath("$.concurso.cargos[0].secoes[0].id").value(savedSecao.getId()));
    }

    @Test
    void testDeleteQuestao() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Questao to Delete");
        questao.setAutoral(true);
        questao = questaoRepository.save(questao);

        // Add some alternatives to the question
        com.studora.entity.Alternativa alt1 = new com.studora.entity.Alternativa();
        alt1.setQuestao(questao);
        alt1.setOrdem(1);
        alt1.setTexto("A");
        alt1.setCorreta(true);
        alt1 = alternativaRepository.save(alt1);
        questao.getAlternativas().add(alt1);

        com.studora.entity.Alternativa alt2 = new com.studora.entity.Alternativa();
        alt2.setQuestao(questao);
        alt2.setOrdem(2);
        alt2.setTexto("B");
        alt2.setCorreta(false);
        alt2 = alternativaRepository.save(alt2);
        questao.getAlternativas().add(alt2);

        // Verify the question and alternatives exist before deletion
        mockMvc
                .perform(get("/api/v1/questoes/{id}", questao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Questao to Delete"))
                .andExpect(jsonPath("$.alternativas.length()").value(2));

        mockMvc
                .perform(delete("/api/v1/questoes/{id}", questao.getId()))
                .andExpect(status().isNoContent());

        mockMvc
                .perform(get("/api/v1/questoes/{id}", questao.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetRandomQuestao() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Random Test");
        questao.setAutoral(true);
        questao = questaoRepository.save(questao);

        mockMvc
                .perform(get("/api/v1/questoes/random").param("includeAutoral", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Random Test"));
    }

    @Test
    void testGetRandomQuestao_FiltersDesatualizada() throws Exception {
        Questao qDesat = new Questao();
        qDesat.setEnunciado("Desatualizada");
        qDesat.setAutoral(true);
        qDesat.setDesatualizada(true);
        questaoRepository.save(qDesat);

        // Should return 404 because the only question is desatualizada and we force it
        // to false
        mockMvc
                .perform(get("/api/v1/questoes/random"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetRandomQuestao_DefaultsAnuladaToFalse() throws Exception {
        Questao qAnulada = new Questao();
        qAnulada.setEnunciado("Anulada");
        qAnulada.setAutoral(true);
        qAnulada.setAnulada(true);
        questaoRepository.save(qAnulada);

        // Should return 404 because we default anulada to false and only have an
        // anulada question
        mockMvc
                .perform(get("/api/v1/questoes/random").param("includeAutoral", "true"))
                .andExpect(status().isNotFound());

        // Should return the question if explicitly asked for anulada
        mockMvc
                .perform(get("/api/v1/questoes/random").param("anulada", "true").param("includeAutoral", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Anulada"));
    }

    @Test
    void testGetRandomQuestao_FiltersRecentlyAnswered() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Recent");
        questao.setAutoral(true);
        questao = questaoRepository.save(questao);

        Alternativa alt = new Alternativa();
        alt.setQuestao(questao);
        alt.setOrdem(1);
        alt.setTexto("A");
        alt.setCorreta(true);
        alt = alternativaRepository.save(alt);

        Resposta resp = new Resposta(questao, alt);
        resp = respostaRepository.save(resp);

        // Force recent date natively to match the specification's string comparison
        // exactly
        String nowStr = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        entityManager.createNativeQuery("UPDATE resposta SET created_at = '" + nowStr + "' WHERE id = " + resp.getId())
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        // Should return 404 because the only question was answered today
        mockMvc
                .perform(get("/api/v1/questoes/random").param("includeAutoral", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetRandomQuestao_IncludesOldAnswers() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Old Answer");
        questao.setAutoral(true);
        questao = questaoRepository.save(questao);

        Alternativa alt = new Alternativa();
        alt.setQuestao(questao);
        alt.setOrdem(1);
        alt.setTexto("A");
        alt.setCorreta(true);
        alt = alternativaRepository.save(alt);

        Resposta resp = new Resposta(questao, alt);
        resp = respostaRepository.save(resp);

        // Use native update to bypass JPA Auditing listeners
        String oldDateStr = java.time.LocalDateTime.now().minusMonths(2)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        entityManager
                .createNativeQuery("UPDATE resposta SET created_at = '" + oldDateStr + "' WHERE id = " + resp.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // Should return 200 because the answer is old (2 months)
        // Gabarito should be HIDDEN because it's not a recent answer
        mockMvc
                .perform(get("/api/v1/questoes/random").param("includeAutoral", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Old Answer"))
                .andExpect(jsonPath("$.alternativas[0].correta").doesNotExist());
    }

    @Test
    void testGetRandomQuestao_NotFound() throws Exception {
        // We are in @Transactional, but we need to ensure the database is empty for
        // this test
        // However, setUp() already populated it.
        // Let's use a filter that won't match anything.
        mockMvc
                .perform(get("/api/v1/questoes/random").param("anulada", "true").param("admin", "true")
                        .param("includeAutoral", "true"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Não foi possível encontrar nenhuma questão com os filtros fornecidos."));
    }

}