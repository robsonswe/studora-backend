package com.studora.mapper;

import com.studora.dto.InstituicaoDto;
import com.studora.entity.Instituicao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InstituicaoMapper {

    InstituicaoDto toDto(Instituicao instituicao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Instituicao toEntity(InstituicaoDto instituicaoDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(InstituicaoDto instituicaoDto, @MappingTarget Instituicao instituicao);
}
