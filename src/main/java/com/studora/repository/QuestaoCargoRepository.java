package com.studora.repository;

import com.studora.entity.QuestaoCargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestaoCargoRepository extends JpaRepository<QuestaoCargo, Long> {

    List<QuestaoCargo> findByQuestaoId(Long questaoId);

    @Query("SELECT qc FROM QuestaoCargo qc WHERE qc.questao.id = :questaoId AND qc.concursoCargo.id = :concursoCargoId")
    List<QuestaoCargo> findByQuestaoIdAndConcursoCargoId(@Param("questaoId") Long questaoId, @Param("concursoCargoId") Long concursoCargoId);

    List<QuestaoCargo> findByConcursoCargoId(Long concursoCargoId);
}
