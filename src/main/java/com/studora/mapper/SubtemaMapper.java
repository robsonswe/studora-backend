package com.studora.mapper;

import com.studora.dto.subtema.DisciplinaReferenceDto;
import com.studora.dto.subtema.SubtemaDetailDto;
import com.studora.dto.subtema.SubtemaSummaryDto;
import com.studora.dto.subtema.TemaReferenceDto;
import com.studora.dto.request.SubtemaCreateRequest;
import com.studora.dto.request.SubtemaUpdateRequest;
import com.studora.entity.Subtema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SubtemaMapper {

    SubtemaSummaryDto toSummaryDto(Subtema subtema);

    @Mapping(target = "questaoStats", ignore = true)
    @Mapping(target = "disciplina", source = "tema.disciplina")
    SubtemaDetailDto toDetailDto(Subtema subtema);

    default TemaReferenceDto toTemaReference(com.studora.entity.Tema tema) {
        if (tema == null) return null;
        TemaReferenceDto ref = new TemaReferenceDto();
        ref.setId(tema.getId());
        ref.setNome(tema.getNome());
        return ref;
    }

    default DisciplinaReferenceDto toDisciplinaReference(com.studora.entity.Disciplina disciplina) {
        if (disciplina == null) return null;
        DisciplinaReferenceDto ref = new DisciplinaReferenceDto();
        ref.setId(disciplina.getId());
        ref.setNome(disciplina.getNome());
        return ref;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tema", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Subtema toEntity(SubtemaCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tema", ignore = true)
    @Mapping(target = "questoes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(SubtemaUpdateRequest request, @MappingTarget Subtema subtema);
}