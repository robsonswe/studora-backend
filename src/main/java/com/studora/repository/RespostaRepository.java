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
}
