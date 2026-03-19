package com.diegoramos.konatus.pesquisaeleitoral.dto.pollDTO;

import com.diegoramos.konatus.pesquisaeleitoral.dto.candidateDTO.CandidateWeightedResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer.PollImportResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PollImportMapper {

    public PollImportResponseDTO toResponse(PollImportResult result) {
        List<CandidateWeightedResultDTO> candidates = result.weightedCandidateResults().stream()
                .map(item -> new CandidateWeightedResultDTO(item.candidateId(), item.candidateName(), item.weightedPercentage()))
                .toList();

        return new PollImportResponseDTO(
                result.pollId(),
                result.pollDate(),
                result.weightedPopulation(),
                candidates
        );
    }
}


