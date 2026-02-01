package com.studora.repository;

import com.studora.entity.Questao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestaoRepository extends JpaRepository<Questao, Long>, JpaSpecificationExecutor<Questao> {
    boolean existsByConcursoId(Long concursoId);
    boolean existsBySubtemasId(Long subtemaId);
}