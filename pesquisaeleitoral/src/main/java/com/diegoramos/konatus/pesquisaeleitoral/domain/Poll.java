package com.diegoramos.konatus.pesquisaeleitoral.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "poll")
public class Poll extends BaseEntity {

    private LocalDate pollDate;

    private Poll(String name, LocalDate pollDate) {
        this.name = name;
        this.pollDate = pollDate;
    }

    public static Poll create(String name, LocalDate pollDate) {
        return new Poll(name, pollDate);
    }
}

