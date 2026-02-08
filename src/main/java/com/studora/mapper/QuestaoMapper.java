package com.studora.mapper;

import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.entity.Questao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {AlternativaMapper.class, SubtemaMapper.class, RespostaMapper.class})
public interface QuestaoMapper {

    @Mapping(target = "concursoId", source = "concurso.id")
    @Mapping(target = "cargos", source = "questaoCargos")
    @Mapping(target = "alternativas", source = "alternativas")
    @Mapping(target = "respostas", source = "respostas")
    @Mapping(target = "subtemaIds", source = "subtemas")
    QuestaoSummaryDto toSummaryDto(Questao questao);

    @Mapping(target = "concursoId", source = "concurso.id")
    @Mapping(target = "alternativas", source = "alternativas")
    @Mapping(target = "subtemaIds", source = "subtemas")
    @Mapping(target = "cargos", source = "questaoCargos")
    @Mapping(target = "respostas", source = "respostas")
    QuestaoDetailDto toDetailDto(Questao questao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "alternativas", ignore = true)
    @Mapping(target = "subtemas", ignore = true)
    @Mapping(target = "questaoCargos", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Questao toEntity(QuestaoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "alternativas", ignore = true)
    @Mapping(target = "subtemas", ignore = true)
    @Mapping(target = "questaoCargos", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(QuestaoUpdateRequest request, @MappingTarget Questao questao);

    default java.util.List<Long> mapCargos(java.util.Set<com.studora.entity.QuestaoCargo> questaoCargos) {
        if (questaoCargos == null) {
            return java.util.Collections.emptyList();
        }
        return questaoCargos.stream()
                .map(qc -> qc.getConcursoCargo().getCargo().getId())
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }

    default java.util.List<Long> mapSubtemas(java.util.Set<com.studora.entity.Subtema> subtemas) {
        if (subtemas == null) {
            return java.util.Collections.emptyList();
        }
        return subtemas.stream()
                .map(com.studora.entity.Subtema::getId)
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }
}