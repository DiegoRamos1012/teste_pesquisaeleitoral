package com.diegoramos.konatus.pesquisaeleitoral.repository;

import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StateRepository extends JpaRepository<State, UUID> {
	Optional<State> findByStateAcronymIgnoreCase(String stateAcronym);

	Optional<State> findByNameIgnoreCase(String name);
}
