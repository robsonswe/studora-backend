package com.studora;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.studora.dto.QuestaoCargoDto;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Questao;
import com.studora.entity.QuestaoCargo;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.QuestaoCargoRepository;
import com.studora.repository.QuestaoRepository;
import com.studora.service.QuestaoCargoService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class QuestaoCargoServiceTest {

    @Mock
    private QuestaoCargoRepository questaoCargoRepository;

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private ConcursoCargoRepository concursoCargoRepository;

    @InjectMocks
    private QuestaoCargoService questaoCargoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave() {
        QuestaoCargoDto dto = new QuestaoCargoDto();
        dto.setQuestaoId(1L);
        dto.setConcursoCargoId(2L);

        when(questaoRepository.findById(1L)).thenReturn(
            Optional.of(new Questao())
        );
        when(concursoCargoRepository.findById(2L)).thenReturn(
            Optional.of(new ConcursoCargo())
        );

        QuestaoCargo saved = new QuestaoCargo();
        saved.setId(5L);
        saved.setQuestao(new Questao());
        saved.getQuestao().setId(1L);
        saved.setConcursoCargo(new ConcursoCargo());
        saved.getConcursoCargo().setId(2L);

        when(questaoCargoRepository.save(any(QuestaoCargo.class))).thenReturn(
            saved
        );

        QuestaoCargoDto result = questaoCargoService.save(dto);
        assertEquals(5L, result.getId());
    }
}
