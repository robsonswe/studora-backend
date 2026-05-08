package com.studora.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studora.entity.SecaoDisciplina;

@Repository
public interface SecaoDisciplinaRepository extends JpaRepository<SecaoDisciplina, Long> {
    List<SecaoDisciplina> findAllBySecaoCargoId(Long secaoCargoId);
    Optional<SecaoDisciplina> findBySecaoCargoIdAndNomeIgnoreCase(Long secaoCargoId, String nome);
}
