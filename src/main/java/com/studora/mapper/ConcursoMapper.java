package com.studora.mapper;

import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.entity.Concurso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {InstituicaoMapper.class, BancaMapper.class})
public interface ConcursoMapper {

    @Mapping(target = "instituicaoId", source = "instituicao.id")
    @Mapping(target = "bancaId", source = "banca.id")
    @Mapping(target = "cargos", source = "concursoCargos")
    ConcursoSummaryDto toSummaryDto(Concurso concurso);

    @Mapping(target = "instituicaoId", source = "instituicao.id")
    @Mapping(target = "bancaId", source = "banca.id")
    @Mapping(target = "cargos", source = "concursoCargos")
    ConcursoDetailDto toDetailDto(Concurso concurso);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instituicao", ignore = true)
    @Mapping(target = "banca", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "concursoCargos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Concurso toEntity(ConcursoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instituicao", ignore = true)
    @Mapping(target = "banca", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "concursoCargos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ConcursoUpdateRequest request, @MappingTarget Concurso concurso);

    default java.util.List<Long> mapCargos(java.util.Set<com.studora.entity.ConcursoCargo> concursoCargos) {
        if (concursoCargos == null) {
            return java.util.Collections.emptyList();
        }
        return concursoCargos.stream()
                .map(cc -> cc.getCargo().getId())
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }
}