package com.studora.dto;



import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Positive;



public class ConcursoDto {



    private Long id;



    @NotNull(message = "ID da instituição é obrigatório")

    private Long instituicaoId;



    @NotNull(message = "ID da banca é obrigatório")

    private Long bancaId;



    @NotNull(message = "Ano é obrigatório")

    @Positive(message = "Ano deve ser um número positivo")

    private Integer ano;



    // Constructors

    public ConcursoDto() {}



    public ConcursoDto(Long instituicaoId, Long bancaId, Integer ano) {

        this.instituicaoId = instituicaoId;

        this.bancaId = bancaId;

        this.ano = ano;

    }



    // Getters and Setters

    public Long getId() {

        return id;

    }



    public void setId(Long id) {

        this.id = id;

    }



    public Long getInstituicaoId() {

        return instituicaoId;

    }



    public void setInstituicaoId(Long instituicaoId) {

        this.instituicaoId = instituicaoId;

    }



    public Long getBancaId() {

        return bancaId;

    }



    public void setBancaId(Long bancaId) {

        this.bancaId = bancaId;

    }



    public Integer getAno() {

        return ano;

    }



    public void setAno(Integer ano) {

        this.ano = ano;

    }

}
