package com.diegoramos.konatus.pesquisaeleitoral.service.ibge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IbgeSyncScheduler {

    private final IbgeService ibgeService;

    @Scheduled(cron = "${ibge.sync.cron:0 0 3 1 * *}")
    public void synchronizeMonthly() {
        long startedAt = System.nanoTime();
        log.info("Iniciando sincronizacao mensal da base IBGE");
        try {
            ibgeService.syncStatesAndMunicipalities(false);
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
            log.info("Sincronizacao mensal IBGE finalizada em {} ms", elapsedMillis);
        } catch (Exception ex) {
            log.error("Falha na sincronizacao mensal da base do IBGE", ex);
        }
    }
}


