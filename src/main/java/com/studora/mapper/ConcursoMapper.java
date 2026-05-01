package com.studora.mapper;

import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.entity.Concurso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {InstituicaoMapper.class, BancaMapper.class, CargoMapper.class, ProvaMapper.class})
public interface ConcursoMapper {

    @Mapping(target = "instituicao", source = "instituicao")
    @Mapping(target = "banca", source = "banca")
    @Mapping(target = "cargos", source = "concursoCargos")
    ConcursoSummaryDto toSummaryDto(Concurso concurso);

    @Mapping(target = "instituicao", source = "instituicao")
    @Mapping(target = "banca", source = "banca")
    @Mapping(target = "cargos", source = "concursoCargos")
    @Mapping(target = "provas", source = "provas")
    ConcursoDetailDto toDetailDto(Concurso concurso);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instituicao", ignore = true)
    @Mapping(target = "banca", ignore = true)
    @Mapping(target = "concursoCargos", ignore = true)
    @Mapping(target = "provas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Concurso toEntity(ConcursoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instituicao", ignore = true)
    @Mapping(target = "banca", ignore = true)
    @Mapping(target = "concursoCargos", ignore = true)
    @Mapping(target = "provas", ignore = true)
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

                    com.studora.entity.Concurso owner = cc.getConcurso();
                    if (owner != null && owner.getProvas() != null) {
                        java.util.List<com.studora.dto.concurso.ConcursoCargoSubtemaDto> topicos = owner.getProvas().stream()
                                // Match cargo by looking at the prova_cargo association set
                                .filter(p -> p.getCargos() != null && p.getCargos().stream()
                                        .anyMatch(pc -> pc.getId().equals(cc.getId())))
                                .flatMap(p -> p.getSecoes().stream())
                                .flatMap(s -> s.getSubtemas().stream())
                                .distinct()
                                .map(s -> {
                                    com.studora.dto.concurso.ConcursoCargoSubtemaDto subDto = new com.studora.dto.concurso.ConcursoCargoSubtemaDto();
                                    subDto.setId(s.getId());
                                    subDto.setNome(s.getNome());
                                    if (s.getTema() != null) {
                                        com.studora.dto.subtema.TemaReferenceDto temaRef = new com.studora.dto.subtema.TemaReferenceDto();
                                        temaRef.setId(s.getTema().getId());
                                        temaRef.setNome(s.getTema().getNome());
                                        subDto.setTema(temaRef);
                                        if (s.getTema().getDisciplina() != null) {
                                            com.studora.dto.subtema.DisciplinaReferenceDto discRef = new com.studora.dto.subtema.DisciplinaReferenceDto();
                                            discRef.setId(s.getTema().getDisciplina().getId());
                                            discRef.setNome(s.getTema().getDisciplina().getNome());
                                            subDto.setDisciplina(discRef);
                                        }
                                    }
                                    return subDto;
                                })
                                .sorted(java.util.Comparator.comparing(com.studora.dto.concurso.ConcursoCargoSubtemaDto::getNome))
                                .collect(java.util.stream.Collectors.toList());
                        dto.setTopicos(topicos);
                    } else {
                        dto.setTopicos(java.util.Collections.emptyList());
                    }

                    return dto;
                })
                .sorted(java.util.Comparator.comparing(com.studora.dto.concurso.ConcursoCargoSummaryDto::getCargoNome))
                .collect(java.util.stream.Collectors.toList());
    }

}
