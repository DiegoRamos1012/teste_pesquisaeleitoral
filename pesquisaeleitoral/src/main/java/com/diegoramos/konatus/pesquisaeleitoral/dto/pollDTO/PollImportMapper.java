package com.diegoramos.konatus.pesquisaeleitoral.dto.pollDTO;

import com.diegoramos.konatus.pesquisaeleitoral.dto.candidateDTO.CandidateWeightedResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.candidateDTO.GroupCandidateWeightedResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.PollImportResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PollImportMapper {

    public PollImportResponseDTO toResponse(PollImportResult result) {
        List<CandidateWeightedResultDTO> candidates = result.weightedCandidateResults().stream()
                .map(item -> new CandidateWeightedResultDTO(item.candidateId(), item.candidateName(), item.weightedPercentage()))
                .toList();

        List<PollGroupBreakdownDTO> groupBreakdown = result.groupBreakdown().stream()
                .map(group -> new PollGroupBreakdownDTO(
                        group.stateAcronym(),
                        group.municipalityGroup().name(),
                        group.groupPopulation(),
                        group.sampledPopulation(),
                        group.candidates().stream()
                                .map(item -> new GroupCandidateWeightedResultDTO(
                                        item.candidateId(),
                                        item.candidateName(),
                                        item.weightedPercentage()
                                ))
                                .toList()
                ))
                .toList();

        return new PollImportResponseDTO(
                result.pollId(),
                result.pollDate(),
                result.weightedPopulation(),
                candidates,
                groupBreakdown
        );
    }
}


