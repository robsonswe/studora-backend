package com.studora.repository;

import com.studora.entity.Subtema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtemaRepository extends JpaRepository<Subtema, Long> {
    List<Subtema> findByTemaId(Long temaId);
}