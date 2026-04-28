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
import com.studora.entity.Disciplina;
import com.studora.entity.Tema;
import com.studora.entity.Subtema;
import com.studora.entity.Questao;
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
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private SubtemaRepository subtemaRepository;
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
        cargo = cargoRepository.save(cargo);

        Disciplina disciplina = new Disciplina("Disciplina Cascade");
        disciplinaRepository.save(disciplina);
        Tema tema = new Tema(disciplina, "Tema Cascade");
        temaRepository.save(tema);
        Subtema subtema = new Subtema(tema, "Subtema Cascade");
        subtema = subtemaRepository.save(subtema);
        
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
        qReq.setSubtemaIds(List.of(subtema.getId()));
        
        AlternativaCreateRequest alt1 = new AlternativaCreateRequest();
        alt1.setOrdem(1);
        alt1.setTexto("Alt 1");
        alt1.setCorreta(true);
        
        AlternativaCreateRequest alt2 = new AlternativaCreateRequest();
        alt2.setOrdem(2);
        alt2.setTexto("Alt 2");
        alt2.setCorreta(false);
        
        qReq.setAlternativas(Arrays.asList(alt1, alt2));
        
        Long questaoId = questaoService.create(qReq);
        QuestaoDetailDto savedQ = questaoService.getQuestaoDetailById(questaoId);
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

    @Test
    void testDeleteQuestaoShouldNotDeleteSubtema() {
        // 1. Setup Hierarchy
        Instituicao inst = new Instituicao();
        inst.setNome("PF");
        inst.setArea("Policial");
        inst = instituicaoRepository.save(inst);

        Banca banca = new Banca();
        banca.setNome("Cebraspe");
        banca = bancaRepository.save(banca);

        Concurso concurso = concursoRepository.save(new Concurso(inst, banca, 2024, 1));
        
        Cargo cargo = new Cargo();
        cargo.setNome("Agente");
        cargo.setNivel(NivelCargo.SUPERIOR);
        cargo.setArea("Policial");
        cargo = cargoRepository.save(cargo);
        
        ConcursoCargo cc = new ConcursoCargo();
        cc.setConcurso(concurso);
        cc.setCargo(cargo);
        cc = concursoCargoRepository.save(cc);

        Disciplina disc = new Disciplina("Direito");
        disc = disciplinaRepository.save(disc);
        Tema tema = new Tema(disc, "Administrativo");
        tema = temaRepository.save(tema);
        Subtema subtema = new Subtema(tema, "Atos");
        subtema = subtemaRepository.save(subtema);
        Long subtemaId = subtema.getId();

        // 2. Create Questao
        Questao questao = new Questao(concurso, "Questao de teste");
        questao.getSubtemas().add(subtema);
        questao = questaoRepository.save(questao);
        Long questaoId = questao.getId();

        // Verify setup
        assertTrue(questaoRepository.existsById(questaoId));
        assertTrue(subtemaRepository.existsById(subtemaId));

        // 3. Delete Questao
        questaoRepository.deleteById(questaoId);
        entityManager.flush();
        entityManager.clear();

        // 4. Check results
        assertFalse(questaoRepository.existsById(questaoId), "Questao should be deleted");
        assertTrue(subtemaRepository.existsById(subtemaId), "Subtema should NOT be deleted when Questao is deleted!");
    }
}
