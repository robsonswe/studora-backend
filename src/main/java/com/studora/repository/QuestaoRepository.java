package com.studora.repository;

import com.studora.entity.Questao;
import com.studora.entity.Subtema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestaoRepository extends JpaRepository<Questao, Long> {
    List<Questao> findByConcursoId(Long concursoId);
    List<Questao> findBySubtemasContaining(Subtema subtema);
    List<Questao> findBySubtemasTemaId(Long temaId);
    List<Questao> findBySubtemasTemaDisciplinaId(Long disciplinaId);
    List<Questao> findByAnuladaTrue(); // Find annulled questions

    boolean existsByConcursoId(Long concursoId);
    boolean existsBySubtemasId(Long subtemaId);
}