package com.studora;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoFilter;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.request.AlternativaCreateRequest;
import com.studora.dto.request.AlternativaUpdateRequest;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.entity.Alternativa;
import com.studora.entity.Prova;
import com.studora.entity.ProvaSecao;
import com.studora.entity.Questao;
import com.studora.entity.QuestaoProvaSecao;
import com.studora.exception.ValidationException;
import com.studora.mapper.AlternativaMapper;
import com.studora.mapper.CargoMapper;
import com.studora.mapper.ConcursoMapper;
import com.studora.mapper.QuestaoMapper;
import com.studora.mapper.RespostaMapper;
import com.studora.mapper.SubtemaMapper;
import com.studora.repository.AlternativaRepository;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.ConcursoRepository;
import com.studora.repository.ProvaSecaoRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.repository.RespostaRepository;
import com.studora.repository.SubtemaRepository;
import com.studora.service.QuestaoService;

import jakarta.persistence.EntityManager;

class QuestaoServiceTest {

    @Mock
    private QuestaoRepository questaoRepository;
    @Mock
    private ConcursoRepository concursoRepository;
    @Mock
    private SubtemaRepository subtemaRepository;
    @Mock
    private ConcursoCargoRepository concursoCargoRepository;
    @Mock
    private RespostaRepository respostaRepository;
    @Mock
    private AlternativaRepository alternativaRepository;
    @Mock
    private ProvaSecaoRepository provaSecaoRepository;
    @Mock
    private EntityManager entityManager;

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
        CargoMapper cargoMapper = org.mapstruct.factory.Mappers.getMapper(CargoMapper.class);
        ConcursoMapper concursoMapper = org.mapstruct.factory.Mappers.getMapper(ConcursoMapper.class);

        ReflectionTestUtils.setField(questaoMapper, "alternativaMapper", alternativaMapper);
        ReflectionTestUtils.setField(questaoMapper, "respostaMapper", respostaMapper);
        ReflectionTestUtils.setField(questaoMapper, "subtemaMapper", subtemaMapper);

