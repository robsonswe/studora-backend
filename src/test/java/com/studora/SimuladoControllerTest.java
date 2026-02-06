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
        
        SimuladoGenerationRequest.ItemSelection item = new SimuladoGenerationRequest.ItemSelection();
        item.setId(1L);
        item.setQuantidade(20);
        request.setDisciplinas(List.of(item));

        mockMvc.perform(post("/api/v1/simulados/gerar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Simulado Teste"))
                .andExpect(jsonPath("$.questoes").doesNotExist()); // Now hidden in Summary view
    }

    @Test
    void testSimuladoLifecycle() throws Exception {
        // 1. Generate
        Simulado simulado = new Simulado();
        simulado.setNome("Lifecycle Test");
        simulado = simuladoRepository.save(simulado);
        Long id = simulado.getId();

        // 2. Start (PATCH) - Should hide 'correta'
        mockMvc.perform(patch("/api/v1/simulados/{id}/iniciar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startedAt").isNotEmpty())
                .andExpect(jsonPath("$.questoes").exists());
        
        // 3. Finish (PATCH) - Should show 'correta'
        mockMvc.perform(patch("/api/v1/simulados/{id}/finalizar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finishedAt").isNotEmpty());
        
        // 4. Get (Finished)
        mockMvc.perform(get("/api/v1/simulados/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finishedAt").isNotEmpty());

        // 5. Delete
        mockMvc.perform(delete("/api/v1/simulados/{id}", id))
                .andExpect(status().isNoContent());
        
        assertFalse(simuladoRepository.existsById(id));
    }

    @Test
    void testGerarSimulado_ValidationMinQuestions() throws Exception {
        SimuladoGenerationRequest request = new SimuladoGenerationRequest();
        request.setNome("Simulado Invalido");
        
        SimuladoGenerationRequest.ItemSelection item = new SimuladoGenerationRequest.ItemSelection();
        item.setId(1L);
        item.setQuantidade(10); // Less than 20
        request.setDisciplinas(List.of(item));

        mockMvc.perform(post("/api/v1/simulados/gerar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("pelo menos 20 questões")));
    }
}
