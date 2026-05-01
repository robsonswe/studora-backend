package com.studora.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.studora.entity.Concurso;

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
           "LEFT JOIN FETCH c.provas p " +
           "LEFT JOIN FETCH p.cargos pc " +
           "LEFT JOIN FETCH pc.cargo " +
           "LEFT JOIN FETCH p.secoes s " +
           "LEFT JOIN FETCH s.subtemas st " +
           "LEFT JOIN FETCH st.tema t " +
           "LEFT JOIN FETCH t.disciplina " +
           "WHERE c.id = :id")
    Optional<Concurso> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Concurso c " +
           "JOIN FETCH c.instituicao " +
           "JOIN FETCH c.banca " +
           "LEFT JOIN FETCH c.concursoCargos cc " +
           "LEFT JOIN FETCH cc.cargo " +
           "LEFT JOIN FETCH c.provas p " +
           "LEFT JOIN FETCH p.cargos pc " +
           "LEFT JOIN FETCH pc.cargo " +
           "LEFT JOIN FETCH p.secoes s " +
           "LEFT JOIN FETCH s.subtemas st " +
           "LEFT JOIN FETCH st.tema t " +
           "LEFT JOIN FETCH t.disciplina " +
           "WHERE c.id IN :ids")
    java.util.List<Concurso> findAllByIdsWithDetails(@Param("ids") java.util.List<Long> ids);
}
