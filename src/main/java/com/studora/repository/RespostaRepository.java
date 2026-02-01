package com.studora.repository;

import com.studora.entity.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, Long> {
    Resposta findByQuestaoId(Long questaoId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM Resposta r WHERE r.questao.id = :questaoId")
    void deleteByQuestaoId(@Param("questaoId") Long questaoId);
}