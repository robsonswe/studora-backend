package com.studora.mapper;

import com.studora.dto.BancaDto;
import com.studora.entity.Banca;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BancaMapper {

    BancaDto toDto(Banca banca);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Banca toEntity(BancaDto bancaDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(BancaDto bancaDto, @MappingTarget Banca banca);
}
