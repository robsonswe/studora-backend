package com.studora.mapper;

import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.entity.Concurso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {InstituicaoMapper.class, BancaMapper.class, CargoMapper.class})
public interface ConcursoMapper {

    @Mapping(target = "instituicao", source = "instituicao")
    @Mapping(target = "banca", source = "banca")
    @Mapping(target = "cargos", source = "concursoCargos")
    ConcursoSummaryDto toSummaryDto(Concurso concurso);

    @Mapping(target = "instituicao", source = "instituicao")
    @Mapping(target = "banca", source = "banca")
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

    default java.util.List<com.studora.dto.concurso.ConcursoCargoSummaryDto> mapCargos(java.util.Set<com.studora.entity.ConcursoCargo> concursoCargos) {
        if (concursoCargos == null) {
            return java.util.Collections.emptyList();
        }
        return concursoCargos.stream()
                .map(cc -> {
                    com.studora.entity.Cargo cargo = cc.getCargo();
                    com.studora.dto.concurso.ConcursoCargoSummaryDto dto = new com.studora.dto.concurso.ConcursoCargoSummaryDto();
                    dto.setId(cc.getId());
                    dto.setCargoId(cargo.getId());
                    dto.setCargoNome(cargo.getNome());
                    dto.setNivel(cargo.getNivel());
                    dto.setArea(cargo.getArea());
                    dto.setInscrito(cc.isInscrito());
                    dto.setTopicos(mapTopicos(cc.getConcursoCargoSubtemas()));
                    return dto;
                })
                .sorted(java.util.Comparator.comparing(com.studora.dto.concurso.ConcursoCargoSummaryDto::getCargoNome))
                .collect(java.util.stream.Collectors.toList());
    }

    default java.util.List<com.studora.dto.subtema.SubtemaSummaryDto> mapTopicos(java.util.Set<com.studora.entity.ConcursoCargoSubtema> concursoCargoSubtemas) {
        if (concursoCargoSubtemas == null) {
            return java.util.Collections.emptyList();
        }
        return concursoCargoSubtemas.stream()
                .map(ccs -> {
                    com.studora.entity.Subtema subtema = ccs.getSubtema();
                    com.studora.dto.subtema.SubtemaSummaryDto dto = new com.studora.dto.subtema.SubtemaSummaryDto();
                    dto.setId(subtema.getId());
                    dto.setNome(subtema.getNome());
                    dto.setTemaId(subtema.getTema().getId());
                    dto.setTemaNome(subtema.getTema().getNome());
                    dto.setDisciplinaId(subtema.getTema().getDisciplina().getId());
                    dto.setDisciplinaNome(subtema.getTema().getDisciplina().getNome());
                    return dto;
                })
                .sorted(java.util.Comparator.comparing(com.studora.dto.subtema.SubtemaSummaryDto::getNome))
                .collect(java.util.stream.Collectors.toList());
    }
}
