package com.studora.mapper;

import com.studora.dto.concurso.ConcursoDetailDto;
import com.studora.dto.concurso.ConcursoSummaryDto;
import com.studora.dto.request.ConcursoCreateRequest;
import com.studora.dto.request.ConcursoUpdateRequest;
import com.studora.entity.Concurso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {InstituicaoMapper.class, BancaMapper.class, CargoMapper.class, ProvaMapper.class})
public interface ConcursoMapper {

    @Mapping(target = "instituicao", source = "instituicao")
    @Mapping(target = "banca", source = "banca")
    @Mapping(target = "cargos", source = "concursoCargos", qualifiedByName = "mapCargosWithoutTopicos")
    ConcursoSummaryDto toSummaryDto(Concurso concurso);

    @Mapping(target = "instituicao", source = "instituicao")
    @Mapping(target = "banca", source = "banca")
    @Mapping(target = "cargos", source = "concursoCargos", qualifiedByName = "mapCargosWithTopicos")
    
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

    @Named("mapCargosWithTopicos")
    default java.util.List<com.studora.dto.concurso.ConcursoCargoSummaryDto> mapCargosWithTopicos(java.util.Set<com.studora.entity.ConcursoCargo> concursoCargos) {
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

                    if (cc.getProvas() != null && !cc.getProvas().isEmpty()) {
                        java.util.List<com.studora.dto.concurso.ConcursoProvaDto> provas = cc.getProvas().stream()
                                .map(p -> {
                                    com.studora.dto.concurso.ConcursoProvaDto provaDto = new com.studora.dto.concurso.ConcursoProvaDto();
                                    provaDto.setId(p.getId());
                                    provaDto.setNome(p.getNome());
                                    return provaDto;
                                })
                                .collect(java.util.stream.Collectors.toList());
                        dto.setProvas(provas);
                    }

                    com.studora.entity.Concurso owner = cc.getConcurso();
                    if (owner != null && owner.getProvas() != null) {
                        java.util.List<com.studora.dto.concurso.ConcursoSecaoDto> topicos = cc.getSecaoCargos().stream()
                                .map(sc -> {
                                    com.studora.dto.concurso.ConcursoSecaoDto secaoDto = new com.studora.dto.concurso.ConcursoSecaoDto();
                                    secaoDto.setId(sc.getId());
                                    secaoDto.setNome(sc.getNome());
                                    secaoDto.setPeso(sc.getPeso());
                                    secaoDto.setNotaMinima(sc.getNotaMinima());
                                    secaoDto.setOrdem(sc.getOrdem());
                                    secaoDto.setNumQuestoes(sc.getNumQuestoes());

                                    java.util.List<com.studora.dto.concurso.ConcursoCargoSubtemaDto> assuntos = sc.getSubtemas().stream()
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
                                    secaoDto.setAssuntos(assuntos);
                                    return secaoDto;
                                })
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

    @Named("mapProvasForDetail")
    default java.util.List<com.studora.dto.prova.ProvaDetailDto> mapProvasForDetail(java.util.Set<com.studora.entity.Prova> provas) {
        if (provas == null) {
            return java.util.Collections.emptyList();
        }
        return provas.stream()
                .map(p -> {
                    com.studora.dto.prova.ProvaDetailDto dto = new com.studora.dto.prova.ProvaDetailDto();
                    dto.setId(p.getId());
                    dto.setNome(p.getNome());
                    if (p.getConcursoCargo() != null && p.getConcursoCargo().getCargo() != null) {
                        dto.setCargoId(p.getConcursoCargo().getCargo().getId());
                    }
                    if (p.getSecoes() != null) {
                        java.util.List<com.studora.dto.prova.ProvaSecaoDto> secoes = p.getSecoes().stream()
                                .map(s -> {
                                    com.studora.dto.prova.ProvaSecaoDto secaoDto = new com.studora.dto.prova.ProvaSecaoDto();
                                    secaoDto.setId(s.getId());
                                    secaoDto.setNome(s.getNome());
                                    secaoDto.setOrdem(s.getOrdem());
                                    secaoDto.setNumQuestoes(s.getNumQuestoes());
                                    return secaoDto;
                                })
                                .collect(java.util.stream.Collectors.toList());
                        dto.setSecoes(secoes);
                    }
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Named("mapCargosWithoutTopicos")
    default java.util.List<com.studora.dto.concurso.ConcursoCargoSummaryDto> mapCargosWithoutTopicos(java.util.Set<com.studora.entity.ConcursoCargo> concursoCargos) {
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
                    return dto;
                })
                .sorted(java.util.Comparator.comparing(com.studora.dto.concurso.ConcursoCargoSummaryDto::getCargoNome))
                .collect(java.util.stream.Collectors.toList());
    }

}
