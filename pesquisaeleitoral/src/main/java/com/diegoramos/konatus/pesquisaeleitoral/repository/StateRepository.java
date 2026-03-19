package com.diegoramos.konatus.pesquisaeleitoral.repository;

import com.diegoramos.konatus.pesquisaeleitoral.domain.State;

import java.util.Optional;

public interface StateRepository extends NamedEntityRepository<State> {
	Optional<State> findByStateAcronymIgnoreCase(String stateAcronym);
}
