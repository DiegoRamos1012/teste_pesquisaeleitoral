package com.diegoramos.konatus.pesquisaeleitoral.repository;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CandidateRepository extends JpaRepository<Candidate, UUID> {
}

