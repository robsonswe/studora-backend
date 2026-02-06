package com.studora.repository;

import com.studora.entity.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlternativaRepository extends JpaRepository<Alternativa, Long> {
    List<Alternativa> findByQuestaoIdOrderByOrdemAsc(Long questaoId);
    List<Alternativa> findByQuestaoIdAndCorretaTrue(Long questaoId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM Alternativa a WHERE a.questao.id = :questaoId")
    void deleteByQuestaoId(@org.springframework.data.repository.query.Param("questaoId") Long questaoId);
}
