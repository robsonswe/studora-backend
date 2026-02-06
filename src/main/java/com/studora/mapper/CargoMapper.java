package com.studora.mapper;

import com.studora.dto.cargo.CargoDetailDto;
import com.studora.dto.cargo.CargoSummaryDto;
import com.studora.dto.request.CargoCreateRequest;
import com.studora.dto.request.CargoUpdateRequest;
import com.studora.entity.Cargo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CargoMapper {

    CargoSummaryDto toSummaryDto(Cargo cargo);

    CargoDetailDto toDetailDto(Cargo cargo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cargo toEntity(CargoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CargoUpdateRequest request, @MappingTarget Cargo cargo);
}