package com.studora.repository;

import com.studora.entity.Simulado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SimuladoRepository extends JpaRepository<Simulado, Long> {

    @Query("SELECT s FROM Simulado s LEFT JOIN FETCH s.questoes q LEFT JOIN FETCH q.respostas WHERE s.id = :id")
    Optional<Simulado> findByIdWithQuestoes(@Param("id") Long id);
}
