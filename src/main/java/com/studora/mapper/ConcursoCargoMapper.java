package com.studora.mapper;

import com.studora.dto.concurso.ConcursoCargoDto;
import com.studora.entity.ConcursoCargo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConcursoCargoMapper {

    @Mapping(target = "concursoId", source = "concurso.id")
    @Mapping(target = "cargoId", source = "cargo.id")
    ConcursoCargoDto toDto(ConcursoCargo concursoCargo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "cargo", ignore = true)
    @Mapping(target = "questaoCargos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ConcursoCargo toEntity(ConcursoCargoDto concursoCargoDto);
}
