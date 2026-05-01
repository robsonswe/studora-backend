package com.studora;

import com.studora.entity.*;
import com.studora.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConcursoMetricsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

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
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private SubtemaRepository subtemaRepository;

    @Autowired
    private ProvaRepository provaRepository;

    @Autowired
    private ProvaSecaoRepository provaSecaoRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private RespostaRepository respostaRepository;

    @Test
    void testGetConcursoMetrics_WithResponses_ShouldSucceed() throws Exception {
        // 1. Setup Taxonomy
        Disciplina disc = disciplinaRepository.save(new Disciplina("Direito Constitucional"));
        Tema tema = new Tema();
        tema.setNome("Direitos Fundamentais");
        tema.setDisciplina(disc);
        tema = temaRepository.save(tema);
        Subtema sub = new Subtema();
        sub.setNome("Habeas Corpus");
        sub.setTema(tema);
        sub = subtemaRepository.save(sub);

        // 2. Setup Concurso and Cargo
        Instituicao inst = new Instituicao();
        inst.setNome("TRF");
        inst.setArea("Judiciário");
        inst = instituicaoRepository.save(inst);

        Banca banca = new Banca();
        banca.setNome("FCC");
        banca.setSigla("FCC");
        banca = bancaRepository.save(banca);

        Concurso concurso = concursoRepository.save(new Concurso(inst, banca, 2026, 5));
        
        Cargo cargo = new Cargo();
        cargo.setNome("Analista Judiciário");
        cargo.setNivel(NivelCargo.SUPERIOR);
        cargo.setArea("Judiciária");
        cargo = cargoRepository.save(cargo);
        
        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo);
        cc = concursoCargoRepository.save(cc);
        concurso.getConcursoCargos().add(cc);
        concursoRepository.save(concurso);

        // 3. Setup Prova Hierarchy
        Prova prova = new Prova();
        prova.setConcurso(concurso);
        prova.setNome("Prova Objetiva");
        prova.addCargo(cc);
        prova = provaRepository.save(prova);

        ProvaSecao secao = new ProvaSecao();
        secao.setProva(prova);
        secao.setNome("Conhecimentos Específicos");
        secao.setOrdem(1);
        secao.getSubtemas().add(sub);
        secao = provaSecaoRepository.save(secao);

        // 4. Setup Question and Answer
        Questao q = new Questao();
        q.setEnunciado("Qual o remédio constitucional para liberdade de locomoção?");
        q.getSubtemas().add(sub);
        q = questaoRepository.save(q);

        Alternativa alt = new Alternativa();
        alt.setTexto("Habeas Corpus");
        alt.setCorreta(true);
        alt.setOrdem(1);
        alt.setQuestao(q);
        q.getAlternativas().add(alt);
        q = questaoRepository.save(q);
        entityManager.flush();

        // Refetch questao to ensure persistent alternatives are loaded
        q = questaoRepository.findById(q.getId()).orElseThrow();
        Alternativa persistentAlt = q.getAlternativas().iterator().next();

        // Link Question to Prova Secao
        QuestaoProvaSecao qps = new QuestaoProvaSecao();
        qps.setQuestao(q);
        qps.setProvaSecao(secao);
        qps.setNumeroQuestao(1);
        q.getSecoes().add(qps);
        questaoRepository.save(q);

        // Add a Response
        Resposta resp = new Resposta();
        resp.setQuestao(q);
        resp.setAlternativaEscolhida(persistentAlt);
        resp.setDificuldade(Dificuldade.FACIL);
        resp.setTempoRespostaSegundos(30);
        resp.setCreatedAt(LocalDateTime.now());
        respostaRepository.save(resp);

        entityManager.flush();
        entityManager.clear();

        // 5. Execute Request with metrics=full
        // This should trigger RespostaRepository.getDificuldadeStatsByConcursoCargoAndSubtemaIds
        mockMvc.perform(get("/api/v1/concursos/{id}", concurso.getId()).param("metrics", "full"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cargos[0].topicos[0].questoesConcursoCargo.totalQuestoes").value(1))
                .andExpect(jsonPath("$.cargos[0].topicos[0].questoesConcursoCargo.respondidas").value(1))
                .andExpect(jsonPath("$.cargos[0].topicos[0].questoesConcursoCargo.acertadas").value(1))
                .andExpect(jsonPath("$.cargos[0].topicos[0].questoesConcursoCargo.dificuldade.FACIL").exists())
                .andExpect(jsonPath("$.cargos[0].topicos[0].questoesConcursoCargo.dificuldade.FACIL.total").value(1));
    }
}
