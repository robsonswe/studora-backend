package com.studora.repository;

import com.studora.entity.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlternativaRepository extends JpaRepository<Alternativa, Long> {
    List<Alternativa> findByQuestaoIdOrderByOrdemAsc(Long questaoId);
    List<Alternativa> findByQuestaoIdAndCorretaTrue(Long questaoId);
}