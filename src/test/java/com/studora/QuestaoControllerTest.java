package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.QuestaoDto;
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
        // Create alternativas
        com.studora.dto.AlternativaDto alt1 = new com.studora.dto.AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Brasília");
        alt1.setCorreta(true);
        alt1.setJustificativa("Capital oficial do Brasil");

        com.studora.dto.AlternativaDto alt2 = new com.studora.dto.AlternativaDto();
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
                post("/api/questoes")
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
    void testGetQuestoesAnuladas() throws Exception {
        Questao qValid = new Questao(concurso, "Valida");
        qValid.setAnulada(false);
        questaoRepository.save(qValid);

        Questao qAnulada = new Questao(concurso, "Anulada");
        qAnulada.setAnulada(true);
        questaoRepository.save(qAnulada);

        mockMvc
            .perform(get("/api/questoes/anuladas"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(1)
                )
            )
            .andExpect(jsonPath("$[?(@.anulada == false)]").doesNotExist());
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
        com.studora.dto.QuestaoCargoDto questaoCargoDto = new com.studora.dto.QuestaoCargoDto();
        questaoCargoDto.setQuestaoId(questao.getId());
        questaoCargoDto.setConcursoCargoId(concursoCargo.getId());

        mockMvc
            .perform(
                post("/api/questoes/{id}/cargos", questao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(questaoCargoDto))
            )
            .andExpect(status().isCreated());

        QuestaoUpdateRequest updatedRequest = new QuestaoUpdateRequest();
        updatedRequest.setEnunciado("New Enunciado");
        updatedRequest.setConcursoId(concurso.getId());
        updatedRequest.setAnulada(true);
        // Maintain the cargo association
        updatedRequest.setConcursoCargoIds(Collections.singletonList(concursoCargo.getId()));

        mockMvc
            .perform(
                put("/api/questoes/{id}", questao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(updatedRequest))
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

    @Test
    void testAddAlternativaToQuestao() throws Exception {
        // First create a question
        Questao questao = new Questao();
        questao.setEnunciado("Questão para alternativas");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        // Create a QuestaoCargo association to ensure the question has at least one cargo
        QuestaoCargo questaoCargo = new QuestaoCargo();
        questaoCargo.setQuestao(questao);
        questaoCargo.setConcursoCargo(concursoCargo);
        questaoCargo = questaoCargoRepository.save(questaoCargo);

        // Create alternativa request
        com.studora.dto.request.AlternativaCreateRequest alternativaRequest = new com.studora.dto.request.AlternativaCreateRequest();
        alternativaRequest.setOrdem(1);
        alternativaRequest.setTexto("Nova alternativa");
        alternativaRequest.setCorreta(false);
        alternativaRequest.setJustificativa("Justificativa da nova alternativa");

        mockMvc
            .perform(
                post("/api/questoes/{id}/alternativas", questao.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(alternativaRequest))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.texto").value("Nova alternativa"))
            .andExpect(jsonPath("$.ordem").value(1));
    }

    @Test
    void testUpdateAlternativaFromQuestao() throws Exception {
        // First create a question
        Questao questao = new Questao();
        questao.setEnunciado("Questão para alternativas");
        questao.setConcurso(concurso);
        questao = questaoRepository.save(questao);

        // Create a QuestaoCargo association
        QuestaoCargo questaoCargo = new QuestaoCargo();
        questaoCargo.setQuestao(questao);
        questaoCargo.setConcursoCargo(concursoCargo);
        questaoCargo = questaoCargoRepository.save(questaoCargo);

        // Create an alternative
        com.studora.entity.Alternativa alternativa = new com.studora.entity.Alternativa();
        alternativa.setQuestao(questao);
        alternativa.setOrdem(1);
        alternativa.setTexto("Texto antigo");
        alternativa.setCorreta(false);
        alternativa.setJustificativa("Justificativa antiga");
        alternativa = alternativaRepository.save(alternativa);

        // Create alternativa update request
        com.studora.dto.request.AlternativaUpdateRequest alternativaUpdateRequest = new com.studora.dto.request.AlternativaUpdateRequest();
        alternativaUpdateRequest.setOrdem(2); // Change order
        alternativaUpdateRequest.setTexto("Texto novo"); // Change text
        alternativaUpdateRequest.setCorreta(true); // Change correctness
        alternativaUpdateRequest.setJustificativa("Justificativa nova"); // Change justification

        mockMvc
            .perform(
                put("/api/questoes/{questaoId}/alternativas/{alternativaId}", questao.getId(), alternativa.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(alternativaUpdateRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.texto").value("Texto novo"))
            .andExpect(jsonPath("$.ordem").value(2));
            // Note: correta field is intentionally not returned in responses to hide the correct answer
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
                post("/api/questoes/{id}/cargos", questao.getId())
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
                post("/api/questoes/{id}/cargos", 99999L) // Non-existent questao ID
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
                post("/api/questoes/{id}/cargos", questao.getId())
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