package com.studora.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studora.entity.SecaoCargo;

@Repository
public interface SecaoCargoRepository extends JpaRepository<SecaoCargo, Long> {
    Optional<SecaoCargo> findByConcursoCargoIdAndNomeIgnoreCase(Long concursoCargoId, String nome);
    List<SecaoCargo> findAllByConcursoCargoId(Long concursoCargoId);
}
