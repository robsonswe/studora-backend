package com.studora;

import static org.junit.jupiter.api.Assertions.*;

import com.studora.dto.AlternativaDto;
import com.studora.dto.QuestaoDto;
import com.studora.entity.*;
import com.studora.repository.*;
import com.studora.service.ConcursoService;
import com.studora.service.QuestaoService;
import com.studora.service.RespostaService;
import com.studora.dto.request.RespostaCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConcursoCascadeDeletionTest {

    @Autowired
    private ConcursoService concursoService;

    @Autowired
    private QuestaoService questaoService;

    @Autowired
    private RespostaService respostaService;

    @Autowired
    private ConcursoRepository concursoRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private RespostaRepository respostaRepository;

    @Autowired
    private ConcursoCargoRepository concursoCargoRepository;

    @Autowired
    private QuestaoCargoRepository questaoCargoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private BancaRepository bancaRepository;

    @Autowired
    private CargoRepository cargoRepository;

    @Test
    void testDeleteConcursoCascadesToEverything() {
        // 1. Setup metadata
        Instituicao inst = new Instituicao();
        inst.setNome("Inst Test");
        inst = instituicaoRepository.save(inst);

        Banca banca = new Banca();
        banca.setNome("Banca Test");
        banca = bancaRepository.save(banca);

        Cargo cargo = new Cargo();
        cargo.setNome("Cargo Test");
        cargo.setNivel(NivelCargo.SUPERIOR);
        cargo.setArea("TI");
        cargo = cargoRepository.save(cargo);

        // 2. Create Concurso
        Concurso concurso = new Concurso(inst, banca, 2023, 6);
        concurso = concursoRepository.save(concurso);
        Long concursoId = concurso.getId();

        // 3. Associate Cargo to Concurso
        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo);
        cc = concursoCargoRepository.save(cc);
        Long concursoCargoId = cc.getId();

        // 4. Create Questao using QuestaoService (to handle complex associations)
        QuestaoDto qDto = new QuestaoDto();
        qDto.setConcursoId(concursoId);
        qDto.setEnunciado("Enunciado Test");
        qDto.setAnulada(false);
        qDto.setConcursoCargoIds(List.of(concursoCargoId));
        
        AlternativaDto alt1 = new AlternativaDto();
        alt1.setOrdem(1);
        alt1.setTexto("Alt 1");
        alt1.setCorreta(true);
        
        AlternativaDto alt2 = new AlternativaDto();
        alt2.setOrdem(2);
        alt2.setTexto("Alt 2");
        alt2.setCorreta(false);
        
        qDto.setAlternativas(Arrays.asList(alt1, alt2));
        
        QuestaoDto savedQ = questaoService.createQuestao(qDto);
        Long questaoId = savedQ.getId();
        Long alternativaId = savedQ.getAlternativas().get(0).getId();

        // 5. Create a Resposta
        RespostaCreateRequest respReq = new RespostaCreateRequest();
        respReq.setQuestaoId(questaoId);
        respReq.setAlternativaId(alternativaId);
        respostaService.createResposta(respReq);

        // Verify setup
        assertTrue(concursoRepository.existsById(concursoId));
        assertTrue(questaoRepository.existsById(questaoId));
        assertTrue(concursoCargoRepository.existsById(concursoCargoId));
        assertFalse(alternativaRepository.findByQuestaoIdOrderByOrdemAsc(questaoId).isEmpty());
        assertNotNull(respostaRepository.findByQuestaoId(questaoId));

        // 6. ACTION: Delete the Concurso
        concursoService.deleteById(concursoId);

        // 7. VERIFY CASCADE
        assertFalse(concursoRepository.existsById(concursoId), "Concurso should be deleted");
        assertFalse(questaoRepository.existsByConcursoId(concursoId), "Questao should be deleted via cascade");
        assertFalse(concursoCargoRepository.existsByConcursoId(concursoId), "ConcursoCargo should be deleted via cascade");
        
        // Verify Questao components are also gone (cascade from Questao)
        assertTrue(alternativaRepository.findByQuestaoIdOrderByOrdemAsc(questaoId).isEmpty(), "Alternativas should be deleted");
        assertNull(respostaRepository.findByQuestaoId(questaoId), "Resposta should be deleted");
        assertTrue(questaoCargoRepository.findByQuestaoId(questaoId).isEmpty(), "QuestaoCargo associations should be deleted");
    }
}
