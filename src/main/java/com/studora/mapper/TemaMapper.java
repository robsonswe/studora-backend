package com.studora.mapper;

import com.studora.dto.tema.TemaDetailDto;
import com.studora.dto.tema.TemaSummaryDto;
import com.studora.dto.subtema.DisciplinaReferenceDto;
import com.studora.dto.request.TemaCreateRequest;
import com.studora.dto.request.TemaUpdateRequest;
import com.studora.entity.Tema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {DisciplinaMapper.class})
public interface TemaMapper {

    @Mapping(target = "questaoStats", ignore = true)
    @Mapping(target = "subtemas", ignore = true)
    TemaSummaryDto toSummaryDto(Tema tema);

    @Mapping(target = "disciplina", source = "disciplina")
    @Mapping(target = "subtemas", source = "subtemas")
    TemaDetailDto toDetailDto(Tema tema);

    default DisciplinaReferenceDto toDisciplinaReference(com.studora.entity.Disciplina disciplina) {
        if (disciplina == null) return null;
        DisciplinaReferenceDto ref = new DisciplinaReferenceDto();
        ref.setId(disciplina.getId());
        ref.setNome(disciplina.getNome());
        return ref;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disciplina", ignore = true)
    @Mapping(target = "subtemas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tema toEntity(TemaCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "disciplina", ignore = true)
    @Mapping(target = "subtemas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(TemaUpdateRequest request, @MappingTarget Tema tema);
}
