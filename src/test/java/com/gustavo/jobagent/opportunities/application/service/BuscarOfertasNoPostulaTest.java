package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.out.BuscarOfertasPort;
import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BuscarOfertasNoPostulaTest {

    @Test
    @DisplayName("Buscar no postula, no inicia sesión y no fabrica ofertas de portales reales")
    void buscarNoPostulaNiFabricaPortales() {
        assertThat(BuscarOfertasService.class.getDeclaredMethods())
                .extracting(Method::getName)
                .noneMatch(nombre -> {
                    String normalizado = nombre.toLowerCase();
                    return normalizado.contains("postul") || normalizado.contains("login") || normalizado.contains("aplicar");
                });

        BuscarOfertasPort simulada = new BuscarOfertasPort() {
            @Override
            public String fuenteId() {
                return "SIMULADA";
            }

            @Override
            public boolean estaHabilitada() {
                return true;
            }

            @Override
            public List<OfertaLaboral> buscar(CriteriosBusqueda criterios) {
                return List.of(OfertaLaboral.builder()
                        .idFuente("sim-001")
                        .fuenteId("SIMULADA")
                        .titulo("Backend")
                        .build());
            }
        };

        assertThat(new BuscarOfertasService(List.of(simulada)).buscar(new CriteriosBusqueda("Backend")).ofertas())
                .extracting(OfertaLaboral::fuenteId)
                .doesNotContain("LINKEDIN", "INDEED", "GLASSDOOR");
    }
}
