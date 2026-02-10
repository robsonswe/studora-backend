package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.entity.Banca;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Cargo;
import com.studora.entity.Instituicao;
import com.studora.repository.BancaRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.CargoRepository;
import com.studora.repository.InstituicaoRepository;
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
class ConcursoControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    private Cargo cargo1;
    private Cargo cargo2;

    @BeforeEach
    void setUp() {
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
            .andExpect(jsonPath("$.cargos[0].id").value(cargo1.getId()));
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
            .andExpect(jsonPath("$.cargos[0].id").value(cargo1.getId()));
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
            .andExpect(jsonPath("$.cargos[0].id").value(cargo2.getId()))
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
            .andExpect(jsonPath("$.content[0].cargos[0].id").value(cargo1.getId()));

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
}