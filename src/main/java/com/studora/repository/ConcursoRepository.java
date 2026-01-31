package com.studora.repository;

import com.studora.entity.Concurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcursoRepository extends JpaRepository<Concurso, Long> {
    boolean existsByInstituicaoId(Long instituicaoId);
    boolean existsByBancaId(Long bancaId);
    boolean existsByInstituicaoIdAndBancaIdAndAnoAndMes(Long instituicaoId, Long bancaId, Integer ano, Integer mes);
}