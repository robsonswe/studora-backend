package com.studora.repository;

import com.studora.entity.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RespostaRepository extends JpaRepository<Resposta, Long> {
    List<Resposta> findByQuestaoId(Long questaoId);
    List<Resposta> findByAlternativaEscolhidaId(Long alternativaId);
    void deleteByQuestaoId(Long questaoId);
}