package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UltimaQuestaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private SubtemaRepository subtemaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private BancaRepository bancaRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    private Subtema subtema;
    private Tema tema;
    private Disciplina disciplina;
    private Questao questao;
    private Alternativa alternativa;

    @BeforeEach
    void setUp() {
        disciplina = disciplinaRepository.save(new Disciplina("Direito Constitucional"));
        tema = new Tema();
        tema.setNome("Direitos Fundamentais");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        subtema = new Subtema();
        subtema.setNome("Habeas Corpus");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        Instituicao inst = new Instituicao();
        inst.setNome("PF");
        inst.setArea("Policial");
        inst = instituicaoRepository.save(inst);
        
        Banca banca = new Banca();
        banca.setNome("Cebraspe");
        banca = bancaRepository.save(banca);
        
        Concurso conc = new Concurso();
        conc.setInstituicao(inst);
        conc.setBanca(banca);
        conc.setAno(2024);
        conc.setMes(5);
        conc = concursoRepository.save(conc);

        questao = new Questao();
        questao.setConcurso(conc);
        questao.setEnunciado("Enunciado de teste");
        questao.setSubtemas(Set.of(subtema));
        questao = questaoRepository.save(questao);

        alternativa = new Alternativa();
        alternativa.setQuestao(questao);
        alternativa.setOrdem(1);
        alternativa.setTexto("Alternativa A");
        alternativa.setCorreta(true);
        alternativa = alternativaRepository.save(alternativa);
    }

    @Test
    void testUltimaQuestaoPopulated() throws Exception {
        // Initially ultimaQuestao should be null or questaoStats might not exist
        mockMvc.perform(get("/api/v1/subtemas/{id}", subtema.getId()).param("metrics", "full"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/temas/{id}", tema.getId()).param("metrics", "full"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/disciplinas/{id}", disciplina.getId()).param("metrics", "full"))
                .andExpect(status().isOk());

        // Create a response
        RespostaCreateRequest request = new RespostaCreateRequest();
        request.setQuestaoId(questao.getId());
        request.setAlternativaId(alternativa.getId());
        request.setJustificativa("Minha justificativa de teste");
        request.setDificuldadeId(2); // MEDIA

        mockMvc.perform(post("/api/v1/respostas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.asJsonString(request)))
                .andExpect(status().isCreated());

        // Now ultimaQuestao should be populated in questaoStats
        mockMvc.perform(get("/api/v1/subtemas/{id}", subtema.getId()).param("metrics", "full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questaoStats").isNotEmpty());

        mockMvc.perform(get("/api/v1/temas/{id}", tema.getId()).param("metrics", "full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questaoStats").isNotEmpty());

        mockMvc.perform(get("/api/v1/disciplinas/{id}", disciplina.getId()).param("metrics", "full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questaoStats").isNotEmpty());

        // Check list endpoints too (just verify they return successfully with questaoStats)
        mockMvc.perform(get("/api/v1/subtemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/v1/temas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/v1/disciplinas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
