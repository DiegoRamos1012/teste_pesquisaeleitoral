package com.diegoramos.konatus.pesquisaeleitoral.service.poll.weighting;

public enum MunicipalitySizeGroup {
    GROUP_1,
    GROUP_2,
    GROUP_3,
    GROUP_4;

    public static MunicipalitySizeGroup fromPopulation(int population) {
        if (population <= 20_000) {
            return GROUP_1;
        }
        if (population <= 100_000) {
            return GROUP_2;
        }
        if (population <= 1_000_000) {
            return GROUP_3;
        }
        return GROUP_4;
    }
}

