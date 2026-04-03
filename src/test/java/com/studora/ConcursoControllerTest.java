package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.entity.Banca;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.ConcursoCargoSubtema;
import com.studora.entity.Cargo;
import com.studora.entity.Disciplina;
import com.studora.entity.Instituicao;
import com.studora.entity.Subtema;
import com.studora.entity.Tema;
import com.studora.repository.BancaRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoCargoSubtemaRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.CargoRepository;
import com.studora.repository.DisciplinaRepository;
import com.studora.repository.InstituicaoRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.repository.TemaRepository;
import com.studora.util.TestUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
    private ConcursoCargoSubtemaRepository concursoCargoSubtemaRepository;

    @Autowired
    private CacheManager cacheManager;

    private Cargo cargo1;
    private Cargo cargo2;

    @BeforeEach
    void setUp() {
        // Clear caches to avoid stale data from previous tests
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

        ConcursoCreateRequest concursoCreateRequest = new ConcursoCreateRequest();
        concursoCreateRequest.setInstituicaoId(instituicao.getId());
        concursoCreateRequest.setBancaId(banca.getId());
        concursoCreateRequest.setAno(2023);
        concursoCreateRequest.setMes(1);
        concursoCreateRequest.setCargos(List.of(cargo1.getId()));

        mockMvc
            .perform(
                post("/api/v1/concursos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(concursoCreateRequest))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.instituicao.id").value(instituicao.getId()))
            .andExpect(jsonPath("$.banca.id").value(banca.getId()))
            .andExpect(jsonPath("$.ano").value(2023))
            .andExpect(jsonPath("$.mes").value(1))
            .andExpect(jsonPath("$.cargos[0].cargoId").value(cargo1.getId()));
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

        // Create the first concurso
        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concursoRepository.save(concurso);

        // Try to create an identical one
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

        mockMvc
            .perform(get("/api/v1/concursos/{id}", concurso.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.instituicao.id").value(instituicao.getId()))
            .andExpect(jsonPath("$.banca.id").value(banca.getId()))
            .andExpect(jsonPath("$.ano").value(2023))
            .andExpect(jsonPath("$.mes").value(6))
            .andExpect(jsonPath("$.cargos[0].cargoId").value(cargo1.getId()));
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

        Instituicao instituicao2 = new Instituicao();
        instituicao2.setNome("Instituição All 2");
        instituicao2.setArea("Educação");
        instituicao2 = instituicaoRepository.save(instituicao2);

        Banca banca2 = new Banca();
        banca2.setNome("Banca All 2");
        banca2 = bancaRepository.save(banca2);

        concursoRepository.save(new Concurso(instituicao1, banca1, 2023, 1));
        concursoRepository.save(new Concurso(instituicao2, banca2, 2024, 2));

        mockMvc
            .perform(get("/api/v1/concursos"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.content.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                )
            );
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

        Instituicao instituicao2 = new Instituicao();
        instituicao2.setNome("Instituição Upd 2");
        instituicao2.setArea("Educação");
        instituicao2 = instituicaoRepository.save(instituicao2);

        Banca banca2 = new Banca();
        banca2.setNome("Banca Upd 2");
        banca2 = bancaRepository.save(banca2);

        Concurso concurso = new Concurso(instituicao1, banca1, 2022, 12);
        concurso = concursoRepository.save(concurso);
        
        // Initial cargo association
        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo1);
        concursoCargoRepository.save(cc);
        concurso.getConcursoCargos().add(cc);

        ConcursoUpdateRequest concursoUpdateRequest = new ConcursoUpdateRequest();
        concursoUpdateRequest.setInstituicaoId(instituicao2.getId());
        concursoUpdateRequest.setBancaId(banca2.getId());
        concursoUpdateRequest.setAno(2023);
        concursoUpdateRequest.setMes(6);
        concursoUpdateRequest.setCargos(List.of(cargo2.getId())); // Change cargo to cargo2

        mockMvc
            .perform(
                put("/api/v1/concursos/{id}", concurso.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(concursoUpdateRequest))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.instituicao.id").value(instituicao2.getId()))
            .andExpect(jsonPath("$.banca.id").value(banca2.getId()))
            .andExpect(jsonPath("$.ano").value(2023))
            .andExpect(jsonPath("$.mes").value(6))
            .andExpect(jsonPath("$.cargos[0].cargoId").value(cargo2.getId()))
            .andExpect(jsonPath("$.cargos.length()").value(1));
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

        Instituicao instituicao2 = new Instituicao();
        instituicao2.setNome("Instituição Filter 2");
        instituicao2.setArea("ADM");
        instituicao2 = instituicaoRepository.save(instituicao2);

        Banca banca2 = new Banca();
        banca2.setNome("Banca Filter 2");
        banca2 = bancaRepository.save(banca2);

        Concurso c1 = new Concurso(instituicao1, banca1, 2023, 1);
        ConcursoCargo cc1 = new ConcursoCargo();
        cc1.setCargo(cargo1); // Cargo 1: SUPERIOR, TI
        c1.addConcursoCargo(cc1);
        c1 = concursoRepository.save(c1);

        Concurso c2 = new Concurso(instituicao2, banca2, 2024, 2);
        ConcursoCargo cc2 = new ConcursoCargo();
        cc2.setCargo(cargo2); // Cargo 2: MEDIO, ADM
        c2.addConcursoCargo(cc2);
        c2 = concursoRepository.save(c2);

        // Filter by bancaId
        mockMvc
            .perform(get("/api/v1/concursos").param("bancaId", banca1.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].banca.id").value(banca1.getId()));

        // Filter by instituicaoId
        mockMvc
            .perform(get("/api/v1/concursos").param("instituicaoId", instituicao2.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].instituicao.id").value(instituicao2.getId()));

        // Filter by cargoId
        mockMvc
            .perform(get("/api/v1/concursos").param("cargoId", cargo1.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].cargos[0].cargoId").value(cargo1.getId()));

        // Filter by instituicaoArea
        mockMvc
            .perform(get("/api/v1/concursos").param("instituicaoArea", "TI"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].instituicao.area").value("TI"));

        // Filter by cargoArea
        mockMvc
            .perform(get("/api/v1/concursos").param("cargoArea", "ADM"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].cargos[0].area").value("ADM"));

        // Filter by cargoNivel
        mockMvc
            .perform(get("/api/v1/concursos").param("cargoNivel", "MEDIO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].cargos[0].nivel").value("MEDIO"));
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

        mockMvc
            .perform(delete("/api/v1/concursos/{id}", concurso.getId()))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/api/v1/concursos/{id}", concurso.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllConcursos_DefaultSorting() throws Exception {
        Instituicao i1 = new Instituicao(); i1.setNome("A-Inst"); i1.setArea("TI"); i1 = instituicaoRepository.save(i1);
        Instituicao i2 = new Instituicao(); i2.setNome("B-Inst"); i2.setArea("TI"); i2 = instituicaoRepository.save(i2);
        Banca b = new Banca(); b.setNome("Banca"); b = bancaRepository.save(b);

        // Save in mixed order
        concursoRepository.save(new Concurso(i1, b, 2023, 1));
        concursoRepository.save(new Concurso(i1, b, 2023, 5));
        concursoRepository.save(new Concurso(i2, b, 2022, 12));
        concursoRepository.save(new Concurso(i1, b, 2024, 1));

        // Default sort: ano DESC, mes DESC, inst ASC
        // Expected: 
        // 1. 2024, 1
        // 2. 2023, 5
        // 3. 2023, 1
        // 4. 2022, 12
        mockMvc
            .perform(get("/api/v1/concursos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].ano").value(2024))
            .andExpect(jsonPath("$.content[1].mes").value(5))
            .andExpect(jsonPath("$.content[2].mes").value(1))
            .andExpect(jsonPath("$.content[3].ano").value(2022));
    }

    @Test
    void testGetAllConcursos_CustomSortingByInstituicao() throws Exception {
        Instituicao i1 = new Instituicao(); i1.setNome("Z-Inst"); i1.setArea("TI"); i1 = instituicaoRepository.save(i1);
        Instituicao i2 = new Instituicao(); i2.setNome("A-Inst"); i2.setArea("TI"); i2 = instituicaoRepository.save(i2);
        Banca b = new Banca(); b.setNome("Banca"); b = bancaRepository.save(b);

        concursoRepository.save(new Concurso(i1, b, 2023, 1));
        concursoRepository.save(new Concurso(i2, b, 2024, 1));

        // Sort by instituicao ASC
        // Expected: A-Inst (2024), then Z-Inst (2023)
        mockMvc
            .perform(get("/api/v1/concursos").param("sort", "instituicao").param("direction", "ASC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].ano").value(2024))
            .andExpect(jsonPath("$.content[1].ano").value(2023));
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

        // Toggle to true
        mockMvc
            .perform(patch("/api/v1/concursos/cargos/{concursoCargoId}/inscricao", cc.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.inscrito").value(true));

        // Toggle to false
        mockMvc
            .perform(patch("/api/v1/concursos/cargos/{concursoCargoId}/inscricao", cc.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.inscrito").value(false));
    }

    @Test
    void testInscritoFilterAndPolymorphicProperty() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Filter Test");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Filter Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso = concursoRepository.save(concurso);
        
        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo1);
        cc.setInscrito(true);
        concurso.addConcursoCargo(cc);
        concurso = concursoRepository.save(concurso);

        // Test GET /concursos?inscrito=true
        mockMvc
            .perform(get("/api/v1/concursos").param("inscrito", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].cargos[0].cargoId").value(cargo1.getId()))
            .andExpect(jsonPath("$.content[0].cargos[0].inscrito").value(true));

        // Test GET /concursos?inscrito=false (should be empty if only one concurso exists and it's inscribed)
        mockMvc
            .perform(get("/api/v1/concursos").param("inscrito", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(0));
            
        // Test GET /concursos/{id}
        mockMvc
            .perform(get("/api/v1/concursos/{id}", concurso.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cargos[0].cargoId").value(cargo1.getId()))
            .andExpect(jsonPath("$.cargos[0].inscrito").value(true));
    }

    @Test
    void testOnlyOneCargoPerConcursoConstraint() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Constraint Test");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Constraint Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso = concursoRepository.save(concurso);
        
        ConcursoCargo cc1 = new ConcursoCargo();
        cc1.setConcurso(concurso);
        cc1.setCargo(cargo1);
        cc1.setInscrito(true);
        cc1 = concursoCargoRepository.save(cc1);

        ConcursoCargo cc2 = new ConcursoCargo();
        cc2.setConcurso(concurso);
        cc2.setCargo(cargo2);
        cc2.setInscrito(false);
        cc2 = concursoCargoRepository.save(cc2);

        // Try to toggle cc2 to true -> should fail
        mockMvc
            .perform(patch("/api/v1/concursos/cargos/{concursoCargoId}/inscricao", cc2.getId()))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value("Você já está inscrito em outro cargo para este concurso. Desinscreva-se primeiro."));
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
        cc = concursoCargoRepository.save(cc);
        concurso.addConcursoCargo(cc); // Bidirectional

        ConcursoCargoSubtema ccs1 = new ConcursoCargoSubtema();
        ccs1.setSubtema(subtema1);
        cc.addConcursoCargoSubtema(ccs1); // Bidirectional helper
        concursoCargoSubtemaRepository.save(ccs1);

        ConcursoCargoSubtema ccs2 = new ConcursoCargoSubtema();
        ccs2.setSubtema(subtema2);
        cc.addConcursoCargoSubtema(ccs2); // Bidirectional helper
        concursoCargoSubtemaRepository.save(ccs2);

        mockMvc
            .perform(get("/api/v1/concursos/{id}", concurso.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cargos[0].topicos.length()").value(2))
            .andExpect(jsonPath("$.cargos[0].topicos[0].nome").value("Atos Vinculados"))
            .andExpect(jsonPath("$.cargos[0].topicos[0].temaId").value(tema.getId()))
            .andExpect(jsonPath("$.cargos[0].topicos[0].temaNome").value("Poderes"))
            .andExpect(jsonPath("$.cargos[0].topicos[0].disciplinaId").value(disciplina.getId()))
            .andExpect(jsonPath("$.cargos[0].topicos[0].disciplinaNome").value("Direito Administrativo Topicos"))
            .andExpect(jsonPath("$.cargos[0].topicos[1].nome").value("Espécies de Atos"));
    }

    @Test
    void testGetAllConcursos_WithTopicos() throws Exception {
        Disciplina disciplina = new Disciplina("Direito Topicos List");
        disciplina = disciplinaRepository.save(disciplina);

        Tema tema = new Tema();
        tema.setNome("Tema Topicos List");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        Subtema subtema = new Subtema();
        subtema.setNome("Subtema Topicos List");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Topicos List");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Topicos List");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso = concursoRepository.save(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo1);
        cc = concursoCargoRepository.save(cc);
        concurso.addConcursoCargo(cc); // Bidirectional

        ConcursoCargoSubtema ccs = new ConcursoCargoSubtema();
        ccs.setSubtema(subtema);
        cc.addConcursoCargoSubtema(ccs); // Bidirectional helper
        concursoCargoSubtemaRepository.save(ccs);

        mockMvc
            .perform(get("/api/v1/concursos").param("instituicaoId", instituicao.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].cargos[0].topicos.length()").value(1))
            .andExpect(jsonPath("$.content[0].cargos[0].topicos[0].nome").value("Subtema Topicos List"))
            .andExpect(jsonPath("$.content[0].cargos[0].topicos[0].temaNome").value("Tema Topicos List"))
            .andExpect(jsonPath("$.content[0].cargos[0].topicos[0].disciplinaNome").value("Direito Topicos List"));
    }

    @Test
    void testCreateConcurso_TopicosIsEmpty() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Create Topicos");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Create Topicos");
        banca = bancaRepository.save(banca);

        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(instituicao.getId());
        request.setBancaId(banca.getId());
        request.setAno(2023);
        request.setMes(7);
        request.setCargos(List.of(cargo1.getId()));

        mockMvc
            .perform(
                post("/api/v1/concursos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cargos[0].topicos.length()").value(0));
    }

    @Test
    void testUpdateConcurso_TopicosIsEmpty() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Update Topicos");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Update Topicos");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2022, 12);
        concurso = concursoRepository.save(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo1);
        cc = concursoCargoRepository.save(cc);
        concurso.addConcursoCargo(cc);

        ConcursoUpdateRequest request = new ConcursoUpdateRequest();
        request.setInstituicaoId(instituicao.getId());
        request.setBancaId(banca.getId());
        request.setAno(2023);
        request.setMes(12);
        request.setCargos(List.of(cargo1.getId()));

        mockMvc
            .perform(
                put("/api/v1/concursos/{id}", concurso.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cargos[0].topicos.length()").value(0));
    }

    @Test
    void testDeleteConcurso_CascadeToTopicos() throws Exception {
        Disciplina disciplina = new Disciplina("Direito Cascade");
        disciplina = disciplinaRepository.save(disciplina);

        Tema tema = new Tema();
        tema.setNome("Tema Cascade");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        Subtema subtema = new Subtema();
        subtema.setNome("Subtema Cascade");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Cascade");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Cascade");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 1);
        concurso = concursoRepository.save(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo1);
        cc = concursoCargoRepository.save(cc);
        concurso.addConcursoCargo(cc);

        ConcursoCargoSubtema ccs = new ConcursoCargoSubtema();
        ccs.setSubtema(subtema);
        cc.addConcursoCargoSubtema(ccs);
        concursoCargoSubtemaRepository.save(ccs);
        long ccsId = ccs.getId();

        mockMvc
            .perform(delete("/api/v1/concursos/{id}", concurso.getId()))
            .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        assertFalse(concursoCargoSubtemaRepository.existsById(ccsId));
    }

    @Test
    void testCreateConcurso_WithTopicos() throws Exception {
        Disciplina disciplina = new Disciplina("Direito Topicos Create");
        disciplina = disciplinaRepository.save(disciplina);

        Tema tema = new Tema();
        tema.setNome("Tema Topicos Create");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        Subtema subtema = new Subtema();
        subtema.setNome("Subtema Topicos Create");
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Topicos Create");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Topicos Create");
        banca = bancaRepository.save(banca);

        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(instituicao.getId());
        request.setBancaId(banca.getId());
        request.setAno(2023);
        request.setMes(3);
        request.setCargos(List.of(cargo1.getId()));
        request.setTopicos(Map.of(subtema.getId(), List.of(cargo1.getId())));

        mockMvc
            .perform(
                post("/api/v1/concursos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cargos[0].topicos.length()").value(1))
            .andExpect(jsonPath("$.cargos[0].topicos[0].id").value(subtema.getId()))
            .andExpect(jsonPath("$.cargos[0].topicos[0].nome").value("Subtema Topicos Create"));
    }

    @Test
    void testCreateConcurso_TopicosSubtemaNotFound() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Topicos 404");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Topicos 404");
        banca = bancaRepository.save(banca);

        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(instituicao.getId());
        request.setBancaId(banca.getId());
        request.setAno(2023);
        request.setMes(4);
        request.setCargos(List.of(cargo1.getId()));
        request.setTopicos(Map.of(99999L, List.of(cargo1.getId())));

        mockMvc
            .perform(
                post("/api/v1/concursos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Não foi possível encontrar Subtema com ID: '99999'"));
    }

    @Test
    void testCreateConcurso_TopicosCargoNotInConcurso() throws Exception {
        Subtema subtema = new Subtema();
        subtema.setNome("Subtema Topicos Invalid Cargo");
        Tema tema = new Tema();
        tema.setNome("Tema Temp");
        tema.setDisciplina(new Disciplina("Disc Temp"));
        disciplinaRepository.save(tema.getDisciplina());
        tema = temaRepository.save(tema);
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Topicos Invalid");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Topicos Invalid");
        banca = bancaRepository.save(banca);

        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(instituicao.getId());
        request.setBancaId(banca.getId());
        request.setAno(2023);
        request.setMes(8);
        request.setCargos(List.of(cargo1.getId()));
        request.setTopicos(Map.of(subtema.getId(), List.of(99999L)));

        mockMvc
            .perform(
                post("/api/v1/concursos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value("O cargo ID 99999 não está associado a este concurso."));
    }

    @Test
    void testCreateConcurso_TopicosEmptyCargoList() throws Exception {
        Subtema subtema = new Subtema();
        subtema.setNome("Subtema Topicos Empty");
        Tema tema = new Tema();
        tema.setNome("Tema Temp 2");
        tema.setDisciplina(new Disciplina("Disc Temp 2"));
        disciplinaRepository.save(tema.getDisciplina());
        tema = temaRepository.save(tema);
        subtema.setTema(tema);
        subtema = subtemaRepository.save(subtema);

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Topicos Empty");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Topicos Empty");
        banca = bancaRepository.save(banca);

        ConcursoCreateRequest request = new ConcursoCreateRequest();
        request.setInstituicaoId(instituicao.getId());
        request.setBancaId(banca.getId());
        request.setAno(2023);
        request.setMes(9);
        request.setCargos(List.of(cargo1.getId()));
        request.setTopicos(Map.of(subtema.getId(), List.of()));

        mockMvc
            .perform(
                post("/api/v1/concursos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").value("O subtema ID " + subtema.getId() + " deve estar associado a pelo menos 1 cargo."));
    }

    @Test
    void testUpdateConcurso_ModifyTopicos() throws Exception {
        Disciplina disciplina = new Disciplina("Direito Topicos Update");
        disciplina = disciplinaRepository.save(disciplina);

        Tema tema = new Tema();
        tema.setNome("Tema Topicos Update");
        tema.setDisciplina(disciplina);
        tema = temaRepository.save(tema);

        Subtema subtema1 = new Subtema();
        subtema1.setNome("Subtema Update 1");
        subtema1.setTema(tema);
        subtema1 = subtemaRepository.save(subtema1);

        Subtema subtema2 = new Subtema();
        subtema2.setNome("Subtema Update 2");
        subtema2.setTema(tema);
        subtema2 = subtemaRepository.save(subtema2);

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Topicos Update");
        instituicao.setArea("Educação");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Topicos Update");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023, 10);
        concurso = concursoRepository.save(concurso);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo1);
        cc = concursoCargoRepository.save(cc);
        concurso.addConcursoCargo(cc);

        // Create initial topicos
        ConcursoCargoSubtema ccs = new ConcursoCargoSubtema();
        ccs.setSubtema(subtema1);
        cc.addConcursoCargoSubtema(ccs);
        concursoCargoSubtemaRepository.save(ccs);

        // Update: remove subtema1, add subtema2
        ConcursoUpdateRequest request = new ConcursoUpdateRequest();
        request.setInstituicaoId(instituicao.getId());
        request.setBancaId(banca.getId());
        request.setAno(2023);
        request.setMes(10);
        request.setCargos(List.of(cargo1.getId()));
        request.setTopicos(Map.of(subtema2.getId(), List.of(cargo1.getId())));

        mockMvc
            .perform(
                put("/api/v1/concursos/{id}", concurso.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cargos[0].topicos.length()").value(1))
            .andExpect(jsonPath("$.cargos[0].topicos[0].id").value(subtema2.getId()))
            .andExpect(jsonPath("$.cargos[0].topicos[0].nome").value("Subtema Update 2"));

        entityManager.flush();
        entityManager.clear();

        // Verify old topicos was removed
        assertFalse(concursoCargoSubtemaRepository.existsById(ccs.getId()));
    }
}