package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoCargoDto;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.questao.QuestaoFilter;
import com.studora.dto.request.AlternativaCreateRequest;
import com.studora.dto.request.AlternativaUpdateRequest;
import com.studora.dto.request.QuestaoCargoCreateRequest;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Questao;
import com.studora.entity.QuestaoCargo;
import com.studora.entity.Resposta;
import com.studora.entity.Alternativa;
import com.studora.exception.ValidationException;
import com.studora.repository.*;
import com.studora.service.QuestaoService;
import com.studora.mapper.*;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

class QuestaoServiceTest {

    @Mock private QuestaoRepository questaoRepository;
    @Mock private ConcursoRepository concursoRepository;
    @Mock private SubtemaRepository subtemaRepository;
    @Mock private ConcursoCargoRepository concursoCargoRepository;
    @Mock private QuestaoCargoRepository questaoCargoRepository;
    @Mock private RespostaRepository respostaRepository;
    @Mock private AlternativaRepository alternativaRepository;
    @Mock private EntityManager entityManager;

    private QuestaoService questaoService;

    // Mappers
    private QuestaoMapper questaoMapper;
    private AlternativaMapper alternativaMapper;
    private QuestaoCargoMapper questaoCargoMapper;
    private RespostaMapper respostaMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        questaoMapper = org.mapstruct.factory.Mappers.getMapper(QuestaoMapper.class);
        questaoCargoMapper = org.mapstruct.factory.Mappers.getMapper(QuestaoCargoMapper.class);
        alternativaMapper = org.mapstruct.factory.Mappers.getMapper(AlternativaMapper.class);
        SubtemaMapper subtemaMapper = org.mapstruct.factory.Mappers.getMapper(SubtemaMapper.class);
        respostaMapper = org.mapstruct.factory.Mappers.getMapper(RespostaMapper.class);
        
        ReflectionTestUtils.setField(questaoMapper, "alternativaMapper", alternativaMapper);
        ReflectionTestUtils.setField(questaoMapper, "subtemaMapper", subtemaMapper);
        ReflectionTestUtils.setField(questaoMapper, "questaoCargoMapper", questaoCargoMapper);
        ReflectionTestUtils.setField(questaoMapper, "respostaMapper", respostaMapper);

