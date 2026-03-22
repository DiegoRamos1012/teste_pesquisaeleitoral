package com.diegoramos.konatus.pesquisaeleitoral.domain;

import com.diegoramos.konatus.pesquisaeleitoral.exceptions.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "municipality")
public class Municipality extends BaseEntity {

    @Column(nullable = false)
    private int population;

    // Melhora performance impedindo que carregue todos os estados junto com cada município
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    private Municipality(String name, int population, State state) {
        this.name = requireText(name, "Nome do municipio");
        if (population < 0) {
            throw new BusinessException("População nao pode ser negativa");
        }
        this.population = population;
        if (state == null) {
            throw new BusinessException("Estado do municipio nao pode ser nulo");
        }
        this.state = state;
    }

    public static Municipality create(String name, int population, State state) {
        return new Municipality(name, population, state);
    }

    public boolean syncFromIbge(String municipalityName, int population) {
        String normalizedName = requireText(municipalityName, "Nome do municipio");
        if (population < 0) {
            throw new BusinessException("População nao pode ser negativa");
        }

        boolean changed = false;
        if (!this.name.equals(normalizedName)) {
            this.name = normalizedName;
            changed = true;
        }
        if (this.population != population) {
            this.population = population;
            changed = true;
        }
        return changed;
    }
}
