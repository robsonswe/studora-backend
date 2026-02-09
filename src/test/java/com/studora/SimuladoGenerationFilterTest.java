package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.SimuladoGenerationRequest;
import com.studora.entity.*;
import com.studora.repository.*;
import com.studora.util.TestUtil;
import jakarta.persistence.EntityManager;
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
class SimuladoGenerationFilterTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private QuestaoRepository questaoRepository;
    @Autowired private ConcursoRepository concursoRepository;
    @Autowired private InstituicaoRepository instituicaoRepository;
    @Autowired private BancaRepository bancaRepository;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private SubtemaRepository subtemaRepository;
    @Autowired private RespostaRepository respostaRepository;
    @Autowired private AlternativaRepository alternativaRepository;
    @Autowired private EntityManager entityManager;

    private Subtema subtema;

    @BeforeEach
    void setUp() {
        Instituicao inst = new Instituicao(); inst.setNome("Inst F"); inst.setArea("A"); instituicaoRepository.save(inst);
        Banca banca = new Banca(); banca.setNome("Banca F"); bancaRepository.save(banca);
        Concurso conc = new Concurso(inst, banca, 2023, 1); concursoRepository.save(conc);
        
        Disciplina disc = new Disciplina(); disc.setNome("Direito F"); disc = disciplinaRepository.save(disc);
        Tema tema = new Tema(); tema.setNome("Tema F"); tema.setDisciplina(disc); tema = temaRepository.save(tema);
        subtema = new Subtema(); subtema.setNome("Sub F"); subtema.setTema(tema); subtema = subtemaRepository.save(subtema);

        for (int i = 1; i <= 30; i++) {
            Questao q = new Questao();
            q.setEnunciado("QF" + i);
            q.setConcurso(conc);
            q.getSubtemas().add(subtema);
            
            Alternativa alt = new Alternativa();
            alt.setQuestao(q); alt.setOrdem(1); alt.setTexto("A"); alt.setCorreta(true);
            q.getAlternativas().add(alt);
            
            questaoRepository.save(q);
        }
    }

    @Test
    void testGerarSimulado_Respects30DayFilterByDefault() throws Exception {
        // Answer 15 questions today
        List<Questao> all = questaoRepository.findAll();
        for (int i = 0; i < 15; i++) {
            Questao q = all.get(i);
            Resposta r = new Resposta(q, q.getAlternativas().iterator().next());
            respostaRepository.save(r);
        }
        entityManager.flush();
        entityManager.clear();

        // Try to generate a simulado with 20 questions. 
        // 30 total - 15 recent = 15 available. Should fail since 20 requested > 15 available.
        SimuladoGenerationRequest request = new SimuladoGenerationRequest();
        request.setNome("Fail Simulado");
        request.setIgnorarRespondidas(false); // Default
        
        SimuladoGenerationRequest.ItemSelection item = new SimuladoGenerationRequest.ItemSelection();
        item.setId(subtema.getId());
        item.setQuantidade(20);
        request.setSubtemas(List.of(item));

        mockMvc.perform(post("/api/v1/simulados/gerar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testGerarSimulado_IncludesOldAnswersByDefault() throws Exception {
        // Answer 15 questions 2 months ago
        List<Questao> all = questaoRepository.findAll();
        String oldDate = java.time.LocalDateTime.now().minusMonths(2)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        for (int i = 0; i < 15; i++) {
            Questao q = all.get(i);
            Resposta r = new Resposta(q, q.getAlternativas().iterator().next());
            r = respostaRepository.save(r);
            entityManager.createNativeQuery("UPDATE resposta SET created_at = '" + oldDate + "' WHERE id = " + r.getId())
                .executeUpdate();
        }
        entityManager.flush();
        entityManager.clear();

        // 30 total - 0 recent = 30 available. 20 requested should pass.
        SimuladoGenerationRequest request = new SimuladoGenerationRequest();
        request.setNome("Pass Simulado");
        request.setIgnorarRespondidas(false);
        
        SimuladoGenerationRequest.ItemSelection item = new SimuladoGenerationRequest.ItemSelection();
        item.setId(subtema.getId());
        item.setQuantidade(20);
        request.setSubtemas(List.of(item));

        mockMvc.perform(post("/api/v1/simulados/gerar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGerarSimulado_IgnorarRespondidasTrue_ExcludesAll() throws Exception {
        // Answer 15 questions 2 months ago
        List<Questao> all = questaoRepository.findAll();
        for (int i = 0; i < 15; i++) {
            Questao q = all.get(i);
            Resposta r = new Resposta(q, q.getAlternativas().iterator().next());
            respostaRepository.save(r);
        }
        entityManager.flush();
        entityManager.clear();

        // ignorarRespondidas = true. 
        // 30 total - 15 any answered = 15 available. 20 requested should fail.
        SimuladoGenerationRequest request = new SimuladoGenerationRequest();
        request.setNome("Ignore All Fail");
        request.setIgnorarRespondidas(true);
        
        SimuladoGenerationRequest.ItemSelection item = new SimuladoGenerationRequest.ItemSelection();
        item.setId(subtema.getId());
        item.setQuantidade(20);
        request.setSubtemas(List.of(item));

        mockMvc.perform(post("/api/v1/simulados/gerar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.asJsonString(request)))
                .andExpect(status().isUnprocessableEntity());
    }
}
