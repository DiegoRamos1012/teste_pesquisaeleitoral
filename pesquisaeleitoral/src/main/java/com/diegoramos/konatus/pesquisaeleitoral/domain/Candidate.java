package com.diegoramos.konatus.pesquisaeleitoral.domain;

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

    private String politicalParty;

    private Candidate(String name, String politicalParty) {
        this.name = name;
        this.politicalParty = politicalParty;
    }

    public static Candidate create(String name, String politicalParty) {
        return new Candidate(name, politicalParty);
    }
}
