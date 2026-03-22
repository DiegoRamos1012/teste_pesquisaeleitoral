package com.diegoramos.konatus.pesquisaeleitoral.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "candidate")
public class Candidate extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String politicalParty;

    private Candidate(String name, String politicalParty) {
        this.name = requireText(name, "Nome do candidato");
        this.politicalParty = requireText(politicalParty, "Partido do candidato");
    }

    public static Candidate create(String name, String politicalParty) {
        return new Candidate(name, politicalParty);
    }
}
