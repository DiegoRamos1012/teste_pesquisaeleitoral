package com.diegoramos.konatus.pesquisaeleitoral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PesquisaeleitoralApplication {

    static void main(String[] args) {
        SpringApplication.run(PesquisaeleitoralApplication.class, args);
    }

}
