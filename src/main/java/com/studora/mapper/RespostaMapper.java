package com.studora.mapper;

import com.studora.dto.resposta.RespostaDetailDto;
import com.studora.dto.resposta.RespostaSummaryDto;
import com.studora.dto.request.RespostaCreateRequest;
import com.studora.entity.Resposta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RespostaMapper {

    @Mapping(target = "questaoId", source = "questao.id")
    @Mapping(target = "alternativaId", source = "alternativaEscolhida.id")
    @Mapping(target = "correta", source = "alternativaEscolhida.correta")
    @Mapping(target = "simuladoId", source = "simulado.id")
    RespostaSummaryDto toSummaryDto(Resposta resposta);

    @Mapping(target = "questaoId", source = "questao.id")
    @Mapping(target = "alternativaId", source = "alternativaEscolhida.id")
    @Mapping(target = "correta", source = "alternativaEscolhida.correta")
    @Mapping(target = "simuladoId", source = "simulado.id")
    RespostaDetailDto toDetailDto(Resposta resposta);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "alternativaEscolhida", ignore = true)
    @Mapping(target = "simulado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "dificuldade", expression = "java(com.studora.entity.Dificuldade.fromId(request.getDificuldadeId()))")
    Resposta toEntity(RespostaCreateRequest request);
}