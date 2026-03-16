package com.diegoramos.konatus.pesquisaeleitoral.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "municipiality")
public class Municipality extends BaseEntity {
    private int population;

    // Melhora performance impedindo que carregue todos os estados junto com cada município
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    private Municipality(String name, int population, State state) {
        this.name = name;
        this.population = population;
        this.state = state;
    }

    public static Municipality create(String name, int population, State state) {
        return new Municipality(name, population, state);
    }

}
