package com.diegoramos.konatus.pesquisaeleitoral.repository;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MunicipalityRepository extends JpaRepository<Municipality, UUID> {
	boolean existsByNameIgnoreCaseAndState(String name, State state);

	Optional<Municipality> findByNameIgnoreCaseAndState(String name, State state);
}
