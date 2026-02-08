package com.studora.mapper;

import com.studora.dto.questao.ConcursoQuestaoDto;
import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.questao.SubtemaQuestaoDto;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.entity.Questao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {AlternativaMapper.class, SubtemaMapper.class, RespostaMapper.class, CargoMapper.class, ConcursoMapper.class})
public interface QuestaoMapper {

    @Mapping(target = "concurso", source = "concurso")
    @Mapping(target = "cargos", source = "questaoCargos")
    @Mapping(target = "alternativas", source = "alternativas")
    @Mapping(target = "respostas", source = "respostas")
    @Mapping(target = "subtemas", source = "subtemas")
    QuestaoSummaryDto toSummaryDto(Questao questao);

    @Mapping(target = "concurso", source = "concurso")
    @Mapping(target = "alternativas", source = "alternativas")
    @Mapping(target = "subtemas", source = "subtemas")
    @Mapping(target = "cargos", source = "questaoCargos")
    @Mapping(target = "respostas", source = "respostas")
    QuestaoDetailDto toDetailDto(Questao questao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "alternativas", ignore = true)
    @Mapping(target = "subtemas", ignore = true)
    @Mapping(target = "questaoCargos", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Questao toEntity(QuestaoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "concurso", ignore = true)
    @Mapping(target = "alternativas", ignore = true)
    @Mapping(target = "subtemas", ignore = true)
    @Mapping(target = "questaoCargos", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(QuestaoUpdateRequest request, @MappingTarget Questao questao);

    @Mapping(target = "bancaId", source = "banca.id")
    @Mapping(target = "bancaNome", source = "banca.nome")
    @Mapping(target = "instituicaoId", source = "instituicao.id")
    @Mapping(target = "instituicaoNome", source = "instituicao.nome")
    @Mapping(target = "instituicaoArea", source = "instituicao.area")
    ConcursoQuestaoDto toConcursoQuestaoDto(com.studora.entity.Concurso concurso);

    @Mapping(target = "temaId", source = "tema.id")
    @Mapping(target = "temaNome", source = "tema.nome")
    @Mapping(target = "disciplinaId", source = "tema.disciplina.id")
    @Mapping(target = "disciplinaNome", source = "tema.disciplina.nome")
    SubtemaQuestaoDto toSubtemaQuestaoDto(com.studora.entity.Subtema subtema);

    default java.util.List<com.studora.dto.cargo.CargoSummaryDto> mapCargos(java.util.Set<com.studora.entity.QuestaoCargo> questaoCargos) {
        if (questaoCargos == null) {
            return java.util.Collections.emptyList();
        }
        return questaoCargos.stream()
                .map(qc -> {
                    com.studora.entity.Cargo cargo = qc.getConcursoCargo().getCargo();
                    com.studora.dto.cargo.CargoSummaryDto dto = new com.studora.dto.cargo.CargoSummaryDto();
                    dto.setId(cargo.getId());
                    dto.setNome(cargo.getNome());
                    dto.setNivel(cargo.getNivel());
                    dto.setArea(cargo.getArea());
                    return dto;
                })
                .sorted(java.util.Comparator.comparing(com.studora.dto.cargo.CargoSummaryDto::getNome))
                .collect(java.util.stream.Collectors.toList());
    }
}