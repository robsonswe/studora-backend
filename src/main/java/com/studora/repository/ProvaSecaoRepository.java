package com.studora.repository;

import com.studora.entity.ProvaSecao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvaSecaoRepository extends JpaRepository<ProvaSecao, Long> {
}
