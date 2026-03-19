package com.diegoramos.konatus.pesquisaeleitoral.controller;

import com.diegoramos.konatus.pesquisaeleitoral.dto.ApiErrorResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.pollDTO.PollImportMapper;
import com.diegoramos.konatus.pesquisaeleitoral.dto.pollDTO.PollImportResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer.PollImportResult;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.importer.PollImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
@Tag(name = "Pesquisa", description = "Importacao de arquivos de pesquisa e calculo de intencao de voto ponderada")
public class PollImportController {

    private final PollImportService pollImportService;
    private final PollImportMapper pollImportMapper;

    @Operation(
            summary = "Importar arquivo CSV de pesquisa",
            description = "Processa um CSV com resultados por municipio e candidato, persiste os dados e retorna o ranking ponderado por populacao"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pesquisa importada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PollImportResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validacao ou regra de negocio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    })
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PollImportResponseDTO importPoll(
            @Parameter(
                    description = "Arquivo CSV da pesquisa eleitoral",
                    required = true,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            )
            @RequestPart("file") MultipartFile file
    ) {
        PollImportResult result = pollImportService.researchImport(file);
        return pollImportMapper.toResponse(result);
    }
}

