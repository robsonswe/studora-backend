package com.studora.repository;

import com.studora.entity.Banca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BancaRepository extends JpaRepository<Banca, Long> {
    Optional<Banca> findByNome(String nome);
    Optional<Banca> findByNomeIgnoreCase(String nome);
}
