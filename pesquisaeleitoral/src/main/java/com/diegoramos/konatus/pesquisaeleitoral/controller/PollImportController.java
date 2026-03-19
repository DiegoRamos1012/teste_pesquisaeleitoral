package com.diegoramos.konatus.pesquisaeleitoral.controller;

import com.diegoramos.konatus.pesquisaeleitoral.dto.CandidateWeightedResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.PollImportResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.PollImportResult;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.PollImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
public class PollImportController {

    private final PollImportService pollImportService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PollImportResponseDTO importPoll(@RequestPart("file") MultipartFile file) {
        PollImportResult result = pollImportService.researchImport(file);

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

