package com.studora.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestaoStatsDto {
    private StatSliceDto total;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private StatSliceDto porAutoral;
    private Map<String, StatSliceDto> porNivel;
    private Map<Long, StatSliceDto> porBanca;
    private Map<Long, StatSliceDto> porInstituicao;
    private Map<String, StatSliceDto> porAreaInstituicao;
    private Map<Long, StatSliceDto> porCargo;
    private Map<String, StatSliceDto> porAreaCargo;

    public void setTotal(StatSliceDto total) { this.total = total; }
    public void setPorAutoral(StatSliceDto porAutoral) { this.porAutoral = porAutoral; }
    public void setPorNivel(Map<String, StatSliceDto> porNivel) { this.porNivel = porNivel; }
    public void setPorBanca(Map<Long, StatSliceDto> porBanca) { this.porBanca = porBanca; }
    public void setPorInstituicao(Map<Long, StatSliceDto> porInstituicao) { this.porInstituicao = porInstituicao; }
    public void setPorAreaInstituicao(Map<String, StatSliceDto> porAreaInstituicao) { this.porAreaInstituicao = porAreaInstituicao; }
    public void setPorCargo(Map<Long, StatSliceDto> porCargo) { this.porCargo = porCargo; }
    public void setPorAreaCargo(Map<String, StatSliceDto> porAreaCargo) { this.porAreaCargo = porAreaCargo; }
}
