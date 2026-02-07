package com.studora;

import static org.junit.jupiter.api.Assertions.*;

import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.AlternativaCreateRequest;
import com.studora.dto.request.RespostaCreateRequest;
import com.studora.entity.Banca;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Cargo;
import com.studora.entity.Instituicao;
import com.studora.entity.NivelCargo;
import com.studora.repository.*;
import com.studora.service.ConcursoService;
import com.studora.service.QuestaoService;
import com.studora.service.RespostaService;
import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConcursoCascadeDeletionTest {

    @Autowired private ConcursoService concursoService;
    @Autowired private QuestaoService questaoService;
    @Autowired private RespostaService respostaService;
    
    @Autowired private InstituicaoRepository instituicaoRepository;
    @Autowired private BancaRepository bancaRepository;
    @Autowired private CargoRepository cargoRepository;
    @Autowired private ConcursoRepository concursoRepository;
    @Autowired private ConcursoCargoRepository concursoCargoRepository;
    @Autowired private QuestaoRepository questaoRepository;
    @Autowired private AlternativaRepository alternativaRepository;
    @Autowired private RespostaRepository respostaRepository;
    @Autowired private QuestaoCargoRepository questaoCargoRepository;
    @Autowired private EntityManager entityManager;

    @Test
    void testCascadeDeleteConcurso() {
        // 1. Setup Hierarchy
        Instituicao inst = new Instituicao();
        inst.setNome("Inst Test");
        inst.setArea("Educação");
        instituicaoRepository.save(inst);
        
        Banca banca = new Banca();
        banca.setNome("Banca Test");
        bancaRepository.save(banca);
        
        Cargo cargo = new Cargo();
        cargo.setNome("Cargo Test");
        cargo.setNivel(NivelCargo.SUPERIOR);
        cargo.setArea("TI");
        cargoRepository.save(cargo);
        
        Concurso concurso = new Concurso(inst, banca, 2023, 1);
        concurso = concursoRepository.save(concurso);
        Long concursoId = concurso.getId();
        
        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo);
        cc = concursoCargoRepository.save(cc);
        Long concursoCargoId = cc.getId();

        // 4. Create a Questao with Alternativas and Cargo
        QuestaoCreateRequest qReq = new QuestaoCreateRequest();
        qReq.setConcursoId(concursoId);
        qReq.setEnunciado("Enunciado Test");
        qReq.setAnulada(false);
        qReq.setCargos(List.of(cargo.getId()));
        
        AlternativaCreateRequest alt1 = new AlternativaCreateRequest();
        alt1.setOrdem(1);
        alt1.setTexto("Alt 1");
        alt1.setCorreta(true);
        
        AlternativaCreateRequest alt2 = new AlternativaCreateRequest();
        alt2.setOrdem(2);
        alt2.setTexto("Alt 2");
        alt2.setCorreta(false);
        
        qReq.setAlternativas(Arrays.asList(alt1, alt2));
        
        QuestaoDetailDto savedQ = questaoService.create(qReq);
        Long questaoId = savedQ.getId();
        Long alternativaId = savedQ.getAlternativas().get(0).getId();

        // 5. Create a Resposta
        RespostaCreateRequest respReq = new RespostaCreateRequest();
        respReq.setQuestaoId(questaoId);
        respReq.setAlternativaId(alternativaId);
        respReq.setJustificativa("Test justification");
        respReq.setDificuldadeId(1);
        respostaService.createResposta(respReq);

        // Verify setup
        assertTrue(concursoRepository.existsById(concursoId));
        assertTrue(questaoRepository.existsById(questaoId));
        assertTrue(concursoCargoRepository.existsById(concursoCargoId));
        assertFalse(alternativaRepository.findByQuestaoIdOrderByOrdemAsc(questaoId).isEmpty());
        assertTrue(respostaRepository.findFirstByQuestaoIdOrderByCreatedAtDesc(questaoId).isPresent());

        // 6. ACTION: Delete the Concurso
        concursoService.delete(concursoId);
        entityManager.flush();
        entityManager.clear();

        // 7. VERIFY CASCADE
        assertFalse(concursoRepository.existsById(concursoId), "Concurso should be deleted");
        assertFalse(questaoRepository.existsByConcursoId(concursoId), "Questao should be deleted via cascade");
        assertFalse(concursoCargoRepository.existsByConcursoId(concursoId), "ConcursoCargo should be deleted via cascade");
        
        // Verify Questao components are also gone (cascade from Questao)
        assertTrue(alternativaRepository.findByQuestaoIdOrderByOrdemAsc(questaoId).isEmpty(), "Alternativas should be deleted");
        assertTrue(respostaRepository.findFirstByQuestaoIdOrderByCreatedAtDesc(questaoId).isEmpty(), "Resposta should be deleted");
        assertTrue(questaoCargoRepository.findByQuestaoId(questaoId).isEmpty(), "QuestaoCargo associations should be deleted");
    }
}
