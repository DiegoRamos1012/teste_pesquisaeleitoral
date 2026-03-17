package com.diegoramos.konatus.pesquisaeleitoral.repository;

import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StateRepository extends JpaRepository<State, UUID> {
}