        questaoService = new QuestaoService(
                questaoRepository, concursoRepository, subtemaRepository,
                concursoCargoRepository, respostaRepository,
                alternativaRepository,
                questaoMapper, entityManager, provaSecaoRepository);
    }

    @Test
    void testGetRandomQuestao_ExcludesRecentlyAnswered() {
        com.studora.dto.questao.QuestaoRandomFilter filter = new com.studora.dto.questao.QuestaoRandomFilter();
        when(questaoRepository.count(any(Specification.class))).thenReturn(0L);

        assertThrows(com.studora.exception.ResourceNotFoundException.class,
                () -> questaoService.getRandomQuestao(filter));

        verify(questaoRepository).count(any(Specification.class));
    }

    @Test
    void testFindAll() {
        Questao q1 = new Questao();
        q1.setId(1L);
        Page<Questao> page = new PageImpl<>(Collections.singletonList(q1));
        when(questaoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        Page<QuestaoSummaryDto> result = questaoService.findAll(new QuestaoFilter(), Pageable.unpaged());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindById() {
        Questao q = new Questao();
        q.setId(1L);
        q.setEnunciado("Test?");
        when(questaoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(q));
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoDetailDto result = questaoService.getQuestaoDetailById(1L);
        assertNotNull(result);
        assertEquals("Test?", result.getEnunciado());
    }

    @Test
    void testCreate_Success() {
        Prova prova = new Prova();
        prova.setId(10L);
        ProvaSecao ps = new ProvaSecao();
        ps.setId(100L);
        ps.setProva(prova);
        
        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setEnunciado("New?");
        req.setAlternativas(Arrays.asList(
                new AlternativaCreateRequest(1, "A", true),
                new AlternativaCreateRequest(2, "B", false)));
        req.setSecoesIds(Collections.singletonList(100L));
        req.setSubtemaIds(Collections.singletonList(1L));

        when(provaSecaoRepository.findById(100L)).thenReturn(Optional.of(ps));
        when(subtemaRepository.findAllById(anyList()))
                .thenReturn(Collections.singletonList(new com.studora.entity.Subtema()));

        when(questaoRepository.save(any(Questao.class))).thenAnswer(i -> {
            Questao q = i.getArgument(0);
            q.setId(1L);
            return q;
        });

        Questao savedQ = new Questao();
        savedQ.setId(1L);
        savedQ.setEnunciado("New?");
        when(questaoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(savedQ));
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        Long result = questaoService.create(req);
        assertEquals(1L, result);
        verify(entityManager).flush();
    }

    @Test
    void testCreate_Validation_SecaoRequired() {
        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setEnunciado("Fail");
        req.setAutoral(false); // Questão de concurso precisa de secao
        req.setAlternativas(
                Arrays.asList(new AlternativaCreateRequest(1, "A", true), new AlternativaCreateRequest(2, "B", false)));

        req.setSubtemaIds(Collections.singletonList(1L));
        req.setSecoesIds(Collections.emptyList()); // Secoes vazias deve falhar

        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testCreate_Validation_OneSecaoPerProvaPerQuestao() {
        Prova prova = new Prova();
        prova.setId(10L);
        
        ProvaSecao ps1 = new ProvaSecao();
        ps1.setId(100L);
        ps1.setNome("Secao 1");
        ps1.setProva(prova);
        
        ProvaSecao ps2 = new ProvaSecao();
        ps2.setId(200L);
        ps2.setNome("Secao 2");
        ps2.setProva(prova);

        when(provaSecaoRepository.findById(100L)).thenReturn(Optional.of(ps1));
        when(provaSecaoRepository.findById(200L)).thenReturn(Optional.of(ps2));

        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setEnunciado("Fail");
        req.setAutoral(false);
        req.setAlternativas(
                Arrays.asList(new AlternativaCreateRequest(1, "A", true), new AlternativaCreateRequest(2, "B", false)));
        req.setSubtemaIds(Collections.singletonList(1L));
        // Assign to TWO sections of the SAME prova (10L)
        req.setSecoesIds(Arrays.asList(100L, 200L));

        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testUpdate_Success() {
        Long id = 1L;
        Questao existing = new Questao();
        existing.setId(id);
        existing.setEnunciado("Old");
        
        Prova prova = new Prova();
        prova.setId(10L);

        ProvaSecao ps = new ProvaSecao();
        ps.setId(100L);
        ps.setProva(prova);

        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setEnunciado("Old");
        req.setAnulada(false);
        req.setAlternativas(Arrays.asList(
                new AlternativaUpdateRequest() {{ setTexto("A"); setCorreta(true); setOrdem(1); }},
                new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(2); }}
        ));
        req.setSecoesIds(Collections.singletonList(100L));
        req.setSubtemaIds(Collections.singletonList(1L));

        when(questaoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(provaSecaoRepository.findById(100L)).thenReturn(Optional.of(ps));
        when(subtemaRepository.findAllById(anyList()))
                .thenReturn(Collections.singletonList(new com.studora.entity.Subtema()));

        when(questaoRepository.save(any())).thenReturn(existing);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoDetailDto result = questaoService.update(id, req);
        assertNotNull(result);
    }

    @Test
    void testUpdate_ImplicitSecaoSync() {
        Long id = 1L;
        Questao existing = new Questao();
        existing.setId(id);
        existing.setEnunciado("Old");

        Prova prova = new Prova();
        prova.setId(10L);

        ProvaSecao psNew = new ProvaSecao();
        psNew.setId(200L);
        psNew.setProva(prova);

        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setEnunciado("Old");
        req.setAnulada(false);
        req.setAlternativas(Arrays.asList(
            new AlternativaUpdateRequest() {{ setTexto("A"); setCorreta(true); setOrdem(1); }},
            new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(2); }}
        ));
        req.setSecoesIds(Collections.singletonList(200L));
        req.setSubtemaIds(Collections.singletonList(1L));

        when(questaoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(provaSecaoRepository.findById(200L)).thenReturn(Optional.of(psNew));
        when(subtemaRepository.findAllById(anyList()))
                .thenReturn(Collections.singletonList(new com.studora.entity.Subtema()));
        when(questaoRepository.save(any())).thenReturn(existing);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        questaoService.update(id, req);

        // Verify that secao sync was called (logic inside service)
        verify(provaSecaoRepository, times(2)).findById(200L);
        assertEquals(1, existing.getSecoes().size());
    }

    @Test
    void testDelete() {
        when(questaoRepository.existsById(1L)).thenReturn(true);
        questaoService.delete(1L);
        verify(questaoRepository).deleteById(1L);
    }

    @Test
    void testToggleDesatualizada() {
        Questao q = new Questao();
        q.setId(1L);
        q.setDesatualizada(false);
        when(questaoRepository.findById(1L)).thenReturn(Optional.of(q));
        questaoService.toggleDesatualizada(1L);
        assertTrue(q.getDesatualizada());
        verify(questaoRepository).save(q);
    }

    @Test
    void testCreate_RequiresAtLeastTwoAlternatives() {
        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setEnunciado("E");
        req.setAlternativas(Collections.singletonList(new AlternativaCreateRequest(1, "A", true)));
        req.setSecoesIds(Collections.singletonList(100L));
        req.setSubtemaIds(Collections.singletonList(1L));
        
        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testCreate_RequiresExactlyOneCorrect() {
        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setEnunciado("E");
        req.setSecoesIds(Collections.singletonList(100L));
        req.setSubtemaIds(Collections.singletonList(1L));
        req.setAlternativas(
                Arrays.asList(new AlternativaCreateRequest(1, "A", true), new AlternativaCreateRequest(2, "B", true)));
                
        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testUpdate_ContentChangeDeletesResponses() {
        Long qId = 1L;
        Questao q = new Questao();
        q.setId(qId);
        q.setEnunciado("Old");
        
        Prova prova = new Prova();
        prova.setId(10L);

        ProvaSecao ps = new ProvaSecao();
        ps.setId(100L);
        ps.setProva(prova);

        QuestaoProvaSecao qps = new QuestaoProvaSecao();
        qps.setProvaSecao(ps);
        q.addSecao(qps);
        
        when(questaoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(q));
        when(provaSecaoRepository.findById(any())).thenReturn(Optional.of(ps));
        when(questaoRepository.save(any())).thenReturn(q);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setEnunciado("New"); // Changed content
        req.setAnulada(false);
        req.setSecoesIds(Collections.singletonList(100L));
        req.setSubtemaIds(Collections.singletonList(1L));
        req.setAlternativas(Arrays.asList(
            new AlternativaUpdateRequest() {{ setTexto("A"); setCorreta(true); setOrdem(1); }},
            new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(2); }}
        ));

        questaoService.update(1L, req);
        verify(respostaRepository).deleteByQuestaoId(1L);
    }

    @Test
    void testUpdate_ReorderAlternatives_UniqueConstraint() {
        Long id = 1L;
        Questao existing = new Questao();
        existing.setId(id);
        existing.setEnunciado("Old");
        
        Prova prova = new Prova();
        prova.setId(10L);

        ProvaSecao ps = new ProvaSecao();
        ps.setId(100L);
        ps.setProva(prova);

        QuestaoProvaSecao qps = new QuestaoProvaSecao();
        qps.setProvaSecao(ps);
        existing.addSecao(qps);

        // Setup existing alternatives with IDs and Orders
        Alternativa alt1 = new Alternativa();
        alt1.setId(10L);
        alt1.setOrdem(1);
        alt1.setTexto("A");
        alt1.setQuestao(existing);
        Alternativa alt2 = new Alternativa();
        alt2.setId(11L);
        alt2.setOrdem(2);
        alt2.setTexto("B");
        alt2.setQuestao(existing);
        existing.getAlternativas().add(alt1);
        existing.getAlternativas().add(alt2);

        when(alternativaRepository.findByQuestaoIdOrderByOrdemAsc(id)).thenReturn(Arrays.asList(alt1, alt2));

        // Request to SWAP orders: Alt1 -> 2, Alt2 -> 1
        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setEnunciado("Old");
        req.setAnulada(false);
        req.setSecoesIds(Collections.singletonList(100L));
        req.setSubtemaIds(Collections.singletonList(1L));

        AlternativaUpdateRequest update1 = new AlternativaUpdateRequest();
        update1.setId(10L);
        update1.setOrdem(2);
        update1.setTexto("A");
        update1.setCorreta(true);

        AlternativaUpdateRequest update2 = new AlternativaUpdateRequest();
        update2.setId(11L);
        update2.setOrdem(1);
        update2.setTexto("B");
        update2.setCorreta(false);

        req.setAlternativas(Arrays.asList(update1, update2));

        when(questaoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(provaSecaoRepository.findById(100L)).thenReturn(Optional.of(ps));
        when(questaoRepository.save(any())).thenReturn(existing);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        questaoService.update(id, req);

        // Verification
        assertEquals(2, alt1.getOrdem());
        assertEquals(1, alt2.getOrdem());
        verify(entityManager, atLeastOnce()).flush();
    }
}