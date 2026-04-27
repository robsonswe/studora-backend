package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.AlternativaCreateRequest;
import com.studora.dto.request.AlternativaUpdateRequest;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Integration tests for autoral questions (Phase 7.1 of IMPLEMENTATION_PLAN_AUTORAL.md).
 * Tests creation, type immutability, filtering, and random endpoint behavior.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QuestaoAutoralControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private SubtemaRepository subtemaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private BancaRepository bancaRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private ConcursoCargoRepository concursoCargoRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Subtema subtema;
    private Concurso concurso;
    private Cargo cargo;
    private ConcursoCargo concursoCargo;

    @BeforeEach
    void setUp() {
        Disciplina disciplina = disciplinaRepository.save(new Disciplina("Disciplina Autoral Test"));
        Tema tema = temaRepository.save(new Tema(disciplina, "Tema Autoral Test"));
        subtema = subtemaRepository.save(new Subtema(tema, "Subtema Autoral Test"));

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Autoral Test");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Autoral Test");
        banca = bancaRepository.save(banca);

        concurso = concursoRepository.save(new Concurso(instituicao, banca, 2024, 1));

        cargo = new Cargo();
        cargo.setNome("Cargo Autoral Test");
        cargo.setNivel(NivelCargo.SUPERIOR);
        cargo.setArea("Área Test");
        cargo = cargoRepository.save(cargo);

        concursoCargo = new ConcursoCargo();
        concursoCargo.setConcurso(concurso);
        concursoCargo.setCargo(cargo);
        concursoCargoRepository.save(concursoCargo);
        concurso.addConcursoCargo(concursoCargo);
    }

    // ========== POST /questoes — autoral creation ==========

    @Test
    void createAutoralQuestion_success() throws Exception {
        AlternativaCreateRequest alt1 = buildAltRequest(1, "A", true);
        AlternativaCreateRequest alt2 = buildAltRequest(2, "B", false);

        QuestaoCreateRequest request = new QuestaoCreateRequest();
        request.setEnunciado("Questão autoral de teste");
        request.setAutoral(true);
        request.setSubtemaIds(Collections.singletonList(subtema.getId()));
        request.setAlternativas(Arrays.asList(alt1, alt2));
        // No concursoId, no cargos — autoral doesn't need them

        // POST returns PostResponseDto with id and message
        String postResponse = mockMvc.perform(post("/api/v1/questoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.message").exists())
            .andReturn().getResponse().getContentAsString();

        Long createdId = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(postResponse).get("id").asLong();

        // Verify autoral fields via GET (admin=true to see full detail)
        mockMvc.perform(get("/api/v1/questoes/{id}", createdId).param("admin", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.autoral").value(true))
            .andExpect(jsonPath("$.concurso").doesNotExist())
            .andExpect(jsonPath("$.cargos").isArray())
            .andExpect(jsonPath("$.cargos").isEmpty());
    }

    @Test
    void createStandardQuestionWithoutConcurso_fails() throws Exception {
        AlternativaCreateRequest alt1 = buildAltRequest(1, "A", true);
        AlternativaCreateRequest alt2 = buildAltRequest(2, "B", false);

        QuestaoCreateRequest request = new QuestaoCreateRequest();
        request.setEnunciado("Questão padrão sem concurso");
        request.setAutoral(false); // default
        request.setSubtemaIds(Collections.singletonList(subtema.getId()));
        request.setAlternativas(Arrays.asList(alt1, alt2));
        // No concursoId, no cargos

        mockMvc.perform(post("/api/v1/questoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createAutoralQuestionWithTooFewAlternatives_fails() throws Exception {
        AlternativaCreateRequest alt1 = buildAltRequest(1, "A", true);

        QuestaoCreateRequest request = new QuestaoCreateRequest();
        request.setEnunciado("Questão autoral com poucas alternativas");
        request.setAutoral(true);
        request.setSubtemaIds(Collections.singletonList(subtema.getId()));
        request.setAlternativas(Collections.singletonList(alt1));

        // @Size triggers Bean Validation → 400 (not service-level ValidationException → 422)
        mockMvc.perform(post("/api/v1/questoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createAutoralQuestionWithoutSubtemas_fails() throws Exception {
        AlternativaCreateRequest alt1 = buildAltRequest(1, "A", true);
        AlternativaCreateRequest alt2 = buildAltRequest(2, "B", false);

        QuestaoCreateRequest request = new QuestaoCreateRequest();
        request.setEnunciado("Questão autoral sem subtema");
        request.setAutoral(true);
        request.setSubtemaIds(Collections.emptyList()); // no subtemas
        request.setAlternativas(Arrays.asList(alt1, alt2));

        mockMvc.perform(post("/api/v1/questoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("pelo menos um subtema")));
    }

    @Test
    void createStandardQuestionWithoutSubtemas_fails() throws Exception {
        AlternativaCreateRequest alt1 = buildAltRequest(1, "A", true);
        AlternativaCreateRequest alt2 = buildAltRequest(2, "B", false);

        QuestaoCreateRequest request = new QuestaoCreateRequest();
        request.setEnunciado("Questão padrão sem subtema");
        request.setAutoral(false);
        request.setConcursoId(concurso.getId());
        request.setSubtemaIds(null); // no subtemas
        request.setCargos(Collections.singletonList(cargo.getId()));
        request.setAlternativas(Arrays.asList(alt1, alt2));

        mockMvc.perform(post("/api/v1/questoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("pelo menos um subtema")));
    }

    @Test
    void createAutoralQuestionWithTwoCorrectAlternatives_fails() throws Exception {
        AlternativaCreateRequest alt1 = buildAltRequest(1, "A", true);
        AlternativaCreateRequest alt2 = buildAltRequest(2, "B", true);

        QuestaoCreateRequest request = new QuestaoCreateRequest();
        request.setEnunciado("Questão autoral com duas corretas");
        request.setAutoral(true);
        request.setAnulada(false);
        request.setSubtemaIds(Collections.singletonList(subtema.getId()));
        request.setAlternativas(Arrays.asList(alt1, alt2));

        mockMvc.perform(post("/api/v1/questoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
            .andExpect(status().isUnprocessableEntity());
    }

    // ========== PUT /questoes/{id} — type immutability ==========

    @Test
    void updateAutoralToStandard_fails() throws Exception {
        Questao autoral = createAutoralQuestao("Questão autoral original");
        Long altId = getFirstAlternativaId(autoral);

        AlternativaUpdateRequest alt1 = buildUpdateAlt(altId, "A atualizada", true);
        AlternativaUpdateRequest alt2 = buildUpdateAlt(2L, "B atualizada", false);

        QuestaoUpdateRequest request = new QuestaoUpdateRequest();
        request.setEnunciado("Questão autoral atualizada");
        request.setAutoral(false); // attempt to change type
        request.setSubtemaIds(Collections.singletonList(subtema.getId()));
        request.setAlternativas(List.of(alt1, alt2));

        mockMvc.perform(put("/api/v1/questoes/{id}", autoral.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("não pode ser alterado")));
    }

    @Test
    void updateStandardToAutoral_fails() throws Exception {
        Questao standard = createStandardQuestao("Questão padrão original");
        Long altId = getFirstAlternativaId(standard);

        AlternativaUpdateRequest alt1 = buildUpdateAlt(altId, "A atualizada", true);
        AlternativaUpdateRequest alt2 = buildUpdateAlt(2L, "B atualizada", false);

        QuestaoUpdateRequest request = new QuestaoUpdateRequest();
        request.setEnunciado("Questão padrão atualizada");
        request.setAutoral(true); // attempt to change type
        request.setSubtemaIds(Collections.singletonList(subtema.getId()));
        request.setAlternativas(List.of(alt1, alt2));

        mockMvc.perform(put("/api/v1/questoes/{id}", standard.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("não pode ser alterado")));
    }

    @Test
    void updateAutoralQuestionWithUnchangedAutoral_success() throws Exception {
        Questao autoral = createAutoralQuestao("Questão autoral original");
        Long altId = getFirstAlternativaId(autoral);

        AlternativaUpdateRequest alt1 = buildUpdateAlt(altId, "A atualizada", true);
        AlternativaUpdateRequest alt2 = buildUpdateAlt(2L, "B atualizada", false);

        QuestaoUpdateRequest request = new QuestaoUpdateRequest();
        request.setEnunciado("Questão autoral atualizada");
        request.setAutoral(true); // unchanged
        request.setSubtemaIds(Collections.singletonList(subtema.getId()));
        request.setAlternativas(List.of(alt1, alt2));

        mockMvc.perform(put("/api/v1/questoes/{id}", autoral.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void updateAutoralQuestionWithoutAutoralField_success() throws Exception {
        Questao autoral = createAutoralQuestao("Questão autoral original");
        Long altId = getFirstAlternativaId(autoral);

        AlternativaUpdateRequest alt1 = buildUpdateAlt(altId, "A atualizada", true);
        AlternativaUpdateRequest alt2 = buildUpdateAlt(2L, "B atualizada", false);

        QuestaoUpdateRequest request = new QuestaoUpdateRequest();
        request.setEnunciado("Questão autoral sem campo autoral");
        request.setSubtemaIds(Collections.singletonList(subtema.getId()));
        request.setAlternativas(List.of(alt1, alt2));
        // autoral field not sent — should not throw

        mockMvc.perform(put("/api/v1/questoes/{id}", autoral.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
            .andExpect(status().isOk());
    }

    // ========== GET /questoes — filtering ==========

    @Test
    void filterByAutoralTrue_returnsOnlyAutoral() throws Exception {
        createAutoralQuestao("Autoral 1");
        createAutoralQuestao("Autoral 2");
        createStandardQuestao("Standard 1");

        mockMvc.perform(get("/api/v1/questoes")
                .param("autoral", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].autoral").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(true))));
    }

    @Test
    void filterByAutoralFalse_returnsOnlyStandard() throws Exception {
        createAutoralQuestao("Autoral 1");
        createStandardQuestao("Standard 1");
        createStandardQuestao("Standard 2");

        mockMvc.perform(get("/api/v1/questoes")
                .param("autoral", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[*].autoral").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(false))));
    }

    @Test
    void noAutoralFilter_returnsBothTypes() throws Exception {
        createAutoralQuestao("Autoral 1");
        createStandardQuestao("Standard 1");

        mockMvc.perform(get("/api/v1/questoes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));
    }

    // ========== GET /questoes/random ==========

    @Test
    void randomWithoutIncludeAutoral_excludesAutoral() throws Exception {
        createAutoralQuestao("Autoral única");

        mockMvc.perform(get("/api/v1/questoes/random")
                .param("subtemaId", subtema.getId().toString()))
            .andExpect(status().isNotFound());
    }

    @Test
    void randomWithIncludeAutoral_includesAutoral() throws Exception {
        createAutoralQuestao("Autoral única");

        mockMvc.perform(get("/api/v1/questoes/random")
                .param("subtemaId", subtema.getId().toString())
                .param("includeAutoral", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.autoral").value(true));
    }

    // ========== Helper methods ==========

    private AlternativaCreateRequest buildAltRequest(int ordem, String texto, boolean correta) {
        AlternativaCreateRequest alt = new AlternativaCreateRequest();
        alt.setOrdem(ordem);
        alt.setTexto(texto);
        alt.setCorreta(correta);
        return alt;
    }

    private AlternativaUpdateRequest buildUpdateAlt(Long id, String texto, boolean correta) {
        AlternativaUpdateRequest alt = new AlternativaUpdateRequest();
        alt.setId(id);
        alt.setTexto(texto);
        alt.setCorreta(correta);
        alt.setOrdem(1);
        return alt;
    }

    private Long getFirstAlternativaId(Questao questao) {
        // Use entityManager to fetch with alternatives since the collection is lazy
        Questao withDetails = entityManager.find(Questao.class, questao.getId());
        entityManager.refresh(withDetails);
        // Force load alternatives through a query
        List<Alternativa> alts = entityManager.createQuery(
            "SELECT a FROM Alternativa a WHERE a.questao.id = :qid ORDER BY a.ordem", Alternativa.class)
            .setParameter("qid", questao.getId())
            .getResultList();
        return alts.isEmpty() ? null : alts.get(0).getId();
    }

    private Questao createAutoralQuestao(String enunciado) {
        AlternativaCreateRequest alt1 = buildAltRequest(1, "A", true);
        AlternativaCreateRequest alt2 = buildAltRequest(2, "B", false);

        QuestaoCreateRequest request = new QuestaoCreateRequest();
        request.setEnunciado(enunciado);
        request.setAutoral(true);
        request.setSubtemaIds(Collections.singletonList(subtema.getId()));
        request.setAlternativas(Arrays.asList(alt1, alt2));

        try {
            String response = mockMvc.perform(post("/api/v1/questoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

            Long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asLong();
            return entityManager.find(Questao.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Questao createStandardQuestao(String enunciado) {
        AlternativaCreateRequest alt1 = buildAltRequest(1, "A", true);
        AlternativaCreateRequest alt2 = buildAltRequest(2, "B", false);

        QuestaoCreateRequest request = new QuestaoCreateRequest();
        request.setEnunciado(enunciado);
        request.setAutoral(false);
        request.setConcursoId(concurso.getId());
        request.setSubtemaIds(Collections.singletonList(subtema.getId()));
        request.setCargos(Collections.singletonList(cargo.getId()));
        request.setAlternativas(Arrays.asList(alt1, alt2));

        try {
            String response = mockMvc.perform(post("/api/v1/questoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

            Long id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asLong();
            return entityManager.find(Questao.class, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
