package com.diegoramos.konatus.pesquisaeleitoral.domain;

import com.diegoramos.konatus.pesquisaeleitoral.exceptions.BusinessException;
import jakarta.persistence.Column;
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

    @Column(nullable = false)
    private LocalDate pollDate;

    private Poll(String name, LocalDate pollDate) {
        this.name = requireText(name, "ID da pesquisa");
        if (pollDate == null) {
            throw new BusinessException("Data da pesquisa nao pode ser nula");
        }
        this.pollDate = pollDate;
    }

    public static Poll create(String name, LocalDate pollDate) {
        return new Poll(name, pollDate);
    }
}

