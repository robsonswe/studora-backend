package com.studora.repository;

import com.studora.entity.Concurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface ConcursoRepository extends JpaRepository<Concurso, Long>, JpaSpecificationExecutor<Concurso> {
    boolean existsByInstituicaoId(Long instituicaoId);
    boolean existsByBancaId(Long bancaId);
    boolean existsByInstituicaoIdAndBancaIdAndAnoAndMes(Long instituicaoId, Long bancaId, Integer ano, Integer mes);

    @Query("SELECT c FROM Concurso c " +
           "JOIN FETCH c.instituicao " +
           "JOIN FETCH c.banca " +
           "LEFT JOIN FETCH c.concursoCargos cc " +
           "LEFT JOIN FETCH cc.cargo " +
           "WHERE c.id = :id")
    Optional<Concurso> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Concurso c " +
           "JOIN FETCH c.instituicao " +
           "JOIN FETCH c.banca " +
           "LEFT JOIN FETCH c.concursoCargos cc " +
           "LEFT JOIN FETCH cc.cargo " +
           "WHERE c.id IN :ids")
    java.util.List<Concurso> findAllByIdsWithDetails(@Param("ids") java.util.List<Long> ids);
}
