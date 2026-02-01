package com.studora.mapper;

import com.studora.dto.DisciplinaDto;
import com.studora.entity.Disciplina;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DisciplinaMapper {

    DisciplinaDto toDto(Disciplina disciplina);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Disciplina toEntity(DisciplinaDto disciplinaDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(DisciplinaDto disciplinaDto, @MappingTarget Disciplina disciplina);
}
