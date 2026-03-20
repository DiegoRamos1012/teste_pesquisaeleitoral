package com.diegoramos.konatus.pesquisaeleitoral.repository;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface MunicipalityRepository extends JpaRepository<Municipality, UUID> {

	Optional<Municipality> findByNameIgnoreCaseAndState(String name, State state);

	List<Municipality> findAllByState(State state);
}
