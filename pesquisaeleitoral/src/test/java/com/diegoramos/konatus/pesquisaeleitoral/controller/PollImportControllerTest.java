package com.diegoramos.konatus.pesquisaeleitoral.controller;

import com.diegoramos.konatus.pesquisaeleitoral.dto.candidateDTO.CandidateWeightedResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.candidateDTO.GroupCandidateWeightedResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.pollDTO.PollGroupBreakdownDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.pollDTO.PollImportMapper;
import com.diegoramos.konatus.pesquisaeleitoral.dto.pollDTO.PollImportResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer.PollImportService;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.CandidateWeightedResult;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.GroupCandidateWeightedResult;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.PollGroupBreakdown;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.PollImportResult;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.weighting.MunicipalitySizeGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PollImportController.class)
class PollImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PollImportService pollImportService;

    @MockitoBean
    private PollImportMapper pollImportMapper;

    @Test
    void shouldImportPollCsvAndReturnWeightedCandidates() throws Exception {
        UUID candidateId = UUID.randomUUID();
        PollImportResult result = new PollImportResult(
                "PESQ-2026-01",
                LocalDate.of(2026, 3, 1),
                1000000L,
                List.of(new CandidateWeightedResult(candidateId, "Maria Silva", new BigDecimal("42.35"))),
                List.of(new PollGroupBreakdown(
                        "SP",
                        MunicipalitySizeGroup.GROUP_4,
                        1_000_000L,
                        500_000L,
                        List.of(new GroupCandidateWeightedResult(candidateId, "Maria Silva", new BigDecimal("42.35")))
                ))
        );
        PollImportResponseDTO response = new PollImportResponseDTO(
                "PESQ-2026-01",
                LocalDate.of(2026, 3, 1),
                1000000L,
                List.of(new CandidateWeightedResultDTO(
                        candidateId,
                        "Maria Silva",
                        new BigDecimal("42.35")
                )),
                List.of(new PollGroupBreakdownDTO(
                        "SP",
                        "GROUP_4",
                        1_000_000L,
                        500_000L,
                        List.of(new GroupCandidateWeightedResultDTO(
                                candidateId,
                                "Maria Silva",
                                new BigDecimal("42.35")
                        ))
                ))
        );

        given(pollImportService.researchImport(any())).willReturn(result);
        given(pollImportMapper.toResponse(result)).willReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "pesquisa.csv",
                "text/csv",
                "poll_id,poll_date,estado,municipio,candidate_id,percentual".getBytes()
        );

        mockMvc.perform(multipart("/api/polls/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pollId").value("PESQ-2026-01"))
                .andExpect(jsonPath("$.weightedPopulation").value(1000000L))
                .andExpect(jsonPath("$.candidates[0].candidateId").value(candidateId.toString()))
                .andExpect(jsonPath("$.candidates[0].candidateName").value("Maria Silva"))
                .andExpect(jsonPath("$.candidates[0].weightedPercentage").value(42.35))
                .andExpect(jsonPath("$.groupBreakdown[0].stateAcronym").value("SP"))
                .andExpect(jsonPath("$.groupBreakdown[0].municipalityGroup").value("GROUP_4"));
    }
}

