package com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer;

import com.diegoramos.konatus.pesquisaeleitoral.domain.Candidate;
import com.diegoramos.konatus.pesquisaeleitoral.domain.Municipality;
import com.diegoramos.konatus.pesquisaeleitoral.domain.State;
import com.diegoramos.konatus.pesquisaeleitoral.repository.CandidateRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.MunicipalityRepository;
import com.diegoramos.konatus.pesquisaeleitoral.repository.StateRepository;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.CandidateWeightedResult;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.PollImportResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pollimport;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Transactional
class PollImportServiceIntegrationTest {

    @Autowired
    private PollImportService pollImportService;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private MunicipalityRepository municipalityRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Test
    void shouldImportCsvAndCalculateWeightedResults() {
        State state = stateRepository.save(State.create("Sao Paulo", "SP"));
        municipalityRepository.save(Municipality.create("Campinas", 1_200_000, state));
        municipalityRepository.save(Municipality.create("Santos", 800_000, state));

        Candidate maria = candidateRepository.save(Candidate.create("Maria Silva", "Partido A"));
        Candidate joao = candidateRepository.save(Candidate.create("Joao Souza", "Partido B"));

        String csv = """
                poll_id,poll_date,estado,municipio,candidate_id,percentual
                PESQ-TESTE-2026,2026-03-10,SP,Campinas,%s,40
                PESQ-TESTE-2026,2026-03-10,SP,Campinas,%s,35
                PESQ-TESTE-2026,2026-03-10,SP,Santos,%s,30
                PESQ-TESTE-2026,2026-03-10,SP,Santos,%s,45
                """.formatted(maria.getId(), joao.getId(), maria.getId(), joao.getId());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "pesquisa.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        PollImportResult result = pollImportService.researchImport(file);

        assertThat(result.pollId()).isEqualTo("PESQ-TESTE-2026");
        assertThat(result.pollDate()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(result.weightedPopulation()).isEqualTo(2_000_000L);
        assertThat(result.groupBreakdown()).hasSize(2);
        assertThat(result.groupBreakdown().getFirst().stateAcronym()).isEqualTo("SP");

        List<CandidateWeightedResult> candidates = result.weightedCandidateResults();
        assertThat(candidates).hasSize(2);

        assertThat(candidates.getFirst().candidateId()).isEqualTo(joao.getId());
        assertThat(candidates.getFirst().candidateName()).isEqualTo("Joao Souza");
        assertThat(candidates.getFirst().weightedPercentage()).isEqualByComparingTo(new BigDecimal("39.00"));

        assertThat(candidates.get(1).candidateId()).isEqualTo(maria.getId());
        assertThat(candidates.get(1).candidateName()).isEqualTo("Maria Silva");
        assertThat(candidates.get(1).weightedPercentage()).isEqualByComparingTo(new BigDecimal("36.00"));
    }
}
