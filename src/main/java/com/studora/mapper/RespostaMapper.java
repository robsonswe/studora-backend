package com.studora.mapper;

import com.studora.dto.RespostaDto;
import com.studora.dto.RespostaComAlternativasDto;
import com.studora.entity.Resposta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AlternativaMapper.class})
public interface RespostaMapper {

    @Mapping(target = "questaoId", source = "questao.id")
    @Mapping(target = "alternativaId", source = "alternativaEscolhida.id")
    @Mapping(target = "correta", source = "alternativaEscolhida.correta")
    RespostaDto toDto(Resposta resposta);

    @Mapping(target = "questaoId", source = "questao.id")
    @Mapping(target = "alternativaId", source = "alternativaEscolhida.id")
    @Mapping(target = "correta", source = "alternativaEscolhida.correta")
    @Mapping(target = "alternativas", source = "questao.alternativas")
    RespostaComAlternativasDto toComAlternativasDto(Resposta resposta);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "questao", ignore = true)
    @Mapping(target = "alternativaEscolhida", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Resposta toEntity(RespostaDto respostaDto);
}
