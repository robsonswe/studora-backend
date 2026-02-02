package com.studora.mapper;

import com.studora.dto.QuestaoDto;
import com.studora.entity.Questao;
import com.studora.entity.Subtema;
import com.studora.entity.ConcursoCargo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AlternativaMapper.class})
public interface QuestaoMapper {

    @Mapping(target = "concursoId", source = "concurso.id")
    @Mapping(target = "subtemaIds", source = "subtemas")
    @Mapping(target = "concursoCargoIds", source = "questaoCargos")
    QuestaoDto toDto(Questao questao);

    @Mapping(target = "id", ignore = true)
    QuestaoDto toDto(QuestaoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    QuestaoDto toDto(QuestaoUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "subtemas", ignore = true)
    @Mapping(target = "questaoCargos", ignore = true)
    @Mapping(target = "alternativas", ignore = true)
    @Mapping(target = "resposta", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Questao toEntity(QuestaoDto questaoDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "subtemas", ignore = true)
    @Mapping(target = "questaoCargos", ignore = true)
    @Mapping(target = "alternativas", ignore = true)
    @Mapping(target = "resposta", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(QuestaoDto questaoDto, @MappingTarget Questao questao);

    default Long mapSubtemaToId(Subtema subtema) {
        return subtema != null ? subtema.getId() : null;
    }

    default Long mapQuestaoCargoToId(com.studora.entity.QuestaoCargo questaoCargo) {
        return (questaoCargo != null && questaoCargo.getConcursoCargo() != null) 
            ? questaoCargo.getConcursoCargo().getId() : null;
    }
}
