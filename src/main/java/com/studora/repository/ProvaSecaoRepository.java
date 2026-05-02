package com.studora.repository;

import com.studora.entity.ProvaSecao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProvaSecaoRepository extends JpaRepository<ProvaSecao, Long> {
    @Query("SELECT DISTINCT ps FROM ProvaSecao ps " +
           "LEFT JOIN FETCH ps.questoes " +
           "WHERE ps.prova.id = :provaId " +
           "ORDER BY ps.ordem ASC")
    List<ProvaSecao> findByProvaIdWithQuestoes(@Param("provaId") Long provaId);
}
