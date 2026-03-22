package com.diegoramos.konatus.pesquisaeleitoral.domain;

import com.diegoramos.konatus.pesquisaeleitoral.exceptions.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "poll_result")
public class PollResult extends BaseEntity {

    private static final BigDecimal MAX_PERCENTAGE = new BigDecimal("100");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    private Municipality municipality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    private PollResult(Poll poll, Municipality municipality, Candidate candidate, BigDecimal percentage) {
        if (poll == null || municipality == null || candidate == null) {
            throw new BusinessException("Pesquisa, municipio e candidato sao obrigatorios");
        }
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(MAX_PERCENTAGE) > 0) {
            throw new BusinessException("A porcentagem deve estar entre 0 a 100");
        }
        this.name = poll.getName() + "-" + municipality.getName() + "-" + candidate.getName();
        this.poll = poll;
        this.municipality = municipality;
        this.candidate = candidate;
        this.percentage = percentage;
    }

    public static PollResult create(
            Poll poll,
            Municipality municipality,
            Candidate candidate,
            BigDecimal percentage
    ) {
        return new PollResult(poll, municipality, candidate, percentage);
    }


}
