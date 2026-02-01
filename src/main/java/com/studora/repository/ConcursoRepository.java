package com.studora.repository;

import com.studora.entity.Concurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface ConcursoRepository extends JpaRepository<Concurso, Long> {
    boolean existsByInstituicaoId(Long instituicaoId);
    boolean existsByBancaId(Long bancaId);
    boolean existsByInstituicaoIdAndBancaIdAndAnoAndMes(Long instituicaoId, Long bancaId, Integer ano, Integer mes);

    @Query("SELECT c FROM Concurso c " +
           "JOIN FETCH c.instituicao " +
           "JOIN FETCH c.banca " +
           "WHERE c.id = :id")
    Optional<Concurso> findByIdWithDetails(@Param("id") Long id);
}