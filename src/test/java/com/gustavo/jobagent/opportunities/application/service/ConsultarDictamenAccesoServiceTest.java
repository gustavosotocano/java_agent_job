package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.out.DictamenAccesoRepository;
import com.gustavo.jobagent.opportunities.domain.DictamenAcceso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultarDictamenAccesoServiceTest {

    @Test
    @DisplayName("Lista los tres portales en orden y no devuelve la fuente simulada")
    void listaPortalesYOmiteSimulada() {
        Instant fecha = Instant.parse("2026-10-10T00:00:00Z");
        List<DictamenAcceso> guardados = new ArrayList<>(List.of(
                DictamenAcceso.noComprobada("GLASSDOOR", "sin API", fecha),
                DictamenAcceso.noComprobada("LINKEDIN", "sin API", fecha),
                DictamenAcceso.noComprobada("INDEED", "sin API", fecha)
        ));
        DictamenAccesoRepository repositorio = new DictamenAccesoRepository() {
            @Override
            public List<DictamenAcceso> listar() {
                return guardados;
            }

            @Override
            public Optional<DictamenAcceso> porFuente(String fuenteId) {
                return guardados.stream().filter(dictamen -> dictamen.fuenteId().equals(fuenteId)).findFirst();
            }
        };
        ConsultarDictamenAccesoService servicio = new ConsultarDictamenAccesoService(repositorio);

        assertThat(servicio.listar()).extracting(DictamenAcceso::fuenteId)
                .containsExactly("LINKEDIN", "INDEED", "GLASSDOOR");
        assertThat(servicio.porFuente("SIMULADA")).isEmpty();
        assertThat(guardados).hasSize(3);
    }
}
