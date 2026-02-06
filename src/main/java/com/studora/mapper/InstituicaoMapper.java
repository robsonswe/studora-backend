package com.studora.mapper;

import com.studora.dto.instituicao.InstituicaoDetailDto;
import com.studora.dto.instituicao.InstituicaoSummaryDto;
import com.studora.dto.request.InstituicaoCreateRequest;
import com.studora.dto.request.InstituicaoUpdateRequest;
import com.studora.entity.Instituicao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InstituicaoMapper {

    InstituicaoSummaryDto toSummaryDto(Instituicao instituicao);

    InstituicaoDetailDto toDetailDto(Instituicao instituicao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Instituicao toEntity(InstituicaoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(InstituicaoUpdateRequest request, @MappingTarget Instituicao instituicao);
}