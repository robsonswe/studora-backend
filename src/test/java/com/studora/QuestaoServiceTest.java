package com.studora;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.studora.dto.request.SecaoQuestaoRequest;
import com.studora.entity.Alternativa;
import com.studora.entity.Cargo;
import com.studora.entity.Concurso;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Prova;
import com.studora.entity.ProvaSecao;
import com.studora.entity.Questao;
import com.studora.entity.QuestaoProvaSecao;
import com.studora.entity.SecaoCargo;
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
import com.studora.repository.SecaoDisciplinaRepository;
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
    private SecaoDisciplinaRepository secaoDisciplinaRepository;
    @Mock
    private EntityManager entityManager;

    private QuestaoService questaoService;

    // Mappers
    private QuestaoMapper questaoMapper;
    private AlternativaMapper alternativaMapper;
    private RespostaMapper respostaMapper;

    private com.studora.entity.Subtema defaultSubtema;

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
                questaoMapper, entityManager, provaSecaoRepository,
                secaoDisciplinaRepository);

        // Default mock for subtema principal requirement
        defaultSubtema = new com.studora.entity.Subtema();
        defaultSubtema.setId(1L);
        defaultSubtema.setNome("Default Subtema");
        when(subtemaRepository.findById(1L)).thenReturn(Optional.of(defaultSubtema));
    }

    private void setupEditalForSubtema(ProvaSecao ps, com.studora.entity.Subtema subtema) {
        SecaoCargo sc = ps.getSecaoCargo();
        if (sc == null) {
            sc = new SecaoCargo();
            ps.setSecaoCargo(sc);
        }
        if (sc.getDisciplinas() == null) {
            sc.setDisciplinas(new java.util.LinkedHashSet<>());
        }
        com.studora.entity.SecaoDisciplina sd = new com.studora.entity.SecaoDisciplina();
        sd.setSubtemas(new java.util.HashSet<>(Collections.singletonList(subtema)));
        sc.getDisciplinas().add(sd);
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
        Cargo cargo = new Cargo();
        cargo.setId(10L);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);

        Prova prova = new Prova();
        prova.setId(10L);
        prova.setConcursoCargo(cc);

        SecaoCargo scDef = new SecaoCargo();
        scDef.setId(200L);
        scDef.setConcursoCargo(cc);
        
        com.studora.entity.SecaoDisciplina sd = new com.studora.entity.SecaoDisciplina();
        sd.setSubtemas(new java.util.HashSet<>(Collections.singletonList(defaultSubtema)));
        scDef.setDisciplinas(new java.util.LinkedHashSet<>(Collections.singletonList(sd)));

        ProvaSecao ps = new ProvaSecao();
        ps.setId(100L);
        ps.setProva(prova);
        ps.setSecaoCargo(scDef);

        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setPrincipalSubtemaId(1L); req.setSubtemaIds(java.util.Collections.singletonList(1L));
        req.setEnunciado("New?");
        req.setAlternativas(Arrays.asList(
                new AlternativaCreateRequest(1, "A", true),
                new AlternativaCreateRequest(2, "B", false)));
        req.setSecoes(Collections.singletonList(new SecaoQuestaoRequest(100L, 1)));
        req.setSubtemaIds(Collections.singletonList(1L));

        when(provaSecaoRepository.findById(100L)).thenReturn(Optional.of(ps));
        when(subtemaRepository.findAllById(anyList()))
                .thenReturn(Collections.singletonList(defaultSubtema));

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
        req.setPrincipalSubtemaId(1L); req.setSubtemaIds(java.util.Collections.singletonList(1L));
        req.setEnunciado("Fail");
        req.setAutoral(false); // Questão de concurso precisa de secao
        req.setAlternativas(
                Arrays.asList(new AlternativaCreateRequest(1, "A", true), new AlternativaCreateRequest(2, "B", false)));

        req.setSubtemaIds(Collections.singletonList(1L));
        req.setSecoes(Collections.emptyList()); // Secoes vazias deve falhar

        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

