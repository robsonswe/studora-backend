package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.entity.*;
import com.studora.repository.*;
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
import java.util.Iterator;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StatsStructureTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CacheManager cacheManager;

    @Autowired private BancaRepository bancaRepository;
    @Autowired private InstituicaoRepository instituicaoRepository;
    @Autowired private CargoRepository cargoRepository;
    @Autowired private ConcursoRepository concursoRepository;
    @Autowired private ConcursoCargoRepository concursoCargoRepository;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private SubtemaRepository subtemaRepository;
    @Autowired private QuestaoRepository questaoRepository;
    @Autowired private AlternativaRepository alternativaRepository;
    @Autowired private RespostaRepository respostaRepository;
    @Autowired private QuestaoCargoRepository questaoCargoRepository;

    @BeforeEach
    void clearCaches() {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(name -> {
                var cache = cacheManager.getCache(name);
                if (cache != null) cache.clear();
            });
        }
    }

    // ==================== TEST DATA BUILDER ====================

    private TestData createTestData() {
        TestData data = new TestData();

        // Create hierarchy
        data.banca = new Banca();
        data.banca.setNome("Cebraspe");
        data.banca = bancaRepository.save(data.banca);

        data.instituicao = new Instituicao();
        data.instituicao.setNome("Polícia Federal");
        data.instituicao.setArea("Policial");
        data.instituicao = instituicaoRepository.save(data.instituicao);

        Concurso concurso = new Concurso();
        concurso.setBanca(data.banca);
        concurso.setInstituicao(data.instituicao);
        concurso.setAno(2024);
        concurso.setMes(6);
        data.concurso = concursoRepository.save(concurso);

        Cargo cargo = new Cargo();
        cargo.setNome("Agente");
        cargo.setNivel(NivelCargo.SUPERIOR);
        cargo.setArea("Policial");
        data.cargo = cargoRepository.save(cargo);

        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setConcurso(data.concurso);
        concursoCargo.setCargo(cargo);
        concursoCargoRepository.save(concursoCargo);

        Disciplina disciplina = new Disciplina();
        disciplina.setNome("Direito Administrativo");
        data.disciplina = disciplinaRepository.save(disciplina);

        Tema tema = new Tema();
        tema.setNome("Atos Administrativos");
        tema.setDisciplina(disciplina);
        data.tema = temaRepository.save(tema);

        Subtema subtema = new Subtema();
        subtema.setNome("Atributos do Ato");
        subtema.setTema(tema);
        data.subtema = subtemaRepository.save(subtema);

        // Create questions with alternatives
        for (int i = 0; i < 5; i++) {
            Questao questao = new Questao();
            questao.setConcurso(data.concurso);
            questao.setEnunciado("Questão " + i);
            questao.setAnulada(false);
            questao = questaoRepository.save(questao);

            // Link questao to cargo
            QuestaoCargo qc = new QuestaoCargo();
            qc.setQuestao(questao);
            qc.setConcursoCargo(concursoCargo);
            questaoCargoRepository.save(qc);

            // Link questao to subtema
            questao.getSubtemas().add(subtema);
            questaoRepository.save(questao);

            // Create alternatives (first one is correct)
            Alternativa altCorreta = new Alternativa();
            altCorreta.setQuestao(questao);
            altCorreta.setTexto("Alternativa A");
            altCorreta.setCorreta(true);
            altCorreta.setOrdem(1);
            alternativaRepository.save(altCorreta);

            Alternativa altErrada = new Alternativa();
            altErrada.setQuestao(questao);
            altErrada.setTexto("Alternativa B");
            altErrada.setCorreta(false);
            altErrada.setOrdem(2);
            alternativaRepository.save(altErrada);

            // Create responses with different difficulties
            if (i < 3) {
                Resposta resp = new Resposta();
                resp.setQuestao(questao);
                resp.setAlternativaEscolhida(i % 2 == 0 ? altCorreta : altErrada);
                resp.setDificuldade(i == 0 ? Dificuldade.FACIL : i == 1 ? Dificuldade.MEDIA : Dificuldade.DIFICIL);
                resp.setTempoRespostaSegundos(30 + i * 10);
                resp.setCreatedAt(LocalDateTime.now().minusDays(5 - i));
                respostaRepository.save(resp);
            }
        }

        return data;
    }

    static class TestData {
        Banca banca;
        Instituicao instituicao;
        Concurso concurso;
        Disciplina disciplina;
        Tema tema;
        Subtema subtema;
        Cargo cargo;
    }

    // ==================== ASSERTION HELPERS ====================

    private void assertDificuldadeHasStringKeys(JsonNode dificuldadeNode, String endpoint) {
        if (dificuldadeNode == null || dificuldadeNode.isMissingNode()) return;
        
        Iterator<String> fieldNames = dificuldadeNode.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (key.matches("\\d+")) {
                throw new AssertionError(endpoint + " - dificuldade key should be string (FACIL/MEDIA/DIFICIL/CHUTE) but got numeric: " + key);
            }
            if (!key.equals("FACIL") && !key.equals("MEDIA") && !key.equals("DIFICIL") && !key.equals("CHUTE")) {
                throw new AssertionError(endpoint + " - dificuldade key has unexpected value: " + key);
            }
            JsonNode stat = dificuldadeNode.get(key);
            if (stat.has("total") && stat.has("corretas")) {
                // Valid structure
            } else {
                throw new AssertionError(endpoint + " - dificuldade[" + key + "] missing total/corretas fields");
            }
        }
    }

    private void assertPorStatsPopulated(JsonNode porNode, String porName, String endpoint) {
        if (porNode == null || porNode.isMissingNode()) return;
        if (!porNode.isObject()) return;
        
        if (porNode.size() == 0) {
            // Empty is OK if no data exists for that breakdown
            return;
        }

        Iterator<String> keys = porNode.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            JsonNode slice = porNode.get(key);
            
            // For Long-keyed maps (porBanca, porInstituicao, porCargo), nome should be entity name not ID
            if (slice.has("nome")) {
                String nome = slice.get("nome").asText();
                // nome should NOT be just a number - it should be the actual entity name
                if (nome.matches("\\d+")) {
                    // This is a bug - nome is showing ID instead of actual name
                    // We'll fail the test for this
                    throw new AssertionError(endpoint + "." + porName + "[" + key + "].nome shows ID '" + nome + "' instead of entity name");
                }
            }
            
            // Should have id field for entity-keyed maps
            if (key.matches("\\d+") && slice.has("id")) {
                long idFromField = slice.get("id").asLong();
                if (idFromField != Long.parseLong(key)) {
                    throw new AssertionError(endpoint + "." + porName + "[" + key + "].id (" + idFromField + ") doesn't match map key");
                }
            }
        }
    }

    private void validateQuestaoStatsStructure(JsonNode statsNode, String endpoint, boolean expectBreakdowns) throws Exception {
        if (statsNode == null || statsNode.isMissingNode()) return;

        // Validate total exists and has structure
        JsonNode total = statsNode.get("total");
        if (total != null && !total.isMissingNode()) {
            // total should NOT have nome or id
            if (total.has("nome") && !total.path("nome").isNull() && !total.path("nome").asText().isEmpty()) {
                // nome in total is acceptable to be absent or null
            }
            if (total.has("dificuldade")) {
                assertDificuldadeHasStringKeys(total.get("dificuldade"), endpoint + ".total");
            }
        }

        if (expectBreakdowns) {
            // Validate all por stats if they exist
            String[] porFields = {"porNivel", "porBanca", "porInstituicao", "porAreaInstituicao", "porCargo", "porAreaCargo"};
            for (String field : porFields) {
                if (statsNode.has(field) && !statsNode.get(field).isMissingNode()) {
                    assertPorStatsPopulated(statsNode.get(field), field, endpoint);
                }
            }
        }
    }

    private MvcResult performGetWithMetrics(String endpoint, String metrics) throws Exception {
        return mockMvc.perform(get(endpoint).param("metrics", metrics))
                .andExpect(status().isOk())
                .andReturn();
    }

    // ==================== BANCA TESTS ====================

    @Nested
    @DisplayName("Banca Stats Structure")
    class BancaTests {

        @Test
        @DisplayName("GET /bancas/1?metrics=full - validates structure with data")
        void testBancaDetailWithFullMetrics() throws Exception {
            TestData data = createTestData();

            MvcResult result = performGetWithMetrics("/api/v1/bancas/" + data.banca.getId(), "full");
            String json = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(json);

            // Validate questaoStats exists
            assert root.has("questaoStats") : "questaoStats should exist";
            validateQuestaoStatsStructure(root.get("questaoStats"), "bancas/" + data.banca.getId(), true);

            // Validate dificuldade keys are strings not numeric IDs
            JsonNode dificuldade = root.path("questaoStats").path("total").path("dificuldade");
            if (!dificuldade.isMissingNode() && dificuldade.size() > 0) {
                assertDificuldadeHasStringKeys(dificuldade, "banca dificuldade");
            }
        }

        @Test
        @DisplayName("GET /bancas?metrics=full - list endpoint doesn't throw 500")
        void testBancaListWithFullMetrics() throws Exception {
            createTestData();

            mockMvc.perform(get("/api/v1/bancas").param("metrics", "full").param("sort", "nome").param("direction", "ASC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ==================== CARGO TESTS ====================

    @Nested
    @DisplayName("Cargo Stats Structure")
    class CargoTests {

        @Test
        @DisplayName("GET /cargos/1?metrics=full - validates structure")
        void testCargoDetailWithFullMetrics() throws Exception {
            TestData data = createTestData();

            MvcResult result = performGetWithMetrics("/api/v1/cargos/" + data.cargo.getId(), "full");
            String json = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(json);

            assert root.has("questaoStats") : "questaoStats should exist";

            JsonNode stats = root.get("questaoStats");
            // porNivel is redundant for Cargo (a cargo has a single nivel)
            assert !stats.has("porNivel") || stats.path("porNivel").isMissingNode() :
                "porNivel should not exist for Cargo endpoint";
            assert stats.has("porBanca") : "porBanca should exist";
            assert stats.has("porAreaCargo") : "porAreaCargo should exist";
            assert stats.has("porAreaInstituicao") : "porAreaInstituicao should exist";

            validateQuestaoStatsStructure(stats, "cargos/" + data.cargo.getId(), true);
        }

        @Test
        @DisplayName("GET /cargos?metrics=full - list endpoint doesn't throw 500")
        void testCargoListWithFullMetrics() throws Exception {
            createTestData();

            mockMvc.perform(get("/api/v1/cargos").param("metrics", "full").param("sort", "nome").param("direction", "ASC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ==================== INSTITUICAO TESTS ====================

    @Nested
    @DisplayName("Instituicao Stats Structure")
    class InstituicaoTests {

        @Test
        @DisplayName("GET /instituicoes/1?metrics=full - validates structure")
        void testInstituicaoDetailWithFullMetrics() throws Exception {
            TestData data = createTestData();

            MvcResult result = performGetWithMetrics("/api/v1/instituicoes/" + data.instituicao.getId(), "full");
            String json = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(json);

            assert root.has("questaoStats") : "questaoStats should exist";
            validateQuestaoStatsStructure(root.get("questaoStats"), "instituicoes/" + data.instituicao.getId(), true);
        }

        @Test
        @DisplayName("GET /instituicoes?metrics=full - list endpoint doesn't throw 500")
        void testInstituicaoListWithFullMetrics() throws Exception {
            createTestData();

            mockMvc.perform(get("/api/v1/instituicoes").param("metrics", "full").param("sort", "nome").param("direction", "ASC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ==================== DISCIPLINA TESTS ====================

    @Nested
    @DisplayName("Disciplina Stats Structure")
    class DisciplinaTests {

        @Test
        @DisplayName("GET /disciplinas/2?metrics=full - validates all por breakdowns")
        void testDisciplinaDetailAllBreakdowns() throws Exception {
            TestData data = createTestData();

            MvcResult result = performGetWithMetrics("/api/v1/disciplinas/" + data.disciplina.getId(), "full");
            String json = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(json);

            JsonNode stats = root.path("questaoStats");
            assert !stats.isMissingNode() : "questaoStats should exist";

            // Validate all 6 por fields exist
            String[] porFields = {"porNivel", "porBanca", "porInstituicao", "porAreaInstituicao", "porCargo", "porAreaCargo"};
            for (String field : porFields) {
                assert stats.has(field) : field + " should exist in disciplina stats";
                // Validate each has proper structure
                assertPorStatsPopulated(stats.get(field), field, "disciplina." + field);
            }

            // Validate dificuldade has string keys (uppercase)
            JsonNode dificuldade = stats.path("total").path("dificuldade");
            if (!dificuldade.isMissingNode() && dificuldade.size() > 0) {
                assertDificuldadeHasStringKeys(dificuldade, "disciplina dificuldade");
            }
        }

        @Test
        @DisplayName("GET /disciplinas/2/completo?metrics=full - recursive stats")
        void testDisciplinaCompleto() throws Exception {
            TestData data = createTestData();

            MvcResult result = performGetWithMetrics("/api/v1/disciplinas/" + data.disciplina.getId() + "/completo", "full");
            String json = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(json);

            // Root disciplina should have stats
            assert root.has("questaoStats") : "disciplina questaoStats should exist";

            // Temas should have stats
            JsonNode temas = root.path("temas");
            if (!temas.isMissingNode() && temas.isArray() && temas.size() > 0) {
                JsonNode tema = temas.get(0);
                assert tema.has("questaoStats") : "tema questaoStats should exist";

                // Subtemas should have stats
                JsonNode subtemas = tema.path("subtemas");
                if (!subtemas.isMissingNode() && subtemas.isArray() && subtemas.size() > 0) {
                    JsonNode subtema = subtemas.get(0);
                    assert subtema.has("questaoStats") : "subtema questaoStats should exist";
                }
            }
        }
    }

    // ==================== TEMA TESTS ====================

    @Nested
    @DisplayName("Tema Stats Structure")
    class TemaTests {

        @Test
        @DisplayName("GET /temas/1?metrics=full - validates structure")
        void testTemaDetailWithFullMetrics() throws Exception {
            TestData data = createTestData();

            MvcResult result = performGetWithMetrics("/api/v1/temas/" + data.tema.getId(), "full");
            String json = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(json);

            assert root.has("questaoStats") : "questaoStats should exist";
            validateQuestaoStatsStructure(root.get("questaoStats"), "temas/" + data.tema.getId(), true);

            // Validate nested disciplina doesn't have questaoStats (lean rule)
            JsonNode disciplina = root.path("disciplina");
            if (!disciplina.isMissingNode()) {
                assert !disciplina.has("questaoStats") || disciplina.path("questaoStats").isNull() : 
                    "nested disciplina should not have questaoStats";
            }
        }
    }

    // ==================== SUBTEMA TESTS ====================

    @Nested
    @DisplayName("Subtema Stats Structure")
    class SubtemaTests {

        @Test
        @DisplayName("GET /subtemas/1?metrics=full - validates lean tema/disciplina references")
        void testSubtemaDetailLeanReferences() throws Exception {
            TestData data = createTestData();

            MvcResult result = performGetWithMetrics("/api/v1/subtemas/" + data.subtema.getId(), "full");
            String json = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(json);

            // Validate questaoStats
            assert root.has("questaoStats") : "questaoStats should exist";

            // Validate tema is lean (only id and nome)
            JsonNode tema = root.path("tema");
            if (!tema.isMissingNode()) {
                assert tema.has("id") : "tema should have id";
                assert tema.has("nome") : "tema should have nome";
                // Should NOT have subtemas, disciplina, stats, etc
                assert !tema.has("subtemas") || tema.path("subtemas").isNull() : 
                    "lean tema should not have subtemas list";
                assert !tema.has("questaoStats") : "lean tema should not have questaoStats";
            }

            // Validate disciplina is lean (only id and nome)
            JsonNode disciplina = root.path("disciplina");
            if (!disciplina.isMissingNode()) {
                assert disciplina.has("id") : "disciplina should have id";
                assert disciplina.has("nome") : "disciplina should have nome";
                // Should NOT have temas, stats, etc
                assert !disciplina.has("temas") : "lean disciplina should not have temas";
                assert !disciplina.has("questaoStats") : "lean disciplina should not have questaoStats";
            }

            // Validate dificuldade has string keys
            JsonNode dificuldade = root.path("questaoStats").path("total").path("dificuldade");
            if (!dificuldade.isMissingNode() && dificuldade.size() > 0) {
                assertDificuldadeHasStringKeys(dificuldade, "subtema dificuldade");
            }
        }

        @Test
        @DisplayName("GET /subtemas?metrics=full - list endpoint")
        void testSubtemaListWithFullMetrics() throws Exception {
            createTestData();

            mockMvc.perform(get("/api/v1/subtemas").param("metrics", "full").param("sort", "nome").param("direction", "ASC"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ==================== CONCORSO TESTS (baseline - should already work) ====================

    @Nested
    @DisplayName("Concurso Stats Structure (baseline)")
    class ConcursoTests {

        @Test
        @DisplayName("GET /concursos/1?metrics=full - validates difficulty names")
        void testConcursoDificuldadeNames() throws Exception {
            TestData data = createTestData();

            MvcResult result = performGetWithMetrics("/api/v1/concursos/" + data.concurso.getId(), "full");
            String json = result.getResponse().getContentAsString();
            JsonNode root = objectMapper.readTree(json);

            // Navigate to cargo -> topicos -> dificuldadeRespostas
            JsonNode cargos = root.path("cargos");
            if (!cargos.isMissingNode() && cargos.isArray() && cargos.size() > 0) {
                JsonNode topicos = cargos.get(0).path("topicos");
                if (!topicos.isMissingNode() && topicos.isArray() && topicos.size() > 0) {
                    JsonNode questaoStats = topicos.get(0).path("questaoStats");
                    if (!questaoStats.isMissingNode()) {
                        JsonNode total = questaoStats.path("total");
                        JsonNode dificuldade = total.path("dificuldade");
                        if (!dificuldade.isMissingNode() && dificuldade.size() > 0) {
                            assertDificuldadeHasStringKeys(dificuldade, "concurso topicos dificuldadeRespostas");
                        }
                    }
                }
            }
        }
    }

    // ==================== METRICS TIERS TESTS ====================

    @Nested
    @DisplayName("Metrics Tier Separation")
    class MetricsTiersTests {

        @Test
        @DisplayName("Disciplina - lean vs summary vs full tiers")
        void testDisciplinaMetricsTiers() throws Exception {
            TestData data = createTestData();
            String endpoint = "/api/v1/disciplinas/" + data.disciplina.getId();

            // Lean - no questaoStats
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.questaoStats").doesNotExist());

            // Summary - only total
            mockMvc.perform(get(endpoint).param("metrics", "summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.questaoStats.total").exists())
                    .andExpect(jsonPath("$.questaoStats.porNivel").doesNotExist());

            // Full - all breakdowns
            mockMvc.perform(get(endpoint).param("metrics", "full"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.questaoStats.total").exists())
                    .andExpect(jsonPath("$.questaoStats.porNivel").exists())
                    .andExpect(jsonPath("$.questaoStats.porBanca").exists());
        }
    }
}
