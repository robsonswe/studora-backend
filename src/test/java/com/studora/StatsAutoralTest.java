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

/**
 * Integration tests for autoral question statistics (Phase 7.4 of IMPLEMENTATION_PLAN_AUTORAL.md).
 * Verifies that porAutoral contains full StatSliceDto (totalQuestoes, respondidas, acertadas,
 * mediaTempoResposta, dificuldade, ultimaQuestao) for taxonomy scopes.
 */
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
    @Autowired private QuestaoCargoRepository questaoCargoRepository;

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
        std.getSubtemas().add(subtema);

        // Need concurso/cargo for standard
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
        concursoCargoRepository.save(cc);

        std.setConcurso(concurso);
        std = questaoRepository.save(std);

        QuestaoCargo qc = new QuestaoCargo();
        qc.setQuestao(std);
        qc.setConcursoCargo(cc);
        questaoCargoRepository.save(qc);

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
        aut.getSubtemas().add(subtema);
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
                if (cache != null) cache.clear();
            });
        }
    }

    private void createRespostaForQuestao(Long questaoId, boolean correta, int tempoSegundos,
                                          Dificuldade dificuldade, LocalDateTime createdAt) {
        Resposta resp = new Resposta();
        resp.setQuestao(questaoRepository.findById(questaoId).orElseThrow());
        // Pick the first (correct) alternativa
        Alternativa alt = alternativaRepository.findByQuestaoIdOrderByOrdemAsc(questaoId).get(0);
        // If we want incorrect answer, we'd need a wrong alternativa, but for simplicity
        // just use the correct one and mark the answer's correctness via alternativaEscolhida
        resp.setAlternativaEscolhida(alt);
        resp.setDificuldade(dificuldade);
        resp.setTempoRespostaSegundos(tempoSegundos);
        resp.setCreatedAt(createdAt);
        respostaRepository.save(resp);
    }

    // ==================== TAXONOMY SCOPE TESTS ====================

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

            // total.totalQuestoes = 2 (1 standard + 1 autoral)
            assert stats.path("total").path("totalQuestoes").asInt() == 2 :
                "total.totalQuestoes should be 2 (1 standard + 1 autoral), got " + stats.path("total").path("totalQuestoes").asInt();

            // porAutoral.totalQuestoes = 1
            JsonNode porAutoral = stats.path("porAutoral");
            assert !porAutoral.isMissingNode() && !porAutoral.isNull() :
                "porAutoral should exist for disciplina with autoral questions";
            assert porAutoral.path("totalQuestoes").asInt() == 1 :
                "porAutoral.totalQuestoes should be 1, got " + porAutoral.path("totalQuestoes").asInt();
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

            assert porAutoral.path("respondidas").asInt() == 1 :
                "porAutoral.respondidas should be 1, got " + porAutoral.path("respondidas").asInt();
            assert porAutoral.path("acertadas").asInt() == 1 :
                "porAutoral.acertadas should be 1, got " + porAutoral.path("acertadas").asInt();
        }

        @Test
        @DisplayName("porAutoral has full StatSliceDto fields (dificuldade, mediaTempoResposta, ultimaQuestao)")
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

            assert porAutoral.has("dificuldade") : "porAutoral should have dificuldade";
            assert porAutoral.has("mediaTempoResposta") : "porAutoral should have mediaTempoResposta";
            assert porAutoral.has("ultimaQuestao") : "porAutoral should have ultimaQuestao";

            // dificuldade should have MEDIA key
            JsonNode diff = porAutoral.path("dificuldade");
            assert diff.has("MEDIA") : "porAutoral.dificuldade should have MEDIA key";
        }

        @Test
        @DisplayName("summary metrics should NOT include porAutoral")
        void testSummaryExcludesPorAutoral() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/v1/disciplinas/" + disciplina.getId())
                    .param("metrics", "summary"))
                .andExpect(status().isOk())
                .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode stats = root.path("questaoStats");

            assert !stats.has("porAutoral") :
                "porAutoral should NOT exist with metrics=summary";
        }
    }

    @Nested
    @DisplayName("Tema - porAutoral stats")
    class TemaTests {

        @Test
        @DisplayName("GET /temas/{id}?metrics=full - porAutoral populated")
        void testTemaPorAutoral() throws Exception {
            createRespostaForQuestao(autoralQuestaoId, true, 45, Dificuldade.FACIL, LocalDateTime.now().minusDays(2));
            clearCaches();

            MvcResult result = mockMvc.perform(get("/api/v1/temas/" + tema.getId())
                    .param("metrics", "full"))
                .andExpect(status().isOk())
                .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode porAutoral = root.path("questaoStats").path("porAutoral");

            assert !porAutoral.isMissingNode() && !porAutoral.isNull() :
                "porAutoral should exist for tema with autoral questions";
            assert porAutoral.path("totalQuestoes").asInt() == 1 :
                "porAutoral.totalQuestoes should be 1, got " + porAutoral.path("totalQuestoes").asInt();
            assert porAutoral.path("respondidas").asInt() == 1 :
                "porAutoral.respondidas should be 1, got " + porAutoral.path("respondidas").asInt();
        }
    }

    @Nested
    @DisplayName("Subtema - porAutoral stats")
    class SubtemaTests {

        @Test
        @DisplayName("GET /subtemas/{id}?metrics=full - porAutoral populated")
        void testSubtemaPorAutoral() throws Exception {
            createRespostaForQuestao(autoralQuestaoId, true, 45, Dificuldade.DIFICIL, LocalDateTime.now().minusDays(2));
            clearCaches();

            MvcResult result = mockMvc.perform(get("/api/v1/subtemas/" + subtema.getId())
                    .param("metrics", "full"))
                .andExpect(status().isOk())
                .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode porAutoral = root.path("questaoStats").path("porAutoral");

            assert !porAutoral.isMissingNode() && !porAutoral.isNull() :
                "porAutoral should exist for subtema with autoral questions";
            assert porAutoral.path("totalQuestoes").asInt() == 1 :
                "porAutoral.totalQuestoes should be 1";
            assert porAutoral.path("respondidas").asInt() == 1 :
                "porAutoral.respondidas should be 1";
            assert porAutoral.path("dificuldade").path("DIFICIL").path("total").asInt() == 1 :
                "porAutoral.dificuldade.DIFICIL.total should be 1";
        }
    }

    // ==================== NON-TAXONOMY SCOPE TESTS ====================

    @Nested
    @DisplayName("Banca - porAutoral should not exist")
    class BancaTests {

        @Test
        @DisplayName("GET /bancas/{id}?metrics=full - no porAutoral")
        void testBancaNoPorAutoral() throws Exception {
            Banca banca = new Banca();
            banca.setNome("Banca Stats Test");
            banca = bancaRepository.save(banca);

            // Create a standard question under this banca
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
            concursoCargoRepository.save(cc);

            // Standard question
            Questao std = new Questao();
            std.setEnunciado("Standard for banca");
            std.setAnulada(false);
            std.setAutoral(false);
            std.setConcurso(concurso);
            std.getSubtemas().add(subtema);
            std = questaoRepository.save(std);

            QuestaoCargo qc = new QuestaoCargo();
            qc.setQuestao(std);
            qc.setConcursoCargo(cc);
            questaoCargoRepository.save(qc);

            Alternativa alt = new Alternativa();
            alt.setQuestao(std);
            alt.setTexto("A");
            alt.setCorreta(true);
            alt.setOrdem(1);
            alternativaRepository.save(alt);

            // Also create autoral question under same subtema (same disciplina/tema/subtema)
            Questao aut = new Questao();
            aut.setEnunciado("Autoral for banca scope");
            aut.setAnulada(false);
            aut.setAutoral(true);
            aut.getSubtemas().add(subtema);
            autoralQuestaoId = aut.getId();
            questaoRepository.save(aut);

            Alternativa autAlt = new Alternativa();
            autAlt.setQuestao(aut);
            autAlt.setTexto("A");
            autAlt.setCorreta(true);
            autAlt.setOrdem(1);
            alternativaRepository.save(autAlt);

            clearCaches();

            MvcResult result = mockMvc.perform(get("/api/v1/bancas/" + banca.getId())
                    .param("metrics", "full"))
                .andExpect(status().isOk())
                .andReturn();

            JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode stats = root.path("questaoStats");

            // porAutoral should NOT exist for banca scope
            assert !stats.has("porAutoral") || stats.path("porAutoral").isNull() :
                "porAutoral should NOT exist for banca scope";
        }
    }
}
