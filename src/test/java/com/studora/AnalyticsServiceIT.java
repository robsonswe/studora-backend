package com.studora;

import static org.junit.jupiter.api.Assertions.*;

import com.studora.dto.analytics.*;
import com.studora.entity.*;
import com.studora.repository.*;
import com.studora.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AnalyticsServiceIT {

    @Autowired private AnalyticsService analyticsService;
    @Autowired private RespostaRepository respostaRepository;
    @Autowired private QuestaoRepository questaoRepository;
    @Autowired private AlternativaRepository alternativaRepository;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private TemaRepository temaRepository;
    @Autowired private SubtemaRepository subtemaRepository;
    @Autowired private ConcursoRepository concursoRepository;
    @Autowired private InstituicaoRepository instituicaoRepository;
    @Autowired private BancaRepository bancaRepository;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @Autowired private jakarta.persistence.EntityManager entityManager;

    private Questao q1;
    private Alternativa q1Correct;
    private Alternativa q1Wrong;
    private Subtema s1;

    @BeforeEach
    void setUp() {
        Instituicao inst = new Instituicao();
        inst.setNome("Inst Test");
        inst.setArea("TI");
        inst = instituicaoRepository.save(inst);

        Banca banca = new Banca();
        banca.setNome("Banca Test");
        banca = bancaRepository.save(banca);

        Concurso concurso = concursoRepository.save(new Concurso(inst, banca, 2023, 1));

        Disciplina d1 = disciplinaRepository.save(new Disciplina("Direito"));
        Tema t1 = temaRepository.save(new Tema(d1, "Constitucional"));
        s1 = subtemaRepository.save(new Subtema(t1, "Direitos Fundamentais"));

        q1 = new Questao(concurso, "Questao 1");
        q1.setSubtemas(new java.util.HashSet<>(java.util.List.of(s1)));
        q1 = questaoRepository.save(q1);

        q1Correct = new Alternativa();
        q1Correct.setQuestao(q1);
        q1Correct.setTexto("Correct");
        q1Correct.setCorreta(true);
        q1Correct.setOrdem(1);
        q1Correct = alternativaRepository.save(q1Correct);

        q1Wrong = new Alternativa();
        q1Wrong.setQuestao(q1);
        q1Wrong.setTexto("Wrong");
        q1Wrong.setCorreta(false);
        q1Wrong.setOrdem(2);
        q1Wrong = alternativaRepository.save(q1Wrong);
        
        q1.getAlternativas().addAll(List.of(q1Correct, q1Wrong));
        questaoRepository.save(q1);
    }

    @Test
    void testConsistencyCalculation() {
        LocalDate today = LocalDate.now();
        // Using a fixed reference point for "days" ago to ensure predictable results
        int analysisDays = 7;
        
        LocalDateTime todayTime = today.atTime(10, 0);
        LocalDateTime yesterdayTime = today.minusDays(1).atTime(10, 0);
        LocalDateTime dayBeforeYesterdayTime = today.minusDays(2).atTime(10, 0);

        // Streak will be: 
        // Day -2: 1 response (streak 1)
        // Day -1: 1 response (streak 2)
        // Day 0: 1 response (streak 3)
        
        createResponse(q1, q1Correct, dayBeforeYesterdayTime, 30, Dificuldade.FACIL);
        createResponse(q1, q1Wrong, yesterdayTime, 45, Dificuldade.MEDIA);
        createResponse(q1, q1Correct, todayTime, 30, Dificuldade.FACIL);
        
        List<ConsistencyDto> result = analyticsService.getConsistencia(analysisDays);
        
        // result should have entries from startDate to today
        assertFalse(result.isEmpty());
        
        ConsistencyDto todayDto = result.stream().filter(d -> d.getDate().equals(today)).findFirst().orElseThrow();
        assertEquals(1, todayDto.getTotalAnswered());
        assertEquals(3, todayDto.getActiveStreak());

        ConsistencyDto yesterdayDto = result.stream().filter(d -> d.getDate().equals(today.minusDays(1))).findFirst().orElseThrow();
        assertEquals(1, yesterdayDto.getTotalAnswered());
        assertEquals(2, yesterdayDto.getActiveStreak());

        ConsistencyDto dayBeforeDto = result.stream().filter(d -> d.getDate().equals(today.minusDays(2))).findFirst().orElseThrow();
        assertEquals(1, dayBeforeDto.getTotalAnswered());
        assertEquals(1, dayBeforeDto.getActiveStreak());
    }

    @Test
    void testTopicMasteryHierarchical() {
        createResponse(q1, q1Correct, LocalDateTime.now(), 10, Dificuldade.FACIL);
        
        // Create another discipline without responses
        Disciplina d2 = disciplinaRepository.save(new Disciplina("Empty Disc"));
        
        // List disciplines
        List<TopicMasteryDto> list = analyticsService.getDisciplinasMastery();
        assertEquals(1, list.size()); // "Direito" should be here, "Empty Disc" should NOT
        assertEquals("Direito", list.get(0).getNome());

        // Detail of "Direito"
        TopicMasteryDto detail = analyticsService.getDisciplinaMasteryDetail(list.get(0).getId());
        assertEquals("Direito", detail.getNome());
        assertFalse(detail.getChildren().isEmpty());
        
        TopicMasteryDto temaDetail = detail.getChildren().get(0);
        assertEquals("Constitucional", temaDetail.getNome());
        assertFalse(temaDetail.getChildren().isEmpty());
        
        TopicMasteryDto subtemaDetail = temaDetail.getChildren().get(0);
        assertEquals("Direitos Fundamentais", subtemaDetail.getNome());
    }

    @Test
    void testLearningRate() {
        // 1st attempt: Wrong
        createResponse(q1, q1Wrong, LocalDateTime.now().minusDays(2), 10, Dificuldade.MEDIA);
        // 2nd attempt: Correct (Recovery)
        createResponse(q1, q1Correct, LocalDateTime.now().minusDays(1), 10, Dificuldade.MEDIA);
        // 3rd attempt: Correct (Retention)
        createResponse(q1, q1Correct, LocalDateTime.now(), 10, Dificuldade.MEDIA);

        LearningRateDto result = analyticsService.getTaxaAprendizado();
        assertEquals(1, result.getTotalRepeatedQuestions());
        assertEquals(1.0, result.getRecoveryRate()); // 1 Wrong -> Correct
        assertEquals(1.0, result.getRetentionRate()); // 1 Correct -> Correct
        assertEquals(3, result.getData().size());
        assertEquals(0.0, result.getData().get(0).getAccuracy());
        assertEquals(1.0, result.getData().get(1).getAccuracy());
        assertEquals(1.0, result.getData().get(2).getAccuracy());
    }

    private void createResponse(Questao q, Alternativa a, LocalDateTime when, int time, Dificuldade d) {
        Resposta r = new Resposta(q, a);
        r.setTempoRespostaSegundos(time);
        r.setDificuldade(d);
        r = respostaRepository.save(r);
        
        // Bypassing JPA Auditing to set past dates
        // Using the converter to match how SQLite stores it
        com.studora.entity.LocalDateTimeConverter converter = new com.studora.entity.LocalDateTimeConverter();
        String dateStr = converter.convertToDatabaseColumn(when);
        
        jdbcTemplate.update("UPDATE resposta SET created_at = ?, updated_at = ? WHERE id = ?", 
            dateStr, dateStr, r.getId());
        
        entityManager.flush();
        entityManager.clear();
    }
}
