package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.JobAgentApplication;
import com.gustavo.jobagent.TestcontainersConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;

public final class ContextosDePersistencia {

    private ContextosDePersistencia() {
    }

    public static ConfigurableApplicationContext arrancar(String... extra) {
        List<String> argumentos = new ArrayList<>();
        argumentos.add("--spring.datasource.url=" + TestcontainersConfiguration.jdbcUrl());
        argumentos.add("--spring.datasource.username=" + TestcontainersConfiguration.username());
        argumentos.add("--spring.datasource.password=" + TestcontainersConfiguration.password());
        if (extra != null) {
            for (String argumento : extra) {
                if (argumento != null && !argumento.isBlank()) {
                    argumentos.add(argumento);
                }
            }
        }
        return new SpringApplicationBuilder(JobAgentApplication.class)
                .run(argumentos.toArray(String[]::new));
    }
}
