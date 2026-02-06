package com.studora.mapper;

import com.studora.dto.banca.BancaDetailDto;
import com.studora.dto.banca.BancaSummaryDto;
import com.studora.dto.request.BancaCreateRequest;
import com.studora.dto.request.BancaUpdateRequest;
import com.studora.entity.Banca;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BancaMapper {

    BancaSummaryDto toSummaryDto(Banca banca);

    BancaDetailDto toDetailDto(Banca banca);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Banca toEntity(BancaCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(BancaUpdateRequest request, @MappingTarget Banca banca);
}
