package com.studora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class QuestaoDto {

    private Long id;

    @NotNull(message = "ID do concurso é obrigatório")
    private Long concursoId;

    @NotBlank(message = "Enunciado da questão é obrigatório")
    private String enunciado;

    private Boolean anulada = false;

    private List<Long> subtemaIds; // IDs of associated subtemas

    private List<Long> concursoCargoIds; // IDs of associated ConcursoCargo records

    // Constructors
    public QuestaoDto() {}

    public QuestaoDto(Long concursoId, String enunciado) {
        this.concursoId = concursoId;
        this.enunciado = enunciado;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConcursoId() {
        return concursoId;
    }

    public void setConcursoId(Long concursoId) {
        this.concursoId = concursoId;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public Boolean getAnulada() {
        return anulada;
    }

    public void setAnulada(Boolean anulada) {
        this.anulada = anulada;
    }

    public List<Long> getSubtemaIds() {
        return subtemaIds;
    }

    public void setSubtemaIds(List<Long> subtemaIds) {
        this.subtemaIds = subtemaIds;
    }

    public List<Long> getConcursoCargoIds() {
        return concursoCargoIds;
    }

    public void setConcursoCargoIds(List<Long> concursoCargoIds) {
        this.concursoCargoIds = concursoCargoIds;
    }
}
