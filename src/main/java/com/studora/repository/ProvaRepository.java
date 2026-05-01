package com.studora.repository;

import com.studora.entity.Prova;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProvaRepository extends JpaRepository<Prova, Long> {
    List<Prova> findByConcursoId(Long concursoId);

    @Query("SELECT p FROM Prova p LEFT JOIN FETCH p.secoes s LEFT JOIN FETCH s.subtemas LEFT JOIN FETCH s.pesos LEFT JOIN FETCH p.cargos c LEFT JOIN FETCH c.cargo WHERE p.id = :id")
    Optional<Prova> findByIdWithDetails(@Param("id") Long id);

    boolean existsByConcursoIdAndNomeIgnoreCase(Long concursoId, String nome);
}
