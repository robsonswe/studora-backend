package com.studora.mapper;

import com.studora.dto.subtema.SubtemaDetailDto;
import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.dto.request.SubtemaCreateRequest;
import com.studora.dto.request.SubtemaUpdateRequest;
import com.studora.entity.Subtema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {TemaMapper.class})
public interface SubtemaMapper {

    @Mapping(target = "temaId", source = "tema.id")
    SubtemaSummaryDto toSummaryDto(Subtema subtema);

    @Mapping(target = "tema", source = "tema")
    SubtemaDetailDto toDetailDto(Subtema subtema);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tema", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Subtema toEntity(SubtemaCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tema", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(SubtemaUpdateRequest request, @MappingTarget Subtema subtema);
}