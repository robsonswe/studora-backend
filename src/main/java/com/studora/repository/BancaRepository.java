package com.studora.repository;

import com.studora.entity.Banca;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BancaRepository extends JpaRepository<Banca, Long> {
    Optional<Banca> findByNome(String nome);
    Optional<Banca> findByNomeIgnoreCase(String nome);
}
