package com.studora.dto;

public class Views {
    /**
     * View básica (ex: listagens).
     */
    public interface Summary {}
    
    /**
     * View para exibir detalhes do objeto mas OMITIR gabarito, justificativas e histórico.
     */
    public interface RespostaOculta extends Summary {}
    
    /**
     * View completa para exibir todos os detalhes, incluindo gabarito e histórico.
     */
    public interface RespostaVisivel extends RespostaOculta {}

    /**
     * View para detalhes que devem aparecer apenas na Questão (ex: anulada),
     * mas mantendo a resposta oculta.
     */
    public interface QuestaoOculta extends RespostaOculta {}

    /**
     * View para detalhes que devem aparecer apenas na Questão (ex: anulada),
     * liberando a resposta.
     */
    public interface QuestaoVisivel extends RespostaVisivel, QuestaoOculta {}
}