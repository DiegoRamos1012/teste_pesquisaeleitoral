package com.diegoramos.konatus.pesquisaeleitoral.repository;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MunicipalityRepository extends JpaRepository<Municipality, UUID> {
}
