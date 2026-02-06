package com.studora.mapper;

import com.studora.dto.simulado.SimuladoDetailDto;
import com.studora.dto.simulado.SimuladoSummaryDto;
import com.studora.entity.Simulado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {QuestaoMapper.class})
public interface SimuladoMapper {

    SimuladoSummaryDto toSummaryDto(Simulado simulado);

    @Mapping(target = "questoes", source = "questoes")
    SimuladoDetailDto toDetailDto(Simulado simulado);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Simulado toEntity(SimuladoSummaryDto dto);
}