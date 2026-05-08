package com.studora.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;

import com.studora.dto.questao.ConcursoQuestaoDto;
import com.studora.dto.questao.QuestaoDetailDto;
import com.studora.dto.questao.QuestaoSummaryDto;
import com.studora.dto.questao.SubtemaQuestaoDto;
import com.studora.dto.request.QuestaoCreateRequest;
import com.studora.dto.request.QuestaoUpdateRequest;
import com.studora.entity.Questao;

@Mapper(componentModel = "spring", uses = {AlternativaMapper.class, SubtemaMapper.class, RespostaMapper.class, CargoMapper.class, ConcursoMapper.class}, nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
public interface QuestaoMapper {

    @Mapping(target = "concurso", expression = "java(mapConcursoFromSecoes(questao.getSecoes()))")
    @Mapping(target = "alternativas", source = "alternativas")
    @Mapping(target = "respostas", source = "respostas")
    @Mapping(target = "subtemas", expression = "java(mapSubtemas(questao.getQuestaoSubtemas()))")
    @Mapping(target = "respondida", expression = "java(questao.getRespostas() != null && !questao.getRespostas().isEmpty())")
    @Mapping(target = "autoral", source = "autoral")
    QuestaoSummaryDto toSummaryDto(Questao questao);

    @Mapping(target = "concurso", expression = "java(mapConcursoFromSecoes(questao.getSecoes()))")
    @Mapping(target = "alternativas", source = "alternativas")
    @Mapping(target = "subtemas", expression = "java(mapSubtemas(questao.getQuestaoSubtemas()))")
    @Mapping(target = "respostas", source = "respostas")
    @Mapping(target = "respondida", expression = "java(questao.getRespostas() != null && !questao.getRespostas().isEmpty())")
    @Mapping(target = "autoral", source = "autoral")
    QuestaoDetailDto toDetailDto(Questao questao);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alternativas", ignore = true)
    @Mapping(target = "questaoSubtemas", ignore = true)
    @Mapping(target = "secoes", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "autoral", source = "autoral")
    Questao toEntity(QuestaoCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "alternativas", ignore = true)
    @Mapping(target = "questaoSubtemas", ignore = true)
    @Mapping(target = "secoes", ignore = true)
    @Mapping(target = "respostas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "autoral", ignore = true)
    void updateEntityFromDto(QuestaoUpdateRequest request, @MappingTarget Questao questao);

    @Mapping(target = "bancaId", source = "banca.id")
    @Mapping(target = "bancaNome", source = "banca.nome")
    @Mapping(target = "bancaSigla", source = "banca.sigla")
    @Mapping(target = "instituicaoId", source = "instituicao.id")
    @Mapping(target = "instituicaoNome", source = "instituicao.nome")
    @Mapping(target = "instituicaoSigla", source = "instituicao.sigla")
    @Mapping(target = "instituicaoArea", source = "instituicao.area")
    @Mapping(target = "cargos", ignore = true)
    ConcursoQuestaoDto toConcursoQuestaoDto(com.studora.entity.Concurso concurso);

    @Mapping(target = "tema", source = "tema")
    @Mapping(target = "disciplina", source = "tema.disciplina")
    SubtemaQuestaoDto toSubtemaQuestaoDto(com.studora.entity.Subtema subtema);

    
    default ConcursoQuestaoDto mapConcursoFromSecoes(java.util.Set<com.studora.entity.QuestaoProvaSecao> qpSecoes) {
        if (qpSecoes == null || qpSecoes.isEmpty()) return null;
        
        com.studora.entity.QuestaoProvaSecao firstQps = qpSecoes.iterator().next();
        if (firstQps.getProvaSecao() != null && firstQps.getProvaSecao().getProva() != null) {
            com.studora.entity.Concurso concurso = firstQps.getProvaSecao().getProva().getConcurso();
            if (concurso != null) {
                ConcursoQuestaoDto dto = toConcursoQuestaoDto(concurso);
                
                // Map to group cargos and their secoes
                java.util.Map<Long, com.studora.dto.questao.CargoQuestaoDto> cargoMap = new java.util.LinkedHashMap<>();
                
                for (com.studora.entity.QuestaoProvaSecao qps : qpSecoes) {
                    com.studora.entity.ProvaSecao ps = qps.getProvaSecao();
                    if (ps == null || ps.getProva() == null || ps.getProva().getConcursoCargo() == null) continue;
                    
                    com.studora.dto.questao.SecaoQuestaoDto sDto = new com.studora.dto.questao.SecaoQuestaoDto();
                    sDto.setId(ps.getId());
                    sDto.setNome(ps.getNome());
                    sDto.setProvaNome(ps.getProva().getNome());
                    sDto.setProvaId(ps.getProva().getId());
                    sDto.setNumeroQuestao(qps.getNumeroQuestao());
                    if (qps.getSecaoDisciplina() != null) {
                        sDto.setDisciplinaEditalId(qps.getSecaoDisciplina().getId());
                        sDto.setDisciplinaEditalNome(qps.getSecaoDisciplina().getNome());
                    }
                    
                    com.studora.entity.Cargo cargo = ps.getProva().getConcursoCargo().getCargo();
                    if (cargo == null) continue;

                    com.studora.dto.questao.CargoQuestaoDto cDto = cargoMap.computeIfAbsent(cargo.getId(), id -> {
                        com.studora.dto.questao.CargoQuestaoDto newCDto = new com.studora.dto.questao.CargoQuestaoDto();
                        newCDto.setId(cargo.getId());
                        newCDto.setNome(cargo.getNome());
                        newCDto.setNivel(cargo.getNivel());
                        newCDto.setArea(cargo.getArea());
                        newCDto.setSecoes(new java.util.ArrayList<>());
                        return newCDto;
                    });
                    
                    // Avoid duplicates if a question is somehow linked twice to the same section
                    if (cDto.getSecoes().stream().noneMatch(existing -> existing.getId().equals(sDto.getId()))) {
                        cDto.getSecoes().add(sDto);
                    }
                }
                
                dto.setCargos(new java.util.ArrayList<>(cargoMap.values()));
                return dto;
            }
        }
        return null;
    }

    default java.util.List<SubtemaQuestaoDto> mapSubtemas(java.util.Set<com.studora.entity.QuestaoSubtema> questaoSubtemas) {
        if (questaoSubtemas == null) return null;
        return questaoSubtemas.stream()
                .map(qs -> {
                    SubtemaQuestaoDto dto = toSubtemaQuestaoDto(qs.getSubtema());
                    dto.setPrincipal(qs.getPrincipal());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}