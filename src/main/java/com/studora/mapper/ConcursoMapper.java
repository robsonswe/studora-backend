package com.studora.mapper;

import com.studora.dto.ConcursoDto;
import com.studora.entity.Concurso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ConcursoMapper {

    @Mapping(target = "instituicaoId", source = "instituicao.id")
    @Mapping(target = "bancaId", source = "banca.id")
    ConcursoDto toDto(Concurso concurso);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instituicao", ignore = true)
    @Mapping(target = "banca", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "concursoCargos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Concurso toEntity(ConcursoDto concursoDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instituicao", ignore = true)
    @Mapping(target = "banca", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "concursoCargos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ConcursoDto concursoDto, @MappingTarget Concurso concurso);
}
