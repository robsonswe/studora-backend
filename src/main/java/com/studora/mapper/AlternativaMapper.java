package com.studora.mapper;

import com.studora.dto.questao.AlternativaDto;
import com.studora.dto.request.AlternativaCreateRequest;
import com.studora.entity.Alternativa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AlternativaMapper {

    @Mapping(target = "questaoId", source = "questao.id")
    AlternativaDto toDto(Alternativa alternativa);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questaoId", ignore = true)
    AlternativaDto toDto(AlternativaCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Alternativa toEntity(AlternativaCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(AlternativaCreateRequest request, @MappingTarget Alternativa alternativa);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Alternativa toEntity(com.studora.dto.request.AlternativaUpdateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(com.studora.dto.request.AlternativaUpdateRequest request, @MappingTarget Alternativa alternativa);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Alternativa toEntity(AlternativaDto alternativaDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(AlternativaDto alternativaDto, @MappingTarget Alternativa alternativa);
}
