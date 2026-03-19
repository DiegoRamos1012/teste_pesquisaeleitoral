package com.diegoramos.konatus.pesquisaeleitoral.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Locale;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "state")
public class State extends BaseEntity {

    @Column(nullable = false, length = 2)
    private String stateAcronym;

    private State(String name, String stateAcronym) {
        this.name = requireText(name, "Nome do estado");
        this.stateAcronym = requireText(stateAcronym, "Sigla do estado").toUpperCase(Locale.ROOT);
    }

    public static State create(String name, String stateAcronym) {
        return new State(name, stateAcronym);
    }

    public void updateName(String name) {
        this.name = requireText(name, "Nome do estado");
    }

}
