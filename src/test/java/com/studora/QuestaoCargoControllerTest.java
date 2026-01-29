package com.studora;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.studora.dto.QuestaoCargoDto;
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
class QuestaoCargoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestaoCargoRepository questaoCargoRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private ConcursoCargoRepository concursoCargoRepository;

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private BancaRepository bancaRepository;

    @Autowired
    private CargoRepository cargoRepository;

    private Questao questao;
    private ConcursoCargo concursoCargo;

    @BeforeEach
    void setUp() {
        Instituicao inst = new Instituicao();
        inst.setNome("Inst QC");
        inst = instituicaoRepository.save(inst);

        Banca banca = new Banca();
        banca.setNome("Banca QC");
        banca = bancaRepository.save(banca);

        Concurso concurso = concursoRepository.save(
            new Concurso(inst, banca, 2025)
        );

        questao = new Questao(concurso, "Enunciado QC");
        questao = questaoRepository.save(questao);

        Cargo cargo = new Cargo();
        cargo.setNome("Cargo QC");
        cargo = cargoRepository.save(cargo);

        concursoCargo = new ConcursoCargo();
        concursoCargo.setConcurso(concurso);
        concursoCargo.setCargo(cargo);
        concursoCargo = concursoCargoRepository.save(concursoCargo);
    }

    @Test
    void testCreateQuestaoCargo() throws Exception {
        QuestaoCargoDto dto = new QuestaoCargoDto();
        dto.setQuestaoId(questao.getId());
        dto.setConcursoCargoId(concursoCargo.getId());

        mockMvc
            .perform(
                post("/api/questao-cargos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.asJsonString(dto))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.questaoId").value(questao.getId()));
    }
}
