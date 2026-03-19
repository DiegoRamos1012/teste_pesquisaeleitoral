package com.diegoramos.konatus.pesquisaeleitoral.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "state")
public class State extends BaseEntity {

    private String stateAcronym;

    /**
     * Construtor privado para uso interno e factory method.
     */
    private State(String name, String stateAcronym) {
        this.name = name;
        this.stateAcronym = stateAcronym;
    }

    /**
     * Cria uma nova instância de State.
     *
     * @param name        Nome do produto
     * @param stateAcronym  Sigla do estado
     * @return Produto criado
     */
    public static State create(String name, String stateAcronym) {
        return new State(name, stateAcronym);
    }

    public void updateName(String name) {
        this.name = name;
    }

}
