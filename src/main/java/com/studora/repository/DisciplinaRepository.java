package com.studora.repository;

import com.studora.entity.Disciplina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {
    Optional<Disciplina> findByNome(String nome);
    Optional<Disciplina> findByNomeIgnoreCase(String nome);
    Optional<Disciplina> findByNomeIgnoreCaseAndIdNot(String nome, Long id);
    Page<Disciplina> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    Page<Disciplina> findByNomeNormalizedContaining(String nomeNormalized, Pageable pageable);
}
