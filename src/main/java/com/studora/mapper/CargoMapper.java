package com.studora.mapper;

import com.studora.dto.CargoDto;
import com.studora.entity.Cargo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CargoMapper {

    CargoDto toDto(Cargo cargo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cargo toEntity(CargoDto cargoDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CargoDto cargoDto, @MappingTarget Cargo cargo);
}
