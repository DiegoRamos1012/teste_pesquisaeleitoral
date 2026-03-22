package com.diegoramos.konatus.pesquisaeleitoral.controller;

import com.diegoramos.konatus.pesquisaeleitoral.dto.ApiErrorResponseDTO;
import com.diegoramos.konatus.pesquisaeleitoral.dto.ibgeDTO.IbgeSyncResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.service.ibge.IbgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ibge")
@RequiredArgsConstructor
@Tag(name = "IBGE", description = "Sincronizacao da base local de estados e municipios")
public class IbgeSyncController {

    private final IbgeService ibgeService;

    @Operation(
            summary = "Sincronizar base IBGE",
            description = "Atualiza estados e municipios na base local utilizando APIs do IBGE e SIDRA"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sincronizacao concluida",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = IbgeSyncResultDTO.class))),
            @ApiResponse(responseCode = "500", description = "Falha na consulta externa ou erro interno",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponseDTO.class)))
    })
    @PostMapping("/sync")
    public IbgeSyncResultDTO synchronize(
            @Parameter(description = "Forca sincronizacao manual", example = "true")
            @RequestParam(defaultValue = "false") boolean force
    ) {
        return ibgeService.syncStatesAndMunicipalities(force);
    }
}

