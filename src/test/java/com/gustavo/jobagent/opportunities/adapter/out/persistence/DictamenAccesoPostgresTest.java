package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.application.port.in.ConsultarDictamenAcceso;
import com.gustavo.jobagent.opportunities.application.port.in.FuenteConfigurada;
import com.gustavo.jobagent.opportunities.application.port.out.FuenteConfiguradaRepository;
import com.gustavo.jobagent.opportunities.domain.DictamenAcceso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DictamenAccesoPostgresTest {

    @Test
    @DisplayName("Los tres dictámenes y el estado de las fuentes se leen igual tras reiniciar")
    void dictamenesSobrevivenAlReinicio() {
        assertDictamenesYFuentes(ContextosDePersistencia.arrancar());
        assertDictamenesYFuentes(ContextosDePersistencia.arrancar());
    }

    private static void assertDictamenesYFuentes(ConfigurableApplicationContext contexto) {
        try (contexto) {
            List<DictamenAcceso> dictamenes = contexto.getBean(ConsultarDictamenAcceso.class).listar();
            assertThat(dictamenes).extracting(DictamenAcceso::fuenteId)
                    .containsExactly("LINKEDIN", "INDEED", "GLASSDOOR");
            assertThat(dictamenes).allSatisfy(dictamen -> {
                assertThat(dictamen.viaComprobada()).isFalse();
                assertThat(dictamen.motivo()).isNotBlank();
                assertThat(dictamen.claseAcceso()).isNull();
                assertThat(dictamen.cobertura()).isNull();
                assertThat(dictamen.limites()).isNull();
                assertThat(dictamen.actualidad()).isNull();
                assertThat(dictamen.verificadoEn().atZone(ZoneOffset.UTC).toLocalDate())
                        .isEqualTo(LocalDate.of(2026, 10, 10));
            });
            assertThat(contexto.getBean(ConsultarDictamenAcceso.class).porFuente("SIMULADA")).isEmpty();

            List<FuenteConfigurada> fuentes = contexto.getBean(FuenteConfiguradaRepository.class).listar();
            assertThat(fuentes).anySatisfy(fuente -> {
                assertThat(fuente.fuenteId()).isEqualTo("SIMULADA");
                assertThat(fuente.estado()).isEqualTo(FuenteConfigurada.EstadoFuente.DISPONIBLE);
                assertThat(fuente.habilitada()).isTrue();
            });
            assertThat(fuentes).filteredOn(fuente -> List.of("LINKEDIN", "INDEED", "GLASSDOOR").contains(fuente.fuenteId()))
                    .allSatisfy(fuente -> {
                        assertThat(fuente.estado()).isEqualTo(FuenteConfigurada.EstadoFuente.PENDIENTE);
                        assertThat(fuente.habilitada()).isFalse();
                    });
        }
    }
}
