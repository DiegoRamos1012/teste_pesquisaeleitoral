package com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer;

import com.diegoramos.konatus.pesquisaeleitoral.exceptions.BusinessException;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.result.ResearchLine;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PollCsvParserTest {

    private final PollCsvParser parser = new PollCsvParser();

    @Test
    void shouldRecognizeAliasHeadersAndBrazilianDate() {
        String csv = "id_pesquisa,data_pesquisa,uf,municipality,id_candidato,intencao\n"
                + "PESQ-TESTE,10/03/2026,SP,Campinas,11111111-1111-1111-1111-111111111111,42.5\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "pesquisa.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        List<ResearchLine> lines = parser.parse(file);

        assertThat(lines).hasSize(1);
        ResearchLine line = lines.getFirst();
        assertThat(line.pollId()).isEqualTo("PESQ-TESTE");
        assertThat(line.pollDate()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(line.state()).isEqualTo("SP");
        assertThat(line.municipality()).isEqualTo("Campinas");
        assertThat(line.candidateId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(line.percentual()).hasToString("42.5");
    }

    @Test
    void shouldFailWhenPercentualContainsLiteralFromBrokenGeneration() {
        String csv = "poll_id,poll_date,estado,municipio,candidate_id,percentual\n"
                + "PESQ-TESTE,2026-03-10,SP,Sao Paulo,11111111-1111-1111-1111-111111111111,30.Replace(',', '.')\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "pesquisa.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> parser.parse(file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("percentual numerico esperado");
    }
}


