package com.diegoramos.konatus.pesquisaeleitoral.repository;

import com.diegoramos.konatus.pesquisaeleitoral.domain.PollResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PollResultRepository extends JpaRepository<PollResult, UUID> {
}

