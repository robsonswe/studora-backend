package com.studora.repository;

import com.studora.entity.Questao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestaoRepository extends JpaRepository<Questao, Long>, JpaSpecificationExecutor<Questao>, QuestaoRepositoryCustom {
    boolean existsByConcursoId(Long concursoId);
    boolean existsBySubtemasId(Long subtemaId);

    @Query("SELECT DISTINCT q FROM Questao q " +
           "LEFT JOIN FETCH q.alternativas " +
           "LEFT JOIN FETCH q.subtemas " +
           "LEFT JOIN FETCH q.questaoCargos qc " +
           "LEFT JOIN FETCH qc.concursoCargo " +
           "LEFT JOIN FETCH q.concurso")
    List<Questao> findAllWithDetails();

    @Query("SELECT DISTINCT q FROM Questao q " +
           "LEFT JOIN FETCH q.alternativas " +
           "LEFT JOIN FETCH q.subtemas " +
           "LEFT JOIN FETCH q.questaoCargos qc " +
           "LEFT JOIN FETCH qc.concursoCargo " +
           "LEFT JOIN FETCH q.concurso " +
           "WHERE q.id = :id")
    Optional<Questao> findByIdWithDetails(@Param("id") Long id);
}
