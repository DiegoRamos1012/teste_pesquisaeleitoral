package com.diegoramos.konatus.pesquisaeleitoral.repository;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Poll;

import java.time.LocalDate;
import java.util.Optional;

public interface PollRepository extends NamedEntityRepository<Poll> {
    Optional<Poll> findByNameAndPollDate(String name, LocalDate pollDate);
}

