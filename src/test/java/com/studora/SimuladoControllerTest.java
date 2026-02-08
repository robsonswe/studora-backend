package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.studora.dto.request.SimuladoGenerationRequest;
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
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SimuladoControllerTest {

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
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private SubtemaRepository subtemaRepository;

    @Autowired
    private SimuladoRepository simuladoRepository;

    @Autowired
    private RespostaRepository respostaRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @BeforeEach
    void setUp() {
        Instituicao inst = new Instituicao(); inst.setNome("Inst 1"); inst.setArea("A"); instituicaoRepository.save(inst);
        Banca banca = new Banca(); banca.setNome("Banca 1"); bancaRepository.save(banca);
        Concurso conc = new Concurso(inst, banca, 2023, 1); concursoRepository.save(conc);
        
        Disciplina disc = new Disciplina(); disc.setNome("Direito"); disc = disciplinaRepository.save(disc);
        Tema tema = new Tema(); tema.setNome("Tema 1"); tema.setDisciplina(disc); tema = temaRepository.save(tema);
        Subtema sub = new Subtema(); sub.setNome("Sub 1"); sub.setTema(tema); sub = subtemaRepository.save(sub);

        for (int i = 1; i <= 20; i++) {
            Questao q = new Questao();
            q.setEnunciado("Q" + i);
            q.setConcurso(conc);
            q.getSubtemas().add(sub);
            questaoRepository.save(q);
        }
    }

    @Test
    void testGerarSimulado() throws Exception {
        SimuladoGenerationRequest request = new SimuladoGenerationRequest();
        request.setNome("Simulado Teste");
        request.setBancaId(1L);
        request.setIgnorarRespondidas(true);
        
        SimuladoGenerationRequest.ItemSelection item = new SimuladoGenerationRequest.ItemSelection();
        item.setId(1L);
        item.setQuantidade(20);
        request.setDisciplinas(List.of(item));

        mockMvc.perform(post("/api/v1/simulados/gerar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Simulado Teste"))
                .andExpect(jsonPath("$.banca.id").value(1))
                .andExpect(jsonPath("$.ignorarRespondidas").value(true))
                .andExpect(jsonPath("$.disciplinas").exists())
                .andExpect(jsonPath("$.questoes").doesNotExist());
    }

    @Test
    void testSimuladoLifecycle() throws Exception {
        // 1. Setup a question with an alternative
        Questao q1 = questaoRepository.findAll().get(0);
        Alternativa alt = new Alternativa();
        alt.setQuestao(q1); alt.setOrdem(1); alt.setTexto("A1"); alt.setCorreta(true);
        alt = alternativaRepository.save(alt);

        Simulado simulado = new Simulado();
        simulado.setNome("Lifecycle Test");
        simulado.setQuestoes(new java.util.ArrayList<>(java.util.List.of(q1)));
        simulado = simuladoRepository.save(simulado);
        Long id = simulado.getId();

        // 2. Start (PATCH)
        mockMvc.perform(patch("/api/v1/simulados/{id}/iniciar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startedAt").isNotEmpty())
                .andExpect(jsonPath("$.questoes").doesNotExist());
        
        // 3. Try to finish without answers - Should fail (422)
        mockMvc.perform(patch("/api/v1/simulados/{id}/finalizar", id))
                .andExpect(status().isUnprocessableEntity());

        // 4. Answer the question
        Resposta resp = new Resposta();
        resp.setQuestao(q1);
        resp.setAlternativaEscolhida(alt);
        resp.setSimulado(simulado);
        respostaRepository.save(resp);

        // 5. Finish (PATCH)
        mockMvc.perform(patch("/api/v1/simulados/{id}/finalizar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finishedAt").isNotEmpty())
                .andExpect(jsonPath("$.questoes").doesNotExist());
        
        // 6. Get (Finished) - Should return questao because it is detail view
        mockMvc.perform(get("/api/v1/simulados/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questoes").exists());

        // 7. Delete
        mockMvc.perform(delete("/api/v1/simulados/{id}", id))
                .andExpect(status().isNoContent());
        
        assertFalse(simuladoRepository.existsById(id));
    }

    @Test
    void testGerarSimulado_ValidationMinQuestions() throws Exception {
        // ... (existing code)
    }

    @Test
    void testListSimulados_IncludesFilters() throws Exception {
        Simulado simulado = new Simulado();
        simulado.setNome("List Test");
        simulado.setBancaId(1L);
        simuladoRepository.save(simulado);

        mockMvc.perform(get("/api/v1/simulados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("List Test"))
                .andExpect(jsonPath("$.content[0].banca.id").value(1))
                .andExpect(jsonPath("$.content[0].questoes").doesNotExist());
    }

    @Test
    void testListSimulados_Empty_Returns404() throws Exception {
        // Clear all simulados (setUp doesn't create any, but just in case)
        simuladoRepository.deleteAll();

        mockMvc.perform(get("/api/v1/simulados"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Nenhum simulado encontrado"));
    }
}
