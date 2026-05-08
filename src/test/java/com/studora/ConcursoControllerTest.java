package com.studora;

import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.entity.Banca;
import com.studora.entity.Cargo;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Disciplina;
import com.studora.entity.Instituicao;
import com.studora.entity.Prova;
import com.studora.entity.ProvaSecao;
import com.studora.entity.SecaoCargo;
import com.studora.entity.SecaoDisciplina;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.repository.BancaRepository;
import com.studora.repository.CargoRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.InstituicaoRepository;
import com.studora.repository.ProvaRepository;
import com.studora.repository.ProvaSecaoRepository;
import com.studora.repository.SecaoCargoRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.util.TestUtil;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConcursoControllerTest {

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
    private SecaoCargoRepository secaoCargoRepository;

    @Autowired
    private CacheManager cacheManager;

    private Cargo cargo1;
    private Cargo cargo2;

    @BeforeEach
    void setUp() {
        if (cacheManager != null) {
            var cache = cacheManager.getCache("concurso-stats");
            if (cache != null) {
                cache.clear();
            }
        }

        cargo1 = new Cargo();
        cargo1.setNome("Cargo 1");
        cargo1.setNivel(com.studora.entity.NivelCargo.SUPERIOR);
        cargo1.setArea("TI");
        cargo1 = cargoRepository.save(cargo1);

        cargo2 = new Cargo();
        cargo2.setNome("Cargo 2");
        cargo2.setNivel(com.studora.entity.NivelCargo.MEDIO);
        cargo2.setArea("ADM");
        cargo2 = cargoRepository.save(cargo2);
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testCreateConcurso() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Create Test");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Create Test");
        banca = bancaRepository.save(banca);

        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(instituicao.getId());
        request.setBancaId(banca.getId());
        request.setAno(2023);
        request.setMes(1);
        request.setCargos(List.of(cargo1.getId()));

        mockMvc
            .perform(
                post("/api/v1/concursos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testCreateConcurso_Duplicate_Conflict() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Conflict Test");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Conflict Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concursoRepository.save(concurso);

        entityManager.flush();
        entityManager.clear();

        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(instituicao.getId());
        request.setBancaId(banca.getId());
        request.setAno(2023);
        request.setMes(1);
        request.setCargos(List.of(cargo1.getId()));

        mockMvc
            .perform(
                post("/api/v1/concursos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Conflito"))
            .andExpect(jsonPath("$.detail").value("Já existe um concurso cadastrado para esta instituição, banca, ano e mês."));
    }

    @Test
    void testGetConcursoById() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Get Test");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Get Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 6);
        concurso = concursoRepository.save(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo1);
        concursoCargoRepository.save(cc);
        concurso.getConcursoCargos().add(cc);

        entityManager.flush();
        entityManager.clear();

        mockMvc
            .perform(get("/api/v1/concursos/{id}", concurso.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.instituicao.id").value(instituicao.getId()))
            .andExpect(jsonPath("$.banca.id").value(banca.getId()))
            .andExpect(jsonPath("$.ano").value(2023))
            .andExpect(jsonPath("$.mes").value(6))
            .andExpect(jsonPath("$.cargos[0].cargoId").value(cargo1.getId()));
    }

    @Test
    void testGetConcursoById_VerifyProvaCargoId() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Inst Prova Cargo");
        instituicao.setArea("TI");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Prova Cargo");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso = concursoRepository.save(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setCargo(cargo1);
        cc.setConcurso(concurso);
        cc = concursoCargoRepository.save(cc);
        concurso.getConcursoCargos().add(cc);

        Prova prova = new Prova();
        prova.setConcurso(concurso);
        prova.setNome("Prova Objetiva");
        prova.setConcursoCargo(cc);
        prova = provaRepository.save(prova);
        concurso.getProvas().add(prova);

        entityManager.flush();
        entityManager.clear();

        mockMvc
            .perform(get("/api/v1/concursos/{id}", concurso.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cargos[0].provas[0].nome").value("Prova Objetiva"));
    }

    @Test
    void testGetConcursoById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/v1/concursos/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllConcursos() throws Exception {
        Instituicao instituicao1 = new Instituicao();
        instituicao1.setNome("Instituição All 1");
        instituicao1.setArea("Educação");
        instituicao1 = instituicaoRepository.save(instituicao1);

        Banca banca1 = new Banca();
        banca1.setNome("Banca All 1");
        banca1 = bancaRepository.save(banca1);

        concursoRepository.save(new Concurso(instituicao1, banca1, 2023, 1));
        
        entityManager.flush();
        entityManager.clear();

        mockMvc
            .perform(get("/api/v1/concursos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void testUpdateConcurso() throws Exception {
        Instituicao instituicao1 = new Instituicao();
        instituicao1.setNome("Instituição Upd 1");
        instituicao1.setArea("Educação");
        instituicao1 = instituicaoRepository.save(instituicao1);

        Banca banca1 = new Banca();
        banca1.setNome("Banca Upd 1");
        banca1 = bancaRepository.save(banca1);

        Concurso concurso = new Concurso(instituicao1, banca1, 2022, 12);
        concurso = concursoRepository.save(concurso);
        
        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo1);
        concursoCargoRepository.save(cc);
        concurso.getConcursoCargos().add(cc);

        entityManager.flush();
        entityManager.clear();

        ConcursoUpdateRequest request = new ConcursoUpdateRequest();
        request.setInstituicaoId(instituicao1.getId());
        request.setBancaId(banca1.getId());
        request.setAno(2023);
        request.setMes(6);
        request.setCargos(List.of(cargo2.getId()));

        mockMvc
            .perform(
                put("/api/v1/concursos/{id}", concurso.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isOk());
    }

    @Test
    void testGetAllConcursos_WithFilters() throws Exception {
        Instituicao instituicao1 = new Instituicao();
        instituicao1.setNome("Instituição Filter 1");
        instituicao1.setArea("TI");
        instituicao1 = instituicaoRepository.save(instituicao1);

        Banca banca1 = new Banca();
        banca1.setNome("Banca Filter 1");
        banca1 = bancaRepository.save(banca1);

        Concurso c1 = new Concurso(instituicao1, banca1, 2023, 1);
        ConcursoCargo cc1 = new ConcursoCargo();
        cc1.setCargo(cargo1);
        c1.addConcursoCargo(cc1);
        c1 = concursoRepository.save(c1);

        entityManager.flush();
        entityManager.clear();

        mockMvc
            .perform(get("/api/v1/concursos").param("bancaId", banca1.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].banca.id").value(banca1.getId()));
    }

    @Test
    void testDeleteConcurso() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Del Test");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Del Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso = concursoRepository.save(concurso);

        entityManager.flush();
        entityManager.clear();

        mockMvc
            .perform(delete("/api/v1/concursos/{id}", concurso.getId()))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/api/v1/concursos/{id}", concurso.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void testToggleInscricao() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Insc Test");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Insc Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso = concursoRepository.save(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo1);
        cc.setInscrito(false);
        cc = concursoCargoRepository.save(cc);

        entityManager.flush();
        entityManager.clear();

        mockMvc
            .perform(patch("/api/v1/concursos/cargos/{concursoCargoId}/inscricao", cc.getId()))
            .andExpect(status().isOk());
    }

    @Test
    void testGetConcursoById_WithTopicos() throws Exception {
        Disciplina disciplina = new Disciplina("Direito Administrativo Topicos");
        disciplina = disciplinaRepository.save(disciplina);

        Tema tema = new Tema();
        tema.setNome("Poderes");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        Subtema subtema1 = new Subtema();
        subtema1.setNome("Espécies de Atos");
        subtema1.setTema(tema);
        subtema1 = subtemaRepository.save(subtema1);

        Subtema subtema2 = new Subtema();
        subtema2.setNome("Atos Vinculados");
        subtema2.setTema(tema);
        subtema2 = subtemaRepository.save(subtema2);

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Topicos Test");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Topicos Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 6);
        concurso = concursoRepository.save(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo1);
        concurso.addConcursoCargo(cc);
        cc = concursoCargoRepository.save(cc);

        SecaoCargo sc1 = new SecaoCargo();
        sc1.setConcursoCargo(cc);
        sc1.setNome("Seção 1");
        sc1.setOrdem(1);
        sc1.setNumQuestoes(20);
        sc1.setPeso(1.5);

        SecaoDisciplina sd1 = new SecaoDisciplina();
        sd1.setSecaoCargo(sc1);
        sd1.setNome("Disciplina 1");
        sd1.getSubtemas().add(subtema1);
        sc1.getDisciplinas().add(sd1);
        secaoCargoRepository.save(sc1);

        SecaoCargo sc2 = new SecaoCargo();
        sc2.setConcursoCargo(cc);
        sc2.setNome("Seção 2");
        sc2.setOrdem(2);
        sc2.setNumQuestoes(30);
        sc2.setPeso(2.0);
        SecaoDisciplina sd2 = new SecaoDisciplina();
        sd2.setSecaoCargo(sc2);
        sd2.setNome("Disciplina 2");
        sd2.getSubtemas().add(subtema2);
        sc2.getDisciplinas().add(sd2);
        secaoCargoRepository.save(sc2);

        Prova prova = new Prova();
        prova.setConcurso(concurso);
        prova.setNome("Prova Topicos");
        prova.setConcursoCargo(cc);
        prova = provaRepository.save(prova);

        ProvaSecao ps1 = new ProvaSecao();
        ps1.setProva(prova);
        ps1.setSecaoCargo(sc1);
        ps1.setNome("Seção 1");
        ps1.setOrdem(1);
        provaSecaoRepository.save(ps1);

        ProvaSecao ps2 = new ProvaSecao();
        ps2.setProva(prova);
        ps2.setSecaoCargo(sc2);
        ps2.setNome("Seção 2");
        ps2.setOrdem(2);
        provaSecaoRepository.save(ps2);

        entityManager.flush();
        entityManager.clear();

        mockMvc
            .perform(get("/api/v1/concursos/{id}", concurso.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cargos[0].topicos.length()").value(2))
            .andExpect(jsonPath("$.cargos[0].provas.length()").value(1))
            .andExpect(jsonPath("$.cargos[0].topicos[?(@.nome=='Seção 1')].ordem").value(1))
            .andExpect(jsonPath("$.cargos[0].topicos[?(@.nome=='Seção 1')].numQuestoes").value(20))
            .andExpect(jsonPath("$.cargos[0].topicos[?(@.nome=='Seção 1')].peso").value(1.5))
            .andExpect(jsonPath("$.cargos[0].topicos[?(@.nome=='Seção 1')].disciplinas.length()").value(1))
            .andExpect(jsonPath("$.cargos[0].topicos[?(@.nome=='Seção 1')].disciplinas[0].assuntos.length()").value(1))
            .andExpect(jsonPath("$.cargos[0].topicos[?(@.nome=='Seção 2')].ordem").value(2))
            .andExpect(jsonPath("$.cargos[0].topicos[?(@.nome=='Seção 2')].numQuestoes").value(30))
            .andExpect(jsonPath("$.cargos[0].topicos[?(@.nome=='Seção 2')].peso").value(2.0));
    }

    @Test
    void testGetConcursoById_MetricsTiers() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Inst Tiers");
        instituicao.setArea("TI");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Tiers");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso = concursoRepository.save(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setCargo(cargo1);
        cc.setConcurso(concurso);
        cc = concursoCargoRepository.save(cc);

        Subtema subtema = new Subtema();
        subtema.setNome("Subtema Tiers");
        Tema tema = new Tema();
        tema.setNome("Tema Tiers");
        tema.setDisciplina(new Disciplina("Disc Tiers"));
        disciplinaRepository.save(tema.getDisciplina());
        tema = temaRepository.save(tema);
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        SecaoCargo sc = new SecaoCargo();
        sc.setConcursoCargo(cc);
        sc.setNome("Seção Tiers");
        SecaoDisciplina sd = new SecaoDisciplina();
        sd.setSecaoCargo(sc);
        sd.setNome("Disciplina Tiers");
        sd.getSubtemas().add(subtema);
        sc.getDisciplinas().add(sd);
        secaoCargoRepository.save(sc);

        Prova prova = new Prova();
        prova.setConcurso(concurso);
        prova.setNome("Prova Tiers");
        prova.setConcursoCargo(cc);
        prova = provaRepository.save(prova);

        ProvaSecao ps = new ProvaSecao();
        ps.setProva(prova);
        ps.setSecaoCargo(sc);
        ps.setNome("Seção Tiers");
        ps.setOrdem(1);
        provaSecaoRepository.save(ps);

        entityManager.flush();
        entityManager.clear();

        mockMvc
            .perform(get("/api/v1/concursos/{id}", concurso.getId()).param("metrics", "full"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cargos[0].topicos[0].disciplinas[0].assuntos[0].nome").value("Subtema Tiers"));
    }

    @Test
    void testToggleFinalizado() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Finalizado");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Finalizado");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso.setFinalizado(false);
        concurso = concursoRepository.save(concurso);

        entityManager.flush();
        entityManager.clear();

        mockMvc
            .perform(patch("/api/v1/concursos/{id}/finalizado", concurso.getId()))
            .andExpect(status().isOk());

        Concurso updated = concursoRepository.findById(concurso.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(updated.isFinalizado());
    }

    @Test
    void testUpdateConcurso_AddNewProvaWithSecoes() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Prova Secao");
        instituicao.setArea("TI");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Prova Secao");
        banca = bancaRepository.save(banca);

        Cargo cargo = new Cargo();
        cargo.setNome("Analista TI");
        cargo.setNivel(com.studora.entity.NivelCargo.SUPERIOR);
        cargo.setArea("TI");
        cargo = cargoRepository.save(cargo);

        Disciplina disciplina = new Disciplina("Português");
        disciplina = disciplinaRepository.save(disciplina);

        Tema tema = new Tema();
        tema.setNome("Gramática");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        Subtema subtema1 = new Subtema();
        subtema1.setNome("Ortografia");
        subtema1.setTema(tema);
        subtema1 = subtemaRepository.save(subtema1);

        Subtema subtema2 = new Subtema();
        subtema2.setNome("Pontuação");
        subtema2.setTema(tema);
        subtema2 = subtemaRepository.save(subtema2);

        // Create existing concurso with cargo
        Concurso concurso = new Concurso(instituicao, banca, 2024, 1);
        concurso = concursoRepository.save(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo);
        cc = concursoCargoRepository.save(cc);
        concurso.getConcursoCargos().add(cc);

        entityManager.flush();
        entityManager.clear();

        // Update with new prova and secoes (using DTOs)
        com.studora.dto.request.ConcursoUpdateRequest request = new com.studora.dto.request.ConcursoUpdateRequest();
        request.setInstituicaoId(instituicao.getId());
        request.setBancaId(banca.getId());
        request.setAno(2024);
        request.setMes(1);
        request.setCargos(List.of(cargo.getId()));

        com.studora.dto.request.ProvaUpdateRequest provaRequest = new com.studora.dto.request.ProvaUpdateRequest();
        provaRequest.setNome("Prova Objetiva");
        provaRequest.setCargoId(cargo.getId());
        provaRequest.setSecoes(List.of(
            createSecaoRequest("Conhecimentos Gerais", 0, 30, 1.0, 0.0, "Geral", List.of(subtema1.getId(), subtema2.getId())),
            createSecaoRequest("Conhecimentos Específicos", 1, 40, 1.5, 60.0, "Específica", List.of())
        ));
        request.setProvas(List.of(provaRequest));

        mockMvc
            .perform(
                put("/api/v1/concursos/{id}", concurso.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isOk());

        // Verify the prova and secoes were created
        Concurso updatedConcurso = concursoRepository.findById(concurso.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertFalse(updatedConcurso.getProvas().isEmpty());

        Prova savedProva = updatedConcurso.getProvas().iterator().next();
        org.junit.jupiter.api.Assertions.assertEquals("Prova Objetiva", savedProva.getNome());
        org.junit.jupiter.api.Assertions.assertEquals(2, savedProva.getSecoes().size());

        for (ProvaSecao ps : savedProva.getSecoes()) {
            org.junit.jupiter.api.Assertions.assertNotNull(ps.getSecaoCargo(),
                "ProvaSecao " + ps.getNome() + " should have secaoCargo set");
        }
    }

    private com.studora.dto.request.ProvaSecaoUpdateRequest createSecaoRequest(
            String nome, int ordem, int numQuestoes, double peso, double notaMinima, String disciplinaNome, List<Long> subtemaIds) {
        com.studora.dto.request.ProvaSecaoUpdateRequest req = new com.studora.dto.request.ProvaSecaoUpdateRequest();
        req.setNome(nome);
        req.setOrdem(ordem);
        req.setNumQuestoes(numQuestoes);
        req.setPeso(peso);
        req.setNotaMinima(notaMinima);
        
        com.studora.dto.request.SecaoDisciplinaRequest dReq = new com.studora.dto.request.SecaoDisciplinaRequest();
        dReq.setNome(disciplinaNome);
        dReq.setSubtemaIds(subtemaIds);
        req.setDisciplinas(List.of(dReq));
        
        return req;
    }
}
