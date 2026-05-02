package com.studora.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.studora.dto.prova.ProvaDetailDto;
import com.studora.dto.prova.ProvaSecaoDto;
import com.studora.dto.prova.ProvaSummaryDto;
import com.studora.dto.request.ProvaCreateRequest;
import com.studora.dto.request.ProvaUpdateRequest;
import com.studora.entity.Prova;
import com.studora.entity.ProvaSecao;
import com.studora.entity.Subtema;

@Mapper(componentModel = "spring")
public interface ProvaMapper {
    @Mapping(target = "concursoId", source = "concurso.id")
    @Mapping(target = "cargoId", source = "concursoCargo.cargo.id")
    ProvaSummaryDto toSummaryDto(Prova prova);

    @Mapping(target = "concursoId", source = "concurso.id")
    @Mapping(target = "secoes", source = "secoes")
    @Mapping(target = "cargoId", source = "concursoCargo.cargo.id")
    ProvaDetailDto toDetailDto(Prova prova);

    @Mapping(target = "peso", source = "secaoCargo.peso")
    @Mapping(target = "notaMinima", source = "secaoCargo.notaMinima")
    @Mapping(target = "subtemaIds", source = "secaoCargo.subtemas", qualifiedByName = "mapSubtemasToIds")
    ProvaSecaoDto toSecaoDto(ProvaSecao secao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "concursoCargo", ignore = true)
    @Mapping(target = "secoes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Prova toEntity(ProvaCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "concursoCargo", ignore = true)
    @Mapping(target = "secoes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ProvaUpdateRequest request, @MappingTarget Prova prova);

    @Named("mapSubtemasToIds")
    default List<Long> mapSubtemasToIds(Set<Subtema> subtemas) {
        if (subtemas == null)
            return List.of();
        return subtemas.stream().map(Subtema::getId).collect(Collectors.toList());
    }
}
