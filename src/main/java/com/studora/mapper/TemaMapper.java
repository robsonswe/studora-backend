package com.studora.mapper;

import com.studora.dto.TemaDto;
import com.studora.entity.Tema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TemaMapper {

    @Mapping(target = "disciplinaId", source = "disciplina.id")
    TemaDto toDto(Tema tema);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disciplina", ignore = true) // Will be handled in service
    @Mapping(target = "subtemas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tema toEntity(TemaDto temaDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disciplina", ignore = true)
    @Mapping(target = "subtemas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(TemaDto temaDto, @MappingTarget Tema tema);
}
