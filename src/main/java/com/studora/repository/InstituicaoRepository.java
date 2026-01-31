package com.studora.repository;

import com.studora.entity.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {
    Optional<Instituicao> findByNome(String nome);
    Optional<Instituicao> findByNomeIgnoreCase(String nome);
}
