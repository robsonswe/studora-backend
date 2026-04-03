package com.studora.repository;

import com.studora.entity.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, Long> {
    
    Optional<Resposta> findFirstByQuestaoIdOrderByCreatedAtDesc(Long questaoId);

    List<Resposta> findByQuestaoIdOrderByCreatedAtDesc(Long questaoId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Resposta r WHERE r.questao.id = :questaoId")
    void deleteByQuestaoId(@Param("questaoId") Long questaoId);

    @Query("SELECT r FROM Resposta r " +
           "JOIN FETCH r.questao " +
           "JOIN FETCH r.alternativaEscolhida " +
           "WHERE r.id = :id")
    Optional<Resposta> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT r FROM Resposta r " +
           "JOIN FETCH r.questao " +
           "JOIN FETCH r.alternativaEscolhida " +
           "WHERE r.questao.id = :questaoId " +
           "ORDER BY r.createdAt DESC")
    List<Resposta> findByQuestaoIdWithDetails(@Param("questaoId") Long questaoId);

    @Query("SELECT r FROM Resposta r " +
           "JOIN FETCH r.questao " +
           "JOIN FETCH r.alternativaEscolhida " +
           "WHERE r.questao.id IN :questaoIds " +
           "ORDER BY r.createdAt DESC")
    List<Resposta> findByQuestaoIdInWithDetails(@Param("questaoIds") Collection<Long> questaoIds);

    List<Resposta> findBySimuladoId(Long simuladoId);

    int countBySimuladoId(Long simuladoId);

    @Transactional
    @Modifying
    @Query("UPDATE Resposta r SET r.simulado = null WHERE r.simulado.id = :simuladoId")
    void detachSimulado(@Param("simuladoId") Long simuladoId);

    @Query("SELECT DISTINCT r FROM Resposta r " +
           "JOIN FETCH r.questao q " +
           "JOIN FETCH r.alternativaEscolhida " +
           "JOIN FETCH q.alternativas " +
           "ORDER BY r.createdAt ASC")
    List<Resposta> findAllWithFullDetails();

    @Query("SELECT DISTINCT r FROM Resposta r " +
           "JOIN FETCH r.questao q " +
           "JOIN FETCH r.alternativaEscolhida " +
           "JOIN FETCH q.alternativas " +
           "WHERE r.createdAt >= :since " +
           "ORDER BY r.createdAt ASC")
    List<Resposta> findAllWithFullDetailsSince(@Param("since") java.time.LocalDateTime since);

    // --- Batch: questoesRespondidas (distinct questions with at least 1 resposta) ---

    @Query("SELECT s.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.id IN :ids GROUP BY s.id")
    List<Object[]> countRespondidasBySubtemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.id IN :ids GROUP BY s.tema.id")
    List<Object[]> countRespondidasByTemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.disciplina.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.disciplina.id IN :ids GROUP BY s.tema.disciplina.id")
    List<Object[]> countRespondidasByDisciplinaIds(@Param("ids") List<Long> ids);

    // --- Batch: questoesAcertadas (distinct questions with at least 1 correct resposta) ---

    @Query("SELECT s.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.id IN :ids AND r.alternativaEscolhida.correta = true GROUP BY s.id")
    List<Object[]> countAcertadasBySubtemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.id IN :ids AND r.alternativaEscolhida.correta = true GROUP BY s.tema.id")
    List<Object[]> countAcertadasByTemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.disciplina.id, COUNT(DISTINCT r.questao.id) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.disciplina.id IN :ids AND r.alternativaEscolhida.correta = true GROUP BY s.tema.disciplina.id")
    List<Object[]> countAcertadasByDisciplinaIds(@Param("ids") List<Long> ids);

    // --- Batch: mediaTempoResposta ---

    @Query("SELECT s.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao.subtemas s WHERE s.id IN :ids AND r.tempoRespostaSegundos IS NOT NULL GROUP BY s.id")
    List<Object[]> avgTempoBySubtemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.id IN :ids AND r.tempoRespostaSegundos IS NOT NULL GROUP BY s.tema.id")
    List<Object[]> avgTempoByTemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.disciplina.id, AVG(r.tempoRespostaSegundos) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.disciplina.id IN :ids AND r.tempoRespostaSegundos IS NOT NULL GROUP BY s.tema.disciplina.id")
    List<Object[]> avgTempoByDisciplinaIds(@Param("ids") List<Long> ids);

    // --- Batch: ultimaQuestao (max createdAt of Resposta) ---

    @Query("SELECT s.id, MAX(r.createdAt) FROM Resposta r JOIN r.questao.subtemas s WHERE s.id IN :ids GROUP BY s.id")
    List<Object[]> findLatestResponseDatesBySubtemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.id, MAX(r.createdAt) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.id IN :ids GROUP BY s.tema.id")
    List<Object[]> findLatestResponseDatesByTemaIds(@Param("ids") List<Long> ids);

    @Query("SELECT s.tema.disciplina.id, MAX(r.createdAt) FROM Resposta r JOIN r.questao.subtemas s WHERE s.tema.disciplina.id IN :ids GROUP BY s.tema.disciplina.id")
    List<Object[]> findLatestResponseDatesByDisciplinaIds(@Param("ids") List<Long> ids);

    // --- Batch: all respostas for difficulty stats (fetch once, process in Java) ---

    @Query("SELECT DISTINCT r FROM Resposta r " +
           "JOIN FETCH r.questao q " +
           "JOIN FETCH r.alternativaEscolhida " +
           "JOIN FETCH q.alternativas " +
           "JOIN q.subtemas s " +
           "WHERE s.id IN :subtemaIds " +
           "ORDER BY r.createdAt ASC")
    List<Resposta> findAllBySubtemaIdsWithDetails(@Param("subtemaIds") List<Long> subtemaIds);

    @Query("SELECT DISTINCT r FROM Resposta r " +
           "JOIN FETCH r.questao q " +
           "JOIN FETCH r.alternativaEscolhida " +
           "JOIN FETCH q.alternativas " +
           "JOIN q.subtemas s " +
           "WHERE s.tema.id IN :temaIds " +
           "ORDER BY r.createdAt ASC")
    List<Resposta> findAllByTemaIdsWithDetails(@Param("temaIds") List<Long> temaIds);

    @Query("SELECT DISTINCT r FROM Resposta r " +
           "JOIN FETCH r.questao q " +
           "JOIN FETCH r.alternativaEscolhida " +
           "JOIN FETCH q.alternativas " +
           "JOIN q.subtemas s " +
           "WHERE s.tema.disciplina.id IN :disciplinaIds " +
           "ORDER BY r.createdAt ASC")
    List<Resposta> findAllByDisciplinaIdsWithDetails(@Param("disciplinaIds") List<Long> disciplinaIds);
}
