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
    private QuestaoCargoRepository questaoCargoRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    private Concurso concurso;
    private Subtema subtema;
    private ConcursoCargo concursoCargo;

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
            new Concurso(instituicao, banca, 2023, 1)
        );

        Disciplina disciplina = disciplinaRepository.save(
            new Disciplina("Disciplina Q Test")
        );
        Tema tema = temaRepository.save(new Tema(disciplina, "Tema Q Test"));
        subtema = subtemaRepository.save(new Subtema(tema, "Subtema Q Test"));

        // Create a cargo and associate it with the concurso
        Cargo cargo = new Cargo();
        cargo.setNome("Cargo Test");
        cargo.setNivel(NivelCargo.SUPERIOR);
        cargo.setArea("Área Test");
        cargo = cargoRepository.save(cargo);

        concursoCargo = new ConcursoCargo();
        concursoCargo.setConcurso(concurso);
        concursoCargo.setCargo(cargo);
        concursoCargo = concursoCargoRepository.save(concursoCargo);
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
        questaoCreateRequest.setConcursoId(concurso.getId());
        questaoCreateRequest.setSubtemaIds(Collections.singletonList(subtema.getId()));
        // Add the concursoCargo association
        questaoCreateRequest.setConcursoCargoIds(Collections.singletonList(concursoCargo.getId()));
        // Add alternativas to comply with validation
        questaoCreateRequest.setAlternativas(Arrays.asList(alt1, alt2));

        mockMvc
            .perform(
                post("/api/v1/questoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(questaoCreateRequest))
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
            .andExpect(jsonPath("$.alternativas[0].texto").value("Brasília"))
            .andExpect(jsonPath("$.alternativas[1].texto").value("São Paulo"));
    }

    @Test
    void testGetQuestaoById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/v1/questoes/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllQuestoes() throws Exception {
        Questao questao1 = new Questao();
        questao1.setEnunciado("Enunciado 1");
        questao1.setConcurso(concurso);
        questao1 = questaoRepository.save(questao1);

        Questao questao2 = new Questao();
        questao2.setEnunciado("Enunciado 2");
        questao2.setConcurso(concurso);
        questao2 = questaoRepository.save(questao2);

        // Default sort should be id DESC
        mockMvc
            .perform(get("/api/v1/questoes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(questao2.getId()))
            .andExpect(jsonPath("$.content[1].id").value(questao1.getId()));
    }

    @Test
    void testGetQuestoesByConcursoId() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Questao Concurso");
        questao.setConcurso(concurso);
        questaoRepository.save(questao);

        mockMvc
            .perform(
                get("/api/v1/questoes").param("concursoId", concurso.getId().toString())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].enunciado").value("Questao Concurso"));
    }

    @Test
    void testGetQuestoesBySubtemaId() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Questao Subtema");
        questao.setConcurso(concurso);
        questao.setSubtemas(new java.util.LinkedHashSet<>(Collections.singletonList(subtema)));
        questaoRepository.save(questao);

        mockMvc
            .perform(get("/api/v1/questoes").param("subtemaId", subtema.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].enunciado").value("Questao Subtema"));
    }

    @Test
    void testGetQuestoesAnuladas() throws Exception {
        Questao qValid = new Questao(concurso, "Valida");
        qValid.setAnulada(false);
        questaoRepository.save(qValid);

        Questao qAnulada = new Questao(concurso, "Anulada");
        qAnulada.setAnulada(true);
        questaoRepository.save(qAnulada);

        mockMvc
            .perform(get("/api/v1/questoes").param("anulada", "true"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(1)
                )
            )
            .andExpect(jsonPath("$.content[?(@.anulada == false)]").doesNotExist());
    }

    @Test
    void testGetQuestoesMultiFilter() throws Exception {
        Questao qTarget = new Questao(concurso, "Target");
        qTarget.setAnulada(true);
        qTarget.setSubtemas(new java.util.LinkedHashSet<>(Collections.singletonList(subtema)));
        questaoRepository.save(qTarget);

        Questao qOther = new Questao(concurso, "Other");
        qOther.setAnulada(false);
        questaoRepository.save(qOther);

        mockMvc
            .perform(get("/api/v1/questoes")
                .param("concursoId", concurso.getId().toString())
                .param("subtemaId", subtema.getId().toString())
                .param("anulada", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].enunciado").value("Target"));
    }

    @Test
    void testUpdateQuestao() throws Exception {
        // First create a question with cargo association
        Questao questao = new Questao();
        questao.setEnunciado("Old Enunciado");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        // Create a QuestaoCargo association to ensure the question has at least one cargo
        QuestaoCargo questaoCargo = new QuestaoCargo();
        questaoCargo.setQuestao(questao);
        questaoCargo.setConcursoCargo(concursoCargo);
        // Save the association directly to the database
        // This ensures the question has at least one cargo association
        // The update method will preserve existing associations if not explicitly changed
        // Create a QuestaoCargoDto to add the cargo association
        com.studora.dto.questao.QuestaoCargoDto questaoCargoDto = new com.studora.dto.questao.QuestaoCargoDto();
        questaoCargoDto.setQuestaoId(questao.getId());
        questaoCargoDto.setConcursoCargoId(concursoCargo.getId());

        mockMvc
            .perform(
                post("/api/v1/questoes/{id}/cargos", questao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(questaoCargoDto))
            )
            .andExpect(status().isCreated());

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
        updatedRequest.setConcursoId(concurso.getId());
        updatedRequest.setAnulada(true);
        // Maintain the cargo association
        updatedRequest.setConcursoCargoIds(Collections.singletonList(concursoCargo.getId()));
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

        mockMvc
            .perform(
                put("/api/v1/questoes/{id}", questao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(updatedRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enunciado").value("New Enunciado"))
            .andExpect(jsonPath("$.anulada").value(true))
            .andExpect(jsonPath("$.alternativas.length()").value(2));
    }

    @Test
    void testDeleteQuestao() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Questao to Delete");
        questao.setConcurso(concurso);
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
    void testToggleDesatualizada() throws Exception {
        Questao questao = new Questao();
        questao.setEnunciado("Toggle Test");
        questao.setConcurso(concurso);
        questao.setDesatualizada(false);
        questao = questaoRepository.save(questao);

        mockMvc
            .perform(patch("/api/v1/questoes/{id}/desatualizada", questao.getId()))
            .andExpect(status().isNoContent());

        Questao updated = questaoRepository.findById(questao.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(updated.getDesatualizada());
    }

    @Test
    void testQuestaoCargoManagement() throws Exception {
        // Setup question
        Questao questao = new Questao();
        questao.setEnunciado("Cargo Mgmt Test");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        // Setup a second cargo
        Cargo cargo2 = new Cargo();
        cargo2.setNome("Cargo 2");
        cargo2.setNivel(NivelCargo.MEDIO);
        cargo2.setArea("ADM");
        cargo2 = cargoRepository.save(cargo2);

        ConcursoCargo concursoCargo2 = new ConcursoCargo();
        concursoCargo2.setConcurso(concurso);
        concursoCargo2.setCargo(cargo2);
        concursoCargo2 = concursoCargoRepository.save(concursoCargo2);

        // 1. Add two Cargo associations (POST)
        com.studora.dto.request.QuestaoCargoCreateRequest addRequest1 = new com.studora.dto.request.QuestaoCargoCreateRequest();
        addRequest1.setConcursoCargoId(concursoCargo.getId());
        mockMvc.perform(post("/api/v1/questoes/{id}/cargos", questao.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(addRequest1)))
                .andExpect(status().isCreated());

        com.studora.dto.request.QuestaoCargoCreateRequest addRequest2 = new com.studora.dto.request.QuestaoCargoCreateRequest();
        addRequest2.setConcursoCargoId(concursoCargo2.getId());
        mockMvc.perform(post("/api/v1/questoes/{id}/cargos", questao.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(addRequest2)))
                .andExpect(status().isCreated());

        // 2. Get Cargos (GET)
        mockMvc
            .perform(get("/api/v1/questoes/{id}/cargos", questao.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        // 3. Remove one Cargo (DELETE) - Should succeed because one remains
        mockMvc
            .perform(delete("/api/v1/questoes/{questaoId}/cargos/{concursoCargoId}", questao.getId(), concursoCargo.getId()))
            .andExpect(status().isNoContent());

        // 4. Verify remaining
        mockMvc
            .perform(get("/api/v1/questoes/{id}/cargos", questao.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].concursoCargoId").value(concursoCargo2.getId()));
    }

    @Test
    void testRemoveCargoFromQuestao_UnprocessableEntity() throws Exception {
        // Setup question
        Questao questao = new Questao();
        questao.setEnunciado("One Cargo Test");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        QuestaoCargo qc = new QuestaoCargo();
        qc.setQuestao(questao);
        qc.setConcursoCargo(concursoCargo);
        questao.addQuestaoCargo(qc); // Add to parent collection
        questaoCargoRepository.save(qc);
        questaoRepository.save(questao); // Update parent

        // Try to remove the last cargo - This depends on business logic in QuestaoService
        // If there's a constraint that a question must have at least one cargo, this should fail.
        mockMvc
            .perform(delete("/api/v1/questoes/{questaoId}/cargos/{concursoCargoId}", questao.getId(), concursoCargo.getId()))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testAddCargoToQuestao() throws Exception {
        // First create a question
        Questao questao = new Questao();
        questao.setEnunciado("Questão para cargo");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        // Create cargo association request
        com.studora.dto.request.QuestaoCargoCreateRequest cargoRequest = new com.studora.dto.request.QuestaoCargoCreateRequest();
        cargoRequest.setConcursoCargoId(concursoCargo.getId());

        mockMvc
            .perform(
                post("/api/v1/questoes/{id}/cargos", questao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(cargoRequest))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.concursoCargoId").value(concursoCargo.getId()))
            .andExpect(jsonPath("$.questaoId").value(questao.getId()));
    }

    @Test
    void testAddCargoToQuestao_NonExistentQuestao() throws Exception {
        // Create cargo association request
        com.studora.dto.request.QuestaoCargoCreateRequest cargoRequest = new com.studora.dto.request.QuestaoCargoCreateRequest();
        cargoRequest.setConcursoCargoId(concursoCargo.getId());

        mockMvc
            .perform(
                post("/api/v1/questoes/{id}/cargos", 99999L) // Non-existent questao ID
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(cargoRequest))
            )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Não foi possível encontrar Questão com ID: '99999'"));
    }

    @Test
    void testAddCargoToQuestao_NonExistentConcursoCargo() throws Exception {
        // First create a question
        Questao questao = new Questao();
        questao.setEnunciado("Questão para cargo");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        // Create cargo association request with non-existent concursoCargo ID
        com.studora.dto.request.QuestaoCargoCreateRequest cargoRequest = new com.studora.dto.request.QuestaoCargoCreateRequest();
        cargoRequest.setConcursoCargoId(99999L); // Non-existent concursoCargo ID

        mockMvc
            .perform(
                post("/api/v1/questoes/{id}/cargos", questao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(cargoRequest))
            )
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Não foi possível encontrar ConcursoCargo com ID: '99999'"));
    }
}
