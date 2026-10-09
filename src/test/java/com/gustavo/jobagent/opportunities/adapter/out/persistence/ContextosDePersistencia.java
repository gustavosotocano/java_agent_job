package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.JobAgentApplication;
import com.gustavo.jobagent.TestcontainersConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public final class ContextosDePersistencia {

    private ContextosDePersistencia() {
    }

    public static ConfigurableApplicationContext arrancar() {
        return new SpringApplicationBuilder(JobAgentApplication.class)
                .run(
                        "--spring.datasource.url=" + TestcontainersConfiguration.jdbcUrl(),
                        "--spring.datasource.username=" + TestcontainersConfiguration.username(),
                        "--spring.datasource.password=" + TestcontainersConfiguration.password()
                );
    }
}
