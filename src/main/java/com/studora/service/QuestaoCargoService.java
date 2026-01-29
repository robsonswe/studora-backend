package com.studora.service;

import com.studora.dto.QuestaoCargoDto;
import com.studora.entity.ConcursoCargo;
import com.studora.entity.Questao;
import com.studora.entity.QuestaoCargo;
import com.studora.repository.ConcursoCargoRepository;
import com.studora.repository.QuestaoCargoRepository;
import com.studora.repository.QuestaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestaoCargoService {

    @Autowired
    private QuestaoCargoRepository questaoCargoRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private ConcursoCargoRepository concursoCargoRepository;

    public List<QuestaoCargoDto> findAll() {
        return questaoCargoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public QuestaoCargoDto findById(Long id) {
        return questaoCargoRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    public QuestaoCargoDto save(QuestaoCargoDto questaoCargoDto) {
        QuestaoCargo questaoCargo = convertToEntity(questaoCargoDto);
        return convertToDto(questaoCargoRepository.save(questaoCargo));
    }

    public void deleteById(Long id) {
        questaoCargoRepository.deleteById(id);
    }

    private QuestaoCargoDto convertToDto(QuestaoCargo questaoCargo) {
        QuestaoCargoDto questaoCargoDto = new QuestaoCargoDto();
        questaoCargoDto.setId(questaoCargo.getId());
        questaoCargoDto.setQuestaoId(questaoCargo.getQuestao().getId());
        questaoCargoDto.setConcursoCargoId(questaoCargo.getConcursoCargo().getId());
        return questaoCargoDto;
    }

    private QuestaoCargo convertToEntity(QuestaoCargoDto questaoCargoDto) {
        QuestaoCargo questaoCargo = new QuestaoCargo();
        questaoCargo.setId(questaoCargoDto.getId());

        if (questaoCargoDto.getQuestaoId() != null) {
            Questao questao = questaoRepository.findById(questaoCargoDto.getQuestaoId())
                    .orElseThrow(() -> new RuntimeException("Questao not found"));
            questaoCargo.setQuestao(questao);
        }

        if (questaoCargoDto.getConcursoCargoId() != null) {
            ConcursoCargo concursoCargo = concursoCargoRepository.findById(questaoCargoDto.getConcursoCargoId())
                    .orElseThrow(() -> new RuntimeException("ConcursoCargo not found"));
            questaoCargo.setConcursoCargo(concursoCargo);
        }

        return questaoCargo;
    }
}
