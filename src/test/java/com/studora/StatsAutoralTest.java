package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.AlternativaCreateRequest;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.entity.*;
import com.studora.repository.*;
import com.studora.util.TestUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StatsAutoralTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CacheManager cacheManager;

    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private SubtemaRepository subtemaRepository;
    @Autowired private QuestaoRepository questaoRepository;
    @Autowired private AlternativaRepository alternativaRepository;
    @Autowired private RespostaRepository respostaRepository;
    @Autowired private BancaRepository bancaRepository;
    @Autowired private InstituicaoRepository instituicaoRepository;
    @Autowired private CargoRepository cargoRepository;
    @Autowired private ConcursoRepository concursoRepository;
    @Autowired private ConcursoCargoRepository concursoCargoRepository;
    @Autowired private ProvaRepository provaRepository;
    @Autowired private ProvaSecaoRepository provaSecaoRepository;
    @Autowired private SecaoCargoRepository secaoCargoRepository;

    private Disciplina disciplina;
    private Tema tema;
    private Subtema subtema;
    private Long standardQuestaoId;
    private Long autoralQuestaoId;

    @BeforeEach
    void setUp() throws Exception {
        clearCaches();

        Disciplina disc = new Disciplina();
        disc.setNome("Disciplina Autoral Stats Test");
        disciplina = disciplinaRepository.save(disc);

        Tema t = new Tema();
        t.setNome("Tema Autoral Stats Test");
        t.setDisciplina(disciplina);
        tema = temaRepository.save(t);

        Subtema s = new Subtema();
        s.setNome("Subtema Autoral Stats Test");
        s.setTema(t);
        subtema = subtemaRepository.save(s);

        // --- Standard question ---
        Questao std = new Questao();
        std.setEnunciado("Standard question");
        std.setAnulada(false);
        std.setAutoral(false);
        std.addSubtema(subtema, true);

        Instituicao inst = new Instituicao();
        inst.setNome("Inst Stats");
        inst.setArea("Area");
        inst = instituicaoRepository.save(inst);

        Banca banca = new Banca();
        banca.setNome("Banca Stats");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso();
        concurso.setInstituicao(inst);
        concurso.setBanca(banca);
        concurso.setAno(2024);
        concurso.setMes(1);
        concurso = concursoRepository.save(concurso);

        Cargo cargo = new Cargo();
        cargo.setNome("Cargo Stats");
        cargo.setNivel(NivelCargo.SUPERIOR);
        cargo.setArea("Area");
        cargo = cargoRepository.save(cargo);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo);
        cc = concursoCargoRepository.save(cc);

        Prova prova = new Prova();
        prova.setConcurso(concurso);
        prova.setNome("Prova Stats");
        prova.setConcursoCargo(cc);
        prova = provaRepository.save(prova);

        SecaoCargo scDef = new SecaoCargo();
        scDef.setConcursoCargo(cc);
        scDef.setNome("Geral");
        scDef.setPeso(1.0);
        secaoCargoRepository.save(scDef);

        ProvaSecao secao = new ProvaSecao();
        secao.setProva(prova);
        secao.setSecaoCargo(scDef);
        secao.setNome("Geral");
        secao.setOrdem(1);
        secao = provaSecaoRepository.save(secao);

        std.setAnulada(false);
        std.setAutoral(false);
        std.addSubtema(subtema, true);

        QuestaoProvaSecao qps = new QuestaoProvaSecao();
        qps.setProvaSecao(secao);
        std.addSecao(qps);

        std = questaoRepository.save(std);

        Alternativa stdAlt = new Alternativa();
        stdAlt.setQuestao(std);
        stdAlt.setTexto("A");
        stdAlt.setCorreta(true);
        stdAlt.setOrdem(1);
        alternativaRepository.save(stdAlt);

        standardQuestaoId = std.getId();

        // --- Autoral question ---
        Questao aut = new Questao();
        aut.setEnunciado("Autoral question");
        aut.setAnulada(false);
        aut.setAutoral(true);
        aut.addSubtema(subtema, true);
        aut = questaoRepository.save(aut);

        Alternativa autAlt = new Alternativa();
        autAlt.setQuestao(aut);
        autAlt.setTexto("A");
        autAlt.setCorreta(true);
        autAlt.setOrdem(1);
        alternativaRepository.save(autAlt);

        autoralQuestaoId = aut.getId();
    }

    private void clearCaches() {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null)
                    cache.clear();
            });
        }
    }

    private void createRespostaForQuestao(Long questaoId, boolean correta, int tempoSegundos,
            Dificuldade dificuldade, LocalDateTime createdAt) {
        Resposta resp = new Resposta();
        resp.setQuestao(questaoRepository.findById(questaoId).orElseThrow());
        Alternativa alt = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(questaoId).get(0);
        resp.setAlternativaEscolhida(alt);
        resp.setDificuldade(dificuldade);
        resp.setTempoRespostaSegundos(tempoSegundos);
        resp.setCreatedAt(createdAt);
        respostaRepository.save(resp);
    }

    @Nested
    @DisplayName("Disciplina - porAutoral stats")
    class DisciplinaTests {
        @Test
        @DisplayName("GET /disciplinas/{id}?metrics=full - total includes autoral, porAutoral shows autoral-only count")
        void testDisciplinaTotalIncludesAutoral_andPorAutoralExists() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/v1/disciplinas/" + disciplina.getId())
                    .param("metrics", "full"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode stats = root.path("questaoStats");

            assert stats.path("total").path("totalQuestoes").asInt() == 2;
            JsonNode porAutoral = stats.path("porAutoral");
            assert porAutoral.path("totalQuestoes").asInt() == 1;
        }

        @Test
        @DisplayName("After answering autoral question, porAutoral.respondidas = 1")
        void testAutoralRespondidas() throws Exception {
            createRespostaForQuestao(autoralQuestaoId, true, 45, Dificuldade.FACIL, LocalDateTime.now().minusDays(2));
            clearCaches();

            MvcResult result = mockMvc.perform(get("/api/v1/disciplinas/" + disciplina.getId())
                    .param("metrics", "full"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode porAutoral = root.path("questaoStats").path("porAutoral");

            assert porAutoral.path("respondidas").asInt() == 1;
            assert porAutoral.path("acertadas").asInt() == 1;
        }

        @Test
        @DisplayName("porAutoral has full StatSliceDto fields")
        void testAutoralFullSlice() throws Exception {
            createRespostaForQuestao(autoralQuestaoId, true, 45, Dificuldade.MEDIA, LocalDateTime.now().minusDays(3));
            createRespostaForQuestao(standardQuestaoId, true, 30, Dificuldade.FACIL, LocalDateTime.now().minusDays(1));
            clearCaches();

            MvcResult result = mockMvc.perform(get("/api/v1/disciplinas/" + disciplina.getId())
                    .param("metrics", "full"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode porAutoral = root.path("questaoStats").path("porAutoral");

            assert porAutoral.has("dificuldade");
            assert porAutoral.has("mediaTempoResposta");
            assert porAutoral.has("ultimaQuestao");
        }
    }

    @Nested
    @DisplayName("Banca - porAutoral should not exist")
    class BancaTests {
        @Test
        @DisplayName("GET /bancas/{id}?metrics=full - no porAutoral")
        void testBancaNoPorAutoral() throws Exception {
            Banca banca = new Banca();
            banca.setNome("Banca Stats Test");
            banca = bancaRepository.save(banca);

            Instituicao inst = new Instituicao();
            inst.setNome("Inst Banca Stats");
            inst.setArea("Area");
            inst = instituicaoRepository.save(inst);

            Concurso concurso = new Concurso();
            concurso.setInstituicao(inst);
            concurso.setBanca(banca);
            concurso.setAno(2024);
            concurso.setMes(1);
            concurso = concursoRepository.save(concurso);

            Cargo cargo = new Cargo();
            cargo.setNome("Cargo Banca Stats");
            cargo.setNivel(NivelCargo.SUPERIOR);
            cargo.setArea("Area");
            cargo = cargoRepository.save(cargo);

            ConcursoCargo cc = new ConcursoCargo();
            cc.setConcurso(concurso);
            cc.setCargo(cargo);
            cc = concursoCargoRepository.save(cc);

            Prova prova = new Prova();
            prova.setConcurso(concurso);
            prova.setNome("Banca Prova");
            prova.setConcursoCargo(cc);
            prova = provaRepository.save(prova);

            SecaoCargo scDef = new SecaoCargo();
            scDef.setConcursoCargo(cc);
            scDef.setNome("Geral");
            scDef.setPeso(1.0);
            secaoCargoRepository.save(scDef);

            ProvaSecao secao = new ProvaSecao();
            secao.setProva(prova);
            secao.setSecaoCargo(scDef);
            secao.setNome("Geral");
            secao.setOrdem(1);
            secao = provaSecaoRepository.save(secao);

            Questao std = new Questao();
            std.setEnunciado("Standard for banca");
            std.setAnulada(false);
            std.setAutoral(false);
            QuestaoProvaSecao qps = new QuestaoProvaSecao();
            qps.setProvaSecao(secao);
            std.addSecao(qps);
            std.addSubtema(subtema, true);
            questaoRepository.save(std);

            Alternativa alt = new Alternativa();
            alt.setQuestao(std);
            alt.setTexto("A");
            alt.setCorreta(true);
            alt.setOrdem(1);
            alternativaRepository.save(alt);

            clearCaches();

            MvcResult result = mockMvc.perform(get("/api/v1/bancas/" + banca.getId())
                    .param("metrics", "full"))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode stats = root.path("questaoStats");

            assert !stats.has("porAutoral") || stats.path("porAutoral").isNull();
        }
    }
}
