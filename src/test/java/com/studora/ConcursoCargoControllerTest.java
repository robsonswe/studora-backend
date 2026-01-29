package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.ConcursoCargoDto;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ConcursoCargoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConcursoCargoRepository concursoCargoRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private BancaRepository bancaRepository;

    private Concurso concurso;
    private Cargo cargo;

    @BeforeEach
    void setUp() {
        Instituicao inst = new Instituicao();
        inst.setNome("Inst CC Test");
        inst = instituicaoRepository.save(inst);

        Banca banca = new Banca();
        banca.setNome("Banca CC Test");
        banca = bancaRepository.save(banca);

        concurso = concursoRepository.save(new Concurso(inst, banca, 2024));

        cargo = new Cargo();
        cargo.setNome("Cargo CC Test");
        cargo = cargoRepository.save(cargo);
    }

    @Test
    void testCreateConcursoCargo() throws Exception {
        ConcursoCargoDto dto = new ConcursoCargoDto();
        dto.setConcursoId(concurso.getId());
        dto.setCargoId(cargo.getId());

        mockMvc
            .perform(
                post("/api/concurso-cargos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(dto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.concursoId").value(concurso.getId()));
    }
}