        questaoService = new QuestaoService(
            questaoRepository, concursoRepository, subtemaRepository,
            concursoCargoRepository, questaoCargoRepository, respostaRepository,
            alternativaRepository,
            questaoMapper, questaoCargoMapper, entityManager
        );
    }

    @Test
    void testFindAll() {
        Questao q1 = new Questao(); q1.setId(1L);
        Page<Questao> page = new PageImpl<>(Collections.singletonList(q1));
        when(questaoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        Page<QuestaoSummaryDto> result = questaoService.findAll(new QuestaoFilter(), Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindById() {
        Concurso c = new Concurso(); c.setId(1L);
        Questao q = new Questao(); q.setId(1L); q.setEnunciado("Test?"); q.setConcurso(c);
        when(questaoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(q));
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoDetailDto result = questaoService.getQuestaoDetailById(1L);
        assertNotNull(result);
        assertEquals("Test?", result.getEnunciado());
    }

    @Test
    void testCreate_Success() {
        Concurso c = new Concurso(); c.setId(1L);
        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setConcursoId(1L);
        req.setEnunciado("New?");
        req.setAlternativas(Arrays.asList(
            new AlternativaCreateRequest(1, "A", true),
            new AlternativaCreateRequest(2, "B", false)
        ));
        req.setConcursoCargoIds(Collections.singletonList(100L));

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(concursoCargoRepository.findById(100L)).thenReturn(Optional.of(createConcursoCargo(100L, c)));
        when(questaoRepository.save(any(Questao.class))).thenAnswer(i -> {
            Questao q = i.getArgument(0);
            q.setId(1L);
            return q;
        });
        
        Questao savedQ = new Questao(); savedQ.setId(1L); savedQ.setEnunciado("New?"); savedQ.setConcurso(c);
        when(questaoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(savedQ));
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoDetailDto result = questaoService.create(req);
        assertEquals(1L, result.getId());
        verify(entityManager).flush();
    }

    @Test
    void testUpdate_Success() {
        Long id = 1L;
        Concurso c = new Concurso(); c.setId(1L);
        Questao existing = new Questao(); existing.setId(id); existing.setEnunciado("Old"); existing.setConcurso(c);
        
        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setConcursoId(1L);
        req.setEnunciado("Old");
        req.setAnulada(false);
        req.setAlternativas(Arrays.asList(
            new AlternativaUpdateRequest() {{ setTexto("A"); setCorreta(true); setOrdem(1); }},
            new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(2); }}
        ));
        req.setConcursoCargoIds(Collections.singletonList(100L));

        when(questaoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(concursoCargoRepository.findById(100L)).thenReturn(Optional.of(createConcursoCargo(100L, c)));
        when(questaoRepository.save(any())).thenReturn(existing);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoDetailDto result = questaoService.update(id, req);
        assertNotNull(result);
    }

    @Test
    void testDelete() {
        when(questaoRepository.existsById(1L)).thenReturn(true);
        questaoService.delete(1L);
        verify(questaoRepository).deleteById(1L);
    }

    @Test
    void testToggleDesatualizada() {
        Questao q = new Questao(); q.setId(1L); q.setDesatualizada(false);
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(q));
        questaoService.toggleDesatualizada(1L);
        assertTrue(q.getDesatualizada());
        verify(questaoRepository).save(q);
    }

    @Test
    void testGetCargosByQuestaoId() {
        QuestaoCargo qc = new QuestaoCargo(); qc.setId(10L);
        when(questaoCargoRepository.findByQuestaoId(1L)).thenReturn(Collections.singletonList(qc));
        List<QuestaoCargoDto> result = questaoService.getCargosByQuestaoId(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testAddCargo_Success() {
        Questao q = new Questao(); q.setId(1L); q.setConcurso(new Concurso()); q.getConcurso().setId(1L);
        ConcursoCargo cc = new ConcursoCargo(); cc.setId(100L); cc.setConcurso(q.getConcurso());
        
        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(1L, 100L)).thenReturn(Collections.emptyList());
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(q));
        when(concursoCargoRepository.findById(100L)).thenReturn(Optional.of(cc));
        when(questaoCargoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        QuestaoCargoCreateRequest req = new QuestaoCargoCreateRequest();
        req.setConcursoCargoId(100L);
        QuestaoCargoDto result = questaoService.addCargoToQuestao(1L, req);
        assertNotNull(result);
    }

    @Test
    void testRemoveCargo_Success() {
        QuestaoCargo qc = new QuestaoCargo();
        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(1L, 100L)).thenReturn(Collections.singletonList(qc));
        when(questaoCargoRepository.countByQuestaoId(1L)).thenReturn(2L);
        
        questaoService.removeCargoFromQuestao(1L, 100L);
        verify(questaoCargoRepository).delete(qc);
    }

    @Test
    void testRemoveCargo_FailsIfLast() {
        when(questaoCargoRepository.findByQuestaoIdAndConcursoCargoId(1L, 100L)).thenReturn(Collections.singletonList(new QuestaoCargo()));
        when(questaoCargoRepository.countByQuestaoId(1L)).thenReturn(1L);
        
        assertThrows(ValidationException.class, () -> questaoService.removeCargoFromQuestao(1L, 100L));
    }

    @Test
    void testCreate_RequiresAtLeastTwoAlternatives() {
        QuestaoCreateRequest req = new QuestaoCreateRequest(1L, "E");
        req.setAlternativas(Collections.singletonList(new AlternativaCreateRequest(1, "A", true)));
        req.setConcursoCargoIds(Collections.singletonList(100L));
        Concurso c = new Concurso(); c.setId(1L);
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(concursoCargoRepository.findById(100L)).thenReturn(Optional.of(createConcursoCargo(100L, c)));
        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testCreate_RequiresExactlyOneCorrect() {
        QuestaoCreateRequest req = new QuestaoCreateRequest(1L, "E");
        req.setConcursoCargoIds(Collections.singletonList(100L));
        req.setAlternativas(Arrays.asList(new AlternativaCreateRequest(1, "A", true), new AlternativaCreateRequest(2, "B", true)));
        Concurso c = new Concurso(); c.setId(1L);
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(concursoCargoRepository.findById(100L)).thenReturn(Optional.of(createConcursoCargo(100L, c)));
        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testUpdate_PreservesIds() {
        Long qId = 1L; Questao q = new Questao(); q.setId(qId); 
        Concurso c = new Concurso(); c.setId(1L); q.setConcurso(c);
        Alternativa a1 = new Alternativa(); a1.setId(10L); a1.setOrdem(1); a1.setQuestao(q); a1.setTexto("A"); a1.setCorreta(true);
        q.setAlternativas(new HashSet<>(Collections.singletonList(a1)));
        
        when(questaoRepository.findByIdWithDetails(qId)).thenReturn(Optional.of(q));
        when(alternativaRepository.findByQuestaoIdOrderByOrdemAsc(qId)).thenReturn(Collections.singletonList(a1));
        when(concursoRepository.findById(any())).thenReturn(Optional.of(c));
        when(questaoRepository.save(any())).thenReturn(q);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setConcursoId(1L); req.setEnunciado("Old"); req.setAnulada(false); req.setConcursoCargoIds(Collections.singletonList(100L));
        req.setAlternativas(Arrays.asList(new AlternativaUpdateRequest() {{ setId(10L); setOrdem(2); setTexto("A"); setCorreta(true); }}, new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(1); }}));
        when(concursoCargoRepository.findById(100L)).thenReturn(Optional.of(createConcursoCargo(100L, c)));

        questaoService.update(qId, req);
        assertEquals(10L, a1.getId());
        assertEquals(2, a1.getOrdem());
    }

    @Test
    void testUpdate_ContentChangeDeletesResponses() {
        Long qId = 1L; Questao q = new Questao(); q.setId(qId); q.setEnunciado("Old"); 
        Concurso c = new Concurso(); c.setId(1L); q.setConcurso(c);
        when(questaoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(q));
        when(concursoRepository.findById(any())).thenReturn(Optional.of(c));
        when(questaoRepository.save(any())).thenReturn(q);
        when(concursoCargoRepository.findById(any())).thenReturn(Optional.of(createConcursoCargo(100L, c)));
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setConcursoId(1L);
        req.setEnunciado("New"); req.setAnulada(false); req.setConcursoCargoIds(Collections.singletonList(100L));
        req.setAlternativas(Arrays.asList(new AlternativaUpdateRequest() {{ setTexto("A"); setCorreta(true); setOrdem(1); }}, new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(2); }}));

        questaoService.update(1L, req);
        verify(respostaRepository).deleteByQuestaoId(1L);
    }

    @Test
    void testNormalizeAlternativaOrders() {
        Questao q = new Questao();
        Alternativa a1 = new Alternativa(); a1.setOrdem(10);
        Alternativa a2 = new Alternativa(); a2.setOrdem(5);
        q.setAlternativas(new HashSet<>(Arrays.asList(a1, a2)));
        
        ReflectionTestUtils.invokeMethod(questaoService, "normalizeAlternativaOrders", q);
        List<Alternativa> sorted = q.getAlternativas().stream().sorted(java.util.Comparator.comparing(Alternativa::getOrdem)).toList();
        assertEquals(1, sorted.get(0).getOrdem());
        assertEquals(2, sorted.get(1).getOrdem());
    }

    @Test
    void testValidateBusinessRules_FailsIfNoCargo() {
        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setConcursoId(1L);
        req.setConcursoCargoIds(Collections.emptyList());
        req.setAlternativas(Arrays.asList(new AlternativaCreateRequest() {{ setCorreta(true); }}, new AlternativaCreateRequest() {{ setCorreta(false); }}));
        when(concursoRepository.findById(any())).thenReturn(Optional.of(new Concurso()));
        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    private ConcursoCargo createConcursoCargo(Long id, Concurso c) {
        ConcursoCargo cc = new ConcursoCargo(); cc.setId(id); cc.setConcurso(c);
        return cc;
    }
}
