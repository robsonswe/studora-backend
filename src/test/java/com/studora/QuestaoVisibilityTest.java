package com.studora;

import com.studora.controller.v1.QuestaoController;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.questao.QuestaoFilter;
import com.studora.service.QuestaoService;
import com.studora.dto.questao.AlternativaDto;
import com.studora.dto.resposta.RespostaSummaryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestaoController.class)
class QuestaoVisibilityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuestaoService questaoService;

    @Test
    void testGetQuestoesWithAdminTrueShowsGabarito() throws Exception {
        QuestaoSummaryDto dto = new QuestaoSummaryDto();
        dto.setId(1L);
        dto.setEnunciado("Questão 1");
        
        AlternativaDto alt = new AlternativaDto();
        alt.setId(10L);
        alt.setTexto("Alternativa A");
        alt.setCorreta(true);
        alt.setJustificativa("Justificativa A");
        dto.setAlternativas(List.of(alt));
        
        // No recent responses
        dto.setRespostas(new ArrayList<>());

        Page<QuestaoSummaryDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        when(questaoService.findAll(any(QuestaoFilter.class), any())).thenReturn(page);

        // admin=true -> correta and justificativa should be visible
        mockMvc.perform(get("/api/v1/questoes")
                .param("admin", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].alternativas[0].correta").value(true))
                .andExpect(jsonPath("$.content[0].alternativas[0].justificativa").value("Justificativa A"));
    }

    @Test
    void testGetQuestoesWithoutAdminHidesGabaritoIfNoRecentResponses() throws Exception {
        QuestaoSummaryDto dto = new QuestaoSummaryDto();
        dto.setId(1L);
        dto.setEnunciado("Questão 1");
        
        AlternativaDto alt = new AlternativaDto();
        alt.setId(10L);
        alt.setTexto("Alternativa A");
        alt.setCorreta(true);
        alt.setJustificativa("Justificativa A");
        dto.setAlternativas(List.of(alt));
        
        // Old response (more than 1 month)
        RespostaSummaryDto resp = new RespostaSummaryDto();
        resp.setCreatedAt(LocalDateTime.now().minusMonths(2));
        dto.setRespostas(List.of(resp));

        Page<QuestaoSummaryDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        when(questaoService.findAll(any(QuestaoFilter.class), any())).thenReturn(page);

        // admin=false (default) -> correta and justificativa should be null
        mockMvc.perform(get("/api/v1/questoes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].alternativas[0].correta").isEmpty())
                .andExpect(jsonPath("$.content[0].alternativas[0].justificativa").isEmpty())
                .andExpect(jsonPath("$.content[0].respostas").isEmpty());
    }
}
