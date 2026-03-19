package com.diegoramos.konatus.pesquisaeleitoral.service.poll;

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
		try {
			ibgeService.syncStatesAndMunicipalities(false);
		} catch (Exception ex) {
			log.error("Falha na sincronizacao mensal da base do IBGE", ex);
		}
	}
}

