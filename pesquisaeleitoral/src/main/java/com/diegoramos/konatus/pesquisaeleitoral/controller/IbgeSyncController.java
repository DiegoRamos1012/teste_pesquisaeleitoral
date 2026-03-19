package com.diegoramos.konatus.pesquisaeleitoral.controller;

import com.diegoramos.konatus.pesquisaeleitoral.dto.IbgeSyncResultDTO;
import com.diegoramos.konatus.pesquisaeleitoral.service.poll.IbgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ibge")
@RequiredArgsConstructor
public class IbgeSyncController {

    private final IbgeService ibgeService;

    @PostMapping("/sync")
    public IbgeSyncResultDTO synchronize(@RequestParam(defaultValue = "false") boolean force) {
        return ibgeService.syncStatesAndMunicipalities(force);
    }
}

