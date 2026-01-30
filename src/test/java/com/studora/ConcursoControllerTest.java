package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.ConcursoDto;
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

    @BeforeEach
    void setUp() {
        // No deleteAll necessary with @Transactional
    }

    @Test
    void testCreateConcurso() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Create Test");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Create Test");
        banca = bancaRepository.save(banca);

        ConcursoDto concursoDto = new ConcursoDto();
        concursoDto.setInstituicaoId(instituicao.getId());
        concursoDto.setBancaId(banca.getId());
        concursoDto.setAno(2023);

        mockMvc
            .perform(
                post("/api/concursos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(concursoDto))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.instituicaoId").value(instituicao.getId()))
            .andExpect(jsonPath("$.bancaId").value(banca.getId()))
            .andExpect(jsonPath("$.ano").value(2023));
    }

    @Test
    void testGetConcursoById() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Get Test");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Get Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023);
        concurso = concursoRepository.save(concurso);

        mockMvc
            .perform(get("/api/concursos/{id}", concurso.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.instituicaoId").value(instituicao.getId()))
            .andExpect(jsonPath("$.bancaId").value(banca.getId()))
            .andExpect(jsonPath("$.ano").value(2023));
    }

    @Test
    void testGetConcursoById_NotFound() throws Exception {
        mockMvc
            .perform(get("/api/concursos/{id}", 99999L))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllConcursos() throws Exception {
        Instituicao instituicao1 = new Instituicao();
        instituicao1.setNome("Instituição All 1");
        instituicao1 = instituicaoRepository.save(instituicao1);

        Banca banca1 = new Banca();
        banca1.setNome("Banca All 1");
        banca1 = bancaRepository.save(banca1);

        Instituicao instituicao2 = new Instituicao();
        instituicao2.setNome("Instituição All 2");
        instituicao2 = instituicaoRepository.save(instituicao2);

        Banca banca2 = new Banca();
        banca2.setNome("Banca All 2");
        banca2 = bancaRepository.save(banca2);

        concursoRepository.save(new Concurso(instituicao1, banca1, 2023));
        concursoRepository.save(new Concurso(instituicao2, banca2, 2024));

        mockMvc
            .perform(get("/api/concursos"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.length()").value(
                    org.hamcrest.Matchers.greaterThanOrEqualTo(2)
                )
            );
    }

    @Test
    void testUpdateConcurso() throws Exception {
        Instituicao instituicao1 = new Instituicao();
        instituicao1.setNome("Instituição Upd 1");
        instituicao1 = instituicaoRepository.save(instituicao1);

        Banca banca1 = new Banca();
        banca1.setNome("Banca Upd 1");
        banca1 = bancaRepository.save(banca1);

        Instituicao instituicao2 = new Instituicao();
        instituicao2.setNome("Instituição Upd 2");
        instituicao2 = instituicaoRepository.save(instituicao2);

        Banca banca2 = new Banca();
        banca2.setNome("Banca Upd 2");
        banca2 = bancaRepository.save(banca2);

        Concurso concurso = new Concurso(instituicao1, banca1, 2022);
        concurso = concursoRepository.save(concurso);

        ConcursoDto updatedDto = new ConcursoDto();
        updatedDto.setInstituicaoId(instituicao2.getId());
        updatedDto.setBancaId(banca2.getId());
        updatedDto.setAno(2023);

        mockMvc
            .perform(
                put("/api/concursos/{id}", concurso.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(updatedDto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.instituicaoId").value(instituicao2.getId()))
            .andExpect(jsonPath("$.bancaId").value(banca2.getId()))
            .andExpect(jsonPath("$.ano").value(2023));
    }

    @Test
    void testDeleteConcurso() throws Exception {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Del Test");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Del Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = new Concurso(instituicao, banca, 2023);
        concurso = concursoRepository.save(concurso);

        mockMvc
            .perform(delete("/api/concursos/{id}", concurso.getId()))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/api/concursos/{id}", concurso.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void testRemoveCargoFromConcurso_UnprocessableEntity_NoRemainingCargo() throws Exception {
        // Create instituicao and banca
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Test");
        instituicao = instituicaoRepository.save(instituicao);

        Banca banca = new Banca();
        banca.setNome("Banca Test");
        banca = bancaRepository.save(banca);

        // Create concurso
        Concurso concurso = new Concurso(instituicao, banca, 2023);
        concurso = concursoRepository.save(concurso);

        // Create cargo
        Cargo cargo = new Cargo();
        cargo.setNome("Cargo Test");
        cargo = cargoRepository.save(cargo);

        // Create concurso-cargo association
        ConcursoCargo concursoCargo = new ConcursoCargo();
        concursoCargo.setConcurso(concurso);
        concursoCargo.setCargo(cargo);
        concursoCargo = concursoCargoRepository.save(concursoCargo);

        // Try to remove the only cargo association (should fail with 422)
        mockMvc
            .perform(delete("/api/concursos/{concursoId}/cargos/{cargoId}", concurso.getId(), cargo.getId()))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Entidade não processável"))
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.detail").value("Um concurso deve estar associado a pelo menos um cargo"));
    }
}