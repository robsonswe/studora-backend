package com.studora.mapper;

import com.studora.dto.AlternativaDto;
import com.studora.entity.Alternativa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AlternativaMapper {

    @Mapping(target = "questaoId", source = "questao.id")
    AlternativaDto toDto(Alternativa alternativa);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "resposta", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Alternativa toEntity(AlternativaDto alternativaDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "resposta", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(AlternativaDto alternativaDto, @MappingTarget Alternativa alternativa);
}
