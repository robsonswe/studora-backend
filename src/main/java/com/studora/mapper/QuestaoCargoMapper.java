package com.studora.mapper;

import com.studora.dto.QuestaoCargoDto;
import com.studora.entity.QuestaoCargo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QuestaoCargoMapper {

    @Mapping(target = "questaoId", source = "questao.id")
    @Mapping(target = "concursoCargoId", source = "concursoCargo.id")
    QuestaoCargoDto toDto(QuestaoCargo questaoCargo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "concursoCargo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    QuestaoCargo toEntity(QuestaoCargoDto questaoCargoDto);
}
