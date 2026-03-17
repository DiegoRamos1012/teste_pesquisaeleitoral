package com.diegoramos.konatus.pesquisaeleitoral.repository;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PollRepository extends JpaRepository<Poll, UUID> {
}