@Test
    void testCreate_Validation_OneSecaoPerProvaPerQuestao() {
        Cargo cargo = new Cargo();
        cargo.setId(10L);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);

        SecaoCargo scDef1 = new SecaoCargo();
        scDef1.setId(200L);
        scDef1.setConcursoCargo(cc);
        scDef1.setNome("Secao 1");

        SecaoCargo scDef2 = new SecaoCargo();
        scDef2.setId(300L);
        scDef2.setConcursoCargo(cc);
        scDef2.setNome("Secao 2");

        Prova prova = new Prova();
        prova.setId(10L);
        prova.setConcursoCargo(cc);

        ProvaSecao ps1 = new ProvaSecao();
        ps1.setId(100L);
        ps1.setNome("Secao 1");
        ps1.setProva(prova);
        ps1.setSecaoCargo(scDef1);

        ProvaSecao ps2 = new ProvaSecao();
        ps2.setId(200L);
        ps2.setNome("Secao 2");
        ps2.setProva(prova);
        ps2.setSecaoCargo(scDef2);

        when(provaSecaoRepository.findById(100L)).thenReturn(Optional.of(ps1));
        when(provaSecaoRepository.findById(200L)).thenReturn(Optional.of(ps2));

        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setPrincipalSubtemaId(1L); req.setSubtemaIds(java.util.Collections.singletonList(1L));
        req.setEnunciado("Fail");
        req.setAutoral(false);
        req.setAlternativas(
                Arrays.asList(new AlternativaCreateRequest(1, "A", true), new AlternativaCreateRequest(2, "B", false)));
        req.setSubtemaIds(Collections.singletonList(1L));
        // Assign to TWO sections with DIFFERENT secaoCargo (same prova/cargo)
        req.setSecoes(Arrays.asList(new SecaoQuestaoRequest(100L, 1), new SecaoQuestaoRequest(200L, 2)));

        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testUpdate_Success() {
        Long id = 1L;
        Questao existing = new Questao();
        existing.setId(id);
        existing.setEnunciado("Old");

        Cargo cargo = new Cargo();
        cargo.setId(10L);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);

        SecaoCargo scDef = new SecaoCargo();
        scDef.setId(200L);
        scDef.setConcursoCargo(cc);
        scDef.setNome("Secao Def");

        Prova prova = new Prova();
        prova.setId(10L);
        prova.setConcursoCargo(cc);

        ProvaSecao ps = new ProvaSecao();
        ps.setId(100L);
        ps.setProva(prova);
        ps.setSecaoCargo(scDef);
        ps.setNome("Prova Secao");

        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setPrincipalSubtemaId(1L); req.setSubtemaIds(java.util.Collections.singletonList(1L));
        req.setEnunciado("Old");
        req.setAnulada(false);
        req.setAlternativas(Arrays.asList(
                new AlternativaUpdateRequest() {{ setTexto("A"); setCorreta(true); setOrdem(1); }},
                new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(2); }}
        ));
        req.setSecoes(Collections.singletonList(new SecaoQuestaoRequest(100L, 1)));
        req.setSubtemaIds(Collections.singletonList(1L));

        when(questaoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(provaSecaoRepository.findById(100L)).thenReturn(Optional.of(ps));
        setupEditalForSubtema(ps, defaultSubtema);
        when(subtemaRepository.findAllById(anyList()))
                .thenReturn(Collections.singletonList(defaultSubtema));

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

        Cargo cargo = new Cargo();
        cargo.setId(10L);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);

        SecaoCargo scDef = new SecaoCargo();
        scDef.setId(200L);
        scDef.setConcursoCargo(cc);
        scDef.setNome("Secao Def");

        Prova prova = new Prova();
        prova.setId(10L);
        prova.setConcursoCargo(cc);

        ProvaSecao psNew = new ProvaSecao();
        psNew.setId(200L);
        psNew.setProva(prova);
        psNew.setSecaoCargo(scDef);
        psNew.setNome("Prova Secao New");

        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setPrincipalSubtemaId(1L); req.setSubtemaIds(java.util.Collections.singletonList(1L));
        req.setEnunciado("Old");
        req.setAnulada(false);
        req.setAlternativas(Arrays.asList(
            new AlternativaUpdateRequest() {{ setTexto("A"); setCorreta(true); setOrdem(1); }},
            new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(2); }}
        ));
        req.setSecoes(Collections.singletonList(new SecaoQuestaoRequest(200L, 1)));
        req.setSubtemaIds(Collections.singletonList(1L));

        when(questaoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(provaSecaoRepository.findById(200L)).thenReturn(Optional.of(psNew));
        setupEditalForSubtema(psNew, defaultSubtema);
        when(subtemaRepository.findAllById(anyList()))
                .thenReturn(Collections.singletonList(defaultSubtema));
        when(questaoRepository.save(any())).thenReturn(existing);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        questaoService.update(id, req);

        verify(provaSecaoRepository, times(3)).findById(200L);
        assertEquals(1, existing.getSecoes().size());
    }

    @Test
    void testDelete() {
        Questao q = new Questao();
        q.setId(1L);
        when(questaoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(q));
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
        req.setPrincipalSubtemaId(1L); req.setSubtemaIds(java.util.Collections.singletonList(1L));
        req.setEnunciado("E");
        req.setAlternativas(Collections.singletonList(new AlternativaCreateRequest(1, "A", true)));
        req.setSecoes(Collections.singletonList(new SecaoQuestaoRequest(100L, 1)));
        req.setSubtemaIds(Collections.singletonList(1L));
        
        assertThrows(ValidationException.class, () -> questaoService.create(req));
    }

    @Test
    void testCreate_RequiresExactlyOneCorrect() {
        QuestaoCreateRequest req = new QuestaoCreateRequest();
        req.setPrincipalSubtemaId(1L); req.setSubtemaIds(java.util.Collections.singletonList(1L));
        req.setEnunciado("E");
        req.setSecoes(Collections.singletonList(new SecaoQuestaoRequest(100L, 1)));
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

        Cargo cargo = new Cargo();
        cargo.setId(10L);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);

        SecaoCargo scDef = new SecaoCargo();
        scDef.setId(200L);
        scDef.setConcursoCargo(cc);

        Prova prova = new Prova();
        prova.setId(10L);
        prova.setConcursoCargo(cc);

        ProvaSecao ps = new ProvaSecao();
        ps.setId(100L);
        ps.setProva(prova);
        ps.setSecaoCargo(scDef);

        QuestaoProvaSecao qps = new QuestaoProvaSecao();
        qps.setProvaSecao(ps);
        q.addSecao(qps);

        when(questaoRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(q));
        when(provaSecaoRepository.findById(any())).thenReturn(Optional.of(ps));
        setupEditalForSubtema(ps, defaultSubtema);
        when(questaoRepository.save(any())).thenReturn(q);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setPrincipalSubtemaId(1L); req.setSubtemaIds(java.util.Collections.singletonList(1L));
        req.setEnunciado("New"); // Changed content
        req.setAnulada(false);
        req.setSecoes(Collections.singletonList(new SecaoQuestaoRequest(100L, 1)));
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

        Cargo cargo = new Cargo();
        cargo.setId(10L);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);

        SecaoCargo scDef = new SecaoCargo();
        scDef.setId(200L);
        scDef.setConcursoCargo(cc);

        Prova prova = new Prova();
        prova.setId(10L);
        prova.setConcursoCargo(cc);

        ProvaSecao ps = new ProvaSecao();
        ps.setId(100L);
        ps.setProva(prova);
        ps.setSecaoCargo(scDef);

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
        req.setPrincipalSubtemaId(1L); req.setSubtemaIds(java.util.Collections.singletonList(1L));
        req.setEnunciado("Old");
        req.setAnulada(false);
        req.setSecoes(Collections.singletonList(new SecaoQuestaoRequest(100L, 1)));
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
        setupEditalForSubtema(ps, defaultSubtema);
        when(questaoRepository.save(any())).thenReturn(existing);
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        questaoService.update(id, req);

        // Verification
        assertEquals(2, alt1.getOrdem());
        assertEquals(1, alt2.getOrdem());
        verify(entityManager, atLeastOnce()).flush();
    }

    @Test
    void testUpdate_ReorderQuestoes_GlobalOrdering() {
        Long id = 1L;
        Questao existing = new Questao();
        existing.setId(id);
        existing.setEnunciado("Old");

        Cargo cargo = new Cargo();
        cargo.setId(10L);

        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(100L);
        cc.setCargo(cargo);

        SecaoCargo scDef = new SecaoCargo();
        scDef.setId(200L);
        scDef.setConcursoCargo(cc);

        Prova prova = new Prova();
        prova.setId(10L);
        prova.setConcursoCargo(cc);

        ProvaSecao ps1 = new ProvaSecao();
        ps1.setId(101L);
        ps1.setProva(prova);
        ps1.setSecaoCargo(scDef);
        ps1.setOrdem(1);

        ProvaSecao ps2 = new ProvaSecao();
        ps2.setId(102L);
        ps2.setProva(prova);
        ps2.setSecaoCargo(scDef);
        ps2.setOrdem(2);
        
        prova.addSecao(ps1);
        prova.addSecao(ps2);

        QuestaoProvaSecao qps1 = new QuestaoProvaSecao();
        qps1.setId(1L);
        qps1.setProvaSecao(ps1);
        qps1.setNumeroQuestao(1);
        qps1.setQuestao(existing);
        existing.addSecao(qps1);
        ps1.getQuestoes().add(qps1);

        QuestaoProvaSecao qps2 = new QuestaoProvaSecao();
        qps2.setId(2L);
        qps2.setProvaSecao(ps2);
        qps2.setNumeroQuestao(2);
        qps2.setQuestao(existing);
        existing.addSecao(qps2);
        ps2.getQuestoes().add(qps2);

        when(questaoRepository.findByIdWithDetails(id)).thenReturn(Optional.of(existing));
        when(provaSecaoRepository.findById(101L)).thenReturn(Optional.of(ps1));
        when(provaSecaoRepository.findById(102L)).thenReturn(Optional.of(ps2));
        setupEditalForSubtema(ps1, defaultSubtema);
        setupEditalForSubtema(ps2, defaultSubtema);
        when(provaSecaoRepository.findByProvaIdWithQuestoes(10L)).thenReturn(Arrays.asList(ps1, ps2));
        when(subtemaRepository.findAllById(anyList())).thenReturn(Collections.singletonList(defaultSubtema));
        when(questaoRepository.save(any())).thenReturn(existing);
        when(alternativaRepository.findByQuestaoIdOrderByOrdemAsc(id)).thenReturn(Collections.emptyList());
        when(respostaRepository.findByQuestaoIdInWithDetails(anyList())).thenReturn(Collections.emptyList());

        // Update: Swap order or just re-request
        SecaoQuestaoRequest req1 = new SecaoQuestaoRequest(101L, 2);
        SecaoQuestaoRequest req2 = new SecaoQuestaoRequest(102L, 1);
        
        QuestaoUpdateRequest req = new QuestaoUpdateRequest();
        req.setPrincipalSubtemaId(1L); req.setSubtemaIds(java.util.Collections.singletonList(1L));
        req.setEnunciado("Old");
        req.setAnulada(false);
        req.setSecoes(Arrays.asList(req1, req2));
        req.setSubtemaIds(Collections.singletonList(1L));
        req.setAlternativas(Arrays.asList(
            new AlternativaUpdateRequest() {{ setTexto("A"); setCorreta(true); setOrdem(1); }},
            new AlternativaUpdateRequest() {{ setTexto("B"); setCorreta(false); setOrdem(2); }}
        ));

        questaoService.update(id, req);

        // Verification: ps1 (ordem 1) should now have numero 1, ps2 (ordem 2) should have numero 2
        assertEquals(1, qps1.getNumeroQuestao(), "PS1 should be first");
        assertEquals(2, qps2.getNumeroQuestao(), "PS2 should be second");
    }

    @Test
    void testSynchronizeSubtemas_BlocksRemovalIfNoReplacement() {
        Questao questao = new Questao();
        com.studora.entity.QuestaoSubtema qs = new com.studora.entity.QuestaoSubtema();
        com.studora.entity.Subtema s = new com.studora.entity.Subtema();
        s.setId(1L);
        qs.setSubtema(s);
        qs.setPrincipal(true);
        questao.getQuestaoSubtemas().add(qs);

        // Attempting to remove Subtema 1 (the only one)
        assertThrows(ValidationException.class, () -> {
            // Using reflection to call private method
            java.lang.reflect.Method method = QuestaoService.class.getDeclaredMethod("synchronizeSubtemas", Questao.class, List.class, Long.class);
            method.setAccessible(true);
            try {
                method.invoke(questaoService, questao, Collections.emptyList(), 1L);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw (RuntimeException) e.getCause();
            }
        });
    }

    @Test
    void verifySecaoDisciplinaMapping() {
        // Setup a questao with secao link and principal subtema
        Questao questao = new Questao();
        
        Cargo cargo = new Cargo();
        cargo.setId(1L);
        cargo.setNome("Cargo Test");
        
        Concurso concurso = new Concurso();
        concurso.setId(1L);
        
        ConcursoCargo cc = new ConcursoCargo();
        cc.setId(99L);
        cc.setCargo(cargo);
        cc.setConcurso(concurso);
        
        Prova prova = new Prova();
        prova.setConcursoCargo(cc);
        prova.setConcurso(concurso);
        
        ProvaSecao ps = new ProvaSecao();
        ps.setId(100L);
        ps.setProva(prova);
        
        // Define SecaoCargo and Disciplina association
        SecaoCargo sc = new SecaoCargo();
        sc.setId(77L);
        sc.setConcursoCargo(cc);
        ps.setSecaoCargo(sc);
        
        com.studora.entity.Subtema subtema = new com.studora.entity.Subtema();
        subtema.setId(42L);
        
        com.studora.entity.SecaoDisciplina sd = new com.studora.entity.SecaoDisciplina();
        sd.setId(50L);
        sd.setNome("Disciplina Test");
        sd.setSubtemas(new java.util.HashSet<>(Collections.singletonList(subtema)));
        sc.setDisciplinas(new java.util.LinkedHashSet<>(Collections.singletonList(sd)));

        QuestaoProvaSecao qps = new QuestaoProvaSecao();
        qps.setProvaSecao(ps);
        qps.setQuestao(questao);
        questao.addSecao(qps);
        
        questao.addSubtema(subtema, true);

        // Act
        com.studora.dto.questao.ConcursoQuestaoDto dto = questaoMapper.mapConcursoFromSecoes(questao.getSecoes());
        
        // Assert
        assertNotNull(dto);
        assertEquals(1, dto.getCargos().size());
        assertEquals(1, dto.getCargos().get(0).getSecoes().size());
        assertEquals(50L, dto.getCargos().get(0).getSecoes().get(0).getDisciplinaEditalId());
        assertEquals("Disciplina Test", dto.getCargos().get(0).getSecoes().get(0).getDisciplinaEditalNome());
    }
}
