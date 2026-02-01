package com.studora.repository;

import com.studora.entity.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, Long> {
    Resposta findByQuestaoId(Long questaoId);
    
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
           "WHERE r.questao.id = :questaoId")
    Optional<Resposta> findByQuestaoIdWithDetails(@Param("questaoId") Long questaoId);
}