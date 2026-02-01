package com.studora.mapper;

import com.studora.dto.SubtemaDto;
import com.studora.entity.Subtema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SubtemaMapper {

    @Mapping(target = "temaId", source = "tema.id")
    SubtemaDto toDto(Subtema subtema);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tema", ignore = true) // Will be handled in service
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Subtema toEntity(SubtemaDto subtemaDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tema", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(SubtemaDto subtemaDto, @MappingTarget Subtema subtema);
}
