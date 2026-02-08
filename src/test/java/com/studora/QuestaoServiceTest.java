package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.questao.QuestaoFilter;
import com.studora.dto.request.AlternativaCreateRequest;
import com.studora.dto.request.AlternativaUpdateRequest;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Cargo;
import com.studora.entity.Questao;
import com.studora.entity.QuestaoCargo;
import com.studora.entity.Alternativa;
import com.studora.exception.ValidationException;
import com.studora.repository.*;
import com.studora.service.QuestaoService;
import com.studora.mapper.*;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Collections;
import java.util.ArrayList;
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
    @Mock private RespostaRepository respostaRepository;
    @Mock private AlternativaRepository alternativaRepository;
    @Mock private EntityManager entityManager;

    private QuestaoService questaoService;

    // Mappers
    private QuestaoMapper questaoMapper;
    private AlternativaMapper alternativaMapper;
    private RespostaMapper respostaMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        questaoMapper = org.mapstruct.factory.Mappers.getMapper(QuestaoMapper.class);
        alternativaMapper = org.mapstruct.factory.Mappers.getMapper(AlternativaMapper.class);
        SubtemaMapper subtemaMapper = org.mapstruct.factory.Mappers.getMapper(SubtemaMapper.class);
        respostaMapper = org.mapstruct.factory.Mappers.getMapper(RespostaMapper.class);
        
        ReflectionTestUtils.setField(questaoMapper, "alternativaMapper", alternativaMapper);
        ReflectionTestUtils.setField(questaoMapper, "subtemaMapper", subtemaMapper);
        ReflectionTestUtils.setField(questaoMapper, "respostaMapper", respostaMapper);

        questaoService = new QuestaoService(
            questaoRepository, concursoRepository, subtemaRepository,
            concursoCargoRepository, respostaRepository,
            alternativaRepository,
            questaoMapper, entityManager
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
        req.setCargos(Collections.singletonList(10L)); // Cargo ID

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(concursoCargoRepository.existsByConcursoIdAndCargoId(1L, 10L)).thenReturn(true);
        when(concursoCargoRepository.findByConcursoIdAndCargoId(1L, 10L)).thenReturn(Collections.singletonList(createConcursoCargo(100L, c, 10L)));
        
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
    void testCreate_Validation_CargoMustBelongToConcurso() {
        Concurso c = new Concurso(); c.setId(1L);
        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setConcursoId(1L);
        req.setEnunciado("Fail");
        req.setAlternativas(Arrays.asList(new AlternativaCreateRequest(1, "A", true), new AlternativaCreateRequest(2, "B", false)));
        req.setCargos(Collections.singletonList(99L)); // Invalid cargo

        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(concursoCargoRepository.existsByConcursoIdAndCargoId(1L, 99L)).thenReturn(false);

        assertThrows(ValidationException.class, () -> questaoService.create(req));
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
        req.setCargos(Collections.singletonList(10L));

        when(questaoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(concursoCargoRepository.existsByConcursoIdAndCargoId(1L, 10L)).thenReturn(true);
        when(concursoCargoRepository.findByConcursoIdAndCargoId(1L, 10L)).thenReturn(Collections.singletonList(createConcursoCargo(100L, c, 10L)));
        
        when(questaoRepository.save(any())).thenReturn(existing);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoDetailDto result = questaoService.update(id, req);
        assertNotNull(result);
    }

    @Test
    void testUpdate_ImplicitCargoSync() {
        Long id = 1L;
        Concurso c = new Concurso(); c.setId(1L);
        Questao existing = new Questao(); existing.setId(id); existing.setEnunciado("Old"); existing.setConcurso(c);
        
        // Existing cargo association (Cargo ID 10)
        ConcursoCargo ccOld = createConcursoCargo(100L, c, 10L);
        QuestaoCargo qcOld = new QuestaoCargo(); qcOld.setId(1L); qcOld.setQuestao(existing); qcOld.setConcursoCargo(ccOld);
        existing.addQuestaoCargo(qcOld);

        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setConcursoId(1L);
        req.setEnunciado("Old");
        req.setAnulada(false);
        req.setAlternativas(Arrays.asList(new AlternativaUpdateRequest() {{ setTexto("A"); setCorreta(true); setOrdem(1); }}, new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(2); }}));
        req.setCargos(Collections.singletonList(20L)); // Change to Cargo ID 20

        when(questaoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(concursoCargoRepository.existsByConcursoIdAndCargoId(1L, 20L)).thenReturn(true);
        when(concursoCargoRepository.findByConcursoIdAndCargoId(1L, 20L)).thenReturn(Collections.singletonList(createConcursoCargo(101L, c, 20L)));
        when(questaoRepository.save(any())).thenReturn(existing);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        questaoService.update(id, req);

        // Verify that old association was removed and new one added
        assertEquals(1, existing.getQuestaoCargos().size());
        assertEquals(20L, existing.getQuestaoCargos().iterator().next().getConcursoCargo().getCargo().getId());
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
    void testCreate_RequiresAtLeastTwoAlternatives() {
        QuestaoCreateRequest req = new QuestaoCreateRequest(1L, "E");
        req.setAlternativas(Collections.singletonList(new AlternativaCreateRequest(1, "A", true)));
        req.setCargos(Collections.singletonList(10L));
        Concurso c = new Concurso(); c.setId(1L);
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testCreate_RequiresExactlyOneCorrect() {
        QuestaoCreateRequest req = new QuestaoCreateRequest(1L, "E");
        req.setCargos(Collections.singletonList(10L));
        req.setAlternativas(Arrays.asList(new AlternativaCreateRequest(1, "A", true), new AlternativaCreateRequest(2, "B", true)));
        Concurso c = new Concurso(); c.setId(1L);
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testUpdate_ContentChangeDeletesResponses() {
        Long qId = 1L; Questao q = new Questao(); q.setId(qId); q.setEnunciado("Old"); 
        Concurso c = new Concurso(); c.setId(1L); q.setConcurso(c);
        when(questaoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(q));
        when(concursoRepository.findById(any())).thenReturn(Optional.of(c));
        when(questaoRepository.save(any())).thenReturn(q);
        when(concursoCargoRepository.existsByConcursoIdAndCargoId(1L, 10L)).thenReturn(true);
        when(concursoCargoRepository.findByConcursoIdAndCargoId(1L, 10L)).thenReturn(Collections.singletonList(createConcursoCargo(100L, c, 10L)));
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setConcursoId(1L);
        req.setEnunciado("New"); req.setAnulada(false); req.setCargos(Collections.singletonList(10L));
        req.setAlternativas(Arrays.asList(new AlternativaUpdateRequest() {{ setTexto("A"); setCorreta(true); setOrdem(1); }}, new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(2); }}));

        questaoService.update(1L, req);
        verify(respostaRepository).deleteByQuestaoId(1L);
    }

    @Test
    void testValidateBusinessRules_FailsIfNoCargo() {
        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setConcursoId(1L);
        req.setCargos(Collections.emptyList());
        req.setAlternativas(Arrays.asList(new AlternativaCreateRequest() {{ setCorreta(true); }}, new AlternativaCreateRequest() {{ setCorreta(false); }}));
        when(concursoRepository.findById(any())).thenReturn(Optional.of(new Concurso()));
        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testUpdate_ReorderAlternatives_UniqueConstraint() {
        Long id = 1L;
        Concurso c = new Concurso(); c.setId(1L);
        Questao existing = new Questao(); existing.setId(id); existing.setEnunciado("Old"); existing.setConcurso(c);
        
        // Setup existing alternatives with IDs and Orders
        Alternativa alt1 = new Alternativa(); alt1.setId(10L); alt1.setOrdem(1); alt1.setTexto("A"); alt1.setQuestao(existing);
        Alternativa alt2 = new Alternativa(); alt2.setId(11L); alt2.setOrdem(2); alt2.setTexto("B"); alt2.setQuestao(existing);
        existing.getAlternativas().add(alt1);
        existing.getAlternativas().add(alt2);

        // Mock repository to return these alternatives
        when(alternativaRepository.findByQuestaoIdOrderByOrdemAsc(id)).thenReturn(Arrays.asList(alt1, alt2));

        // Request to SWAP orders: Alt1 -> 2, Alt2 -> 1
        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setConcursoId(1L);
        req.setEnunciado("Old");
        req.setAnulada(false);
        req.setCargos(Collections.singletonList(10L));
        
        AlternativaUpdateRequest update1 = new AlternativaUpdateRequest();
        update1.setId(10L); update1.setOrdem(2); update1.setTexto("A"); update1.setCorreta(true);
        
        AlternativaUpdateRequest update2 = new AlternativaUpdateRequest();
        update2.setId(11L); update2.setOrdem(1); update2.setTexto("B"); update2.setCorreta(false);
        
        req.setAlternativas(Arrays.asList(update1, update2));

        // Mocks for dependencies
        when(questaoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(concursoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(concursoCargoRepository.existsByConcursoIdAndCargoId(1L, 10L)).thenReturn(true);
        when(concursoCargoRepository.findByConcursoIdAndCargoId(1L, 10L)).thenReturn(Collections.singletonList(createConcursoCargo(100L, c, 10L)));
        when(questaoRepository.save(any())).thenReturn(existing);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        // Execute update
        questaoService.update(id, req);

        // Verification: 
        // 1. Ensure the orders were swapped in the existing entity (JPA dirty checking handles the update)
        assertEquals(2, alt1.getOrdem());
        assertEquals(1, alt2.getOrdem());
        
        // 2. Ensure flush was called to clear positive space and finish transaction
        verify(entityManager, atLeastOnce()).flush();
    }

    private ConcursoCargo createConcursoCargo(Long id, Concurso c, Long cargoId) {
        ConcursoCargo cc = new ConcursoCargo(); cc.setId(id); cc.setConcurso(c);
        Cargo cargo = new Cargo(); cargo.setId(cargoId);
        cc.setCargo(cargo);
        return cc;
    }
}