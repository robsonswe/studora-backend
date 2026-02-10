package com.studora;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.studora.controller.v1.AnalyticsController;
import com.studora.dto.analytics.ConsistencyDto;
import com.studora.dto.analytics.EvolutionDto;
import com.studora.dto.analytics.LearningRateDto;
import com.studora.dto.analytics.TopicMasteryDto;
import com.studora.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    void testGetConsistencia() throws Exception {
        ConsistencyDto dto = ConsistencyDto.builder()
                .date(LocalDate.now())
                .totalAnswered(10)
                .totalCorrect(8)
                .totalTimeSeconds(600)
                .activeStreak(3)
                .build();

        when(analyticsService.getConsistencia(anyInt())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/analytics/consistencia")
                .param("days", "7")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalAnswered").value(10))
                .andExpect(jsonPath("$[0].activeStreak").value(3));
    }

    @Test
    void testGetDisciplinasMastery() throws Exception {
        TopicMasteryDto dto = TopicMasteryDto.builder()
                .id(1L)
                .nome("Direito")
                .totalAttempts(100)
                .correctAttempts(70)
                .masteryScore(70.0)
                .children(Collections.emptyList())
                .build();

        org.springframework.data.domain.Page<TopicMasteryDto> page = new org.springframework.data.domain.PageImpl<>(
                List.of(dto), org.springframework.data.domain.PageRequest.of(0, 20), 1);

        when(analyticsService.getDisciplinasMastery(any(), any(), any(), anyString(), anyString())).thenReturn(page);

        mockMvc.perform(get("/api/v1/analytics/disciplinas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Direito"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void testGetDisciplinaMasteryDetail() throws Exception {
        TopicMasteryDto dto = TopicMasteryDto.builder()
                .id(1L)
                .nome("Direito")
                .totalAttempts(100)
                .correctAttempts(70)
                .masteryScore(70.0)
                .children(List.of(
                    TopicMasteryDto.builder()
                        .id(10L)
                        .nome("Tema 1")
                        .totalAttempts(10)
                        .children(Collections.emptyList())
                        .build()
                ))
                .build();

        when(analyticsService.getDisciplinaMasteryDetail(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/analytics/disciplinas/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Direito"))
                .andExpect(jsonPath("$.children[0].nome").value("Tema 1"));
    }

    @Test
    void testGetEvolucao() throws Exception {
        EvolutionDto dto = EvolutionDto.builder()
                .period("2026-W05")
                .overallAccuracy(0.75)
                .avgResponseTime(45)
                .difficultyDistribution(Map.of("FACIL", 0.5, "MEDIA", 0.5))
                .build();

        when(analyticsService.getEvolucao()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/analytics/evolucao")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].period").value("2026-W05"))
                .andExpect(jsonPath("$[0].overallAccuracy").value(0.75));
    }

    @Test
    void testGetTaxaAprendizado() throws Exception {
        LearningRateDto dto = LearningRateDto.builder()
                .totalRepeatedQuestions(50)
                .recoveryRate(0.4)
                .retentionRate(0.9)
                .data(List.of(new LearningRateDto.AttemptData(1, 0.3), new LearningRateDto.AttemptData(2, 0.7)))
                .build();

        when(analyticsService.getTaxaAprendizado()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/analytics/taxa-aprendizado")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRepeatedQuestions").value(50))
                .andExpect(jsonPath("$.recoveryRate").value(0.4))
                .andExpect(jsonPath("$.data[1].accuracy").value(0.7));
    }
}
