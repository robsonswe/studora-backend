package com.studora.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.studora.dto.concurso.ConcursoCargoSubtemaDto;
import com.studora.dto.concurso.ConcursoSecaoDisciplinaDto;
import com.studora.dto.subtema.DisciplinaReferenceDto;
import com.studora.dto.subtema.TemaReferenceDto;
import com.studora.entity.SecaoDisciplina;
import com.studora.entity.Subtema;

@Mapper(componentModel = "spring")
public interface SecaoDisciplinaMapper {

    @Mapping(target = "assuntos", source = "subtemas", qualifiedByName = "mapSubtemasToAssuntos")
    @Mapping(target = "totalEstudos", ignore = true)
    @Mapping(target = "questaoStats", ignore = true)
    @Mapping(target = "questoesConcursoCargo", ignore = true)
    ConcursoSecaoDisciplinaDto toDto(SecaoDisciplina sd);

    @Named("mapSubtemasToAssuntos")
    default List<ConcursoCargoSubtemaDto> mapSubtemasToAssuntos(java.util.Set<Subtema> subtemas) {
        if (subtemas == null) return List.of();
        return subtemas.stream()
                .map(s -> {
                    ConcursoCargoSubtemaDto dto = new ConcursoCargoSubtemaDto();
                    dto.setId(s.getId());
                    dto.setNome(s.getNome());
                    if (s.getTema() != null) {
                        TemaReferenceDto temaRef = new TemaReferenceDto();
                        temaRef.setId(s.getTema().getId());
                        temaRef.setNome(s.getTema().getNome());
                        dto.setTema(temaRef);
                        if (s.getTema().getDisciplina() != null) {
                            DisciplinaReferenceDto discRef = new DisciplinaReferenceDto();
                            discRef.setId(s.getTema().getDisciplina().getId());
                            discRef.setNome(s.getTema().getDisciplina().getNome());
                            dto.setDisciplina(discRef);
                        }
                    }
                    return dto;
                })
                .sorted(Comparator.comparing(ConcursoCargoSubtemaDto::getNome))
                .collect(Collectors.toList());
    }
}
