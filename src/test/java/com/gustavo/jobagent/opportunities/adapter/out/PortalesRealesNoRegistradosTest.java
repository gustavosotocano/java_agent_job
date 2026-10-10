package com.gustavo.jobagent.opportunities.adapter.out;

import com.gustavo.jobagent.opportunities.adapter.out.persistence.ContextosDePersistencia;
import com.gustavo.jobagent.opportunities.application.port.in.FuenteConfigurada;
import com.gustavo.jobagent.opportunities.application.port.out.BuscarOfertasPort;
import com.gustavo.jobagent.opportunities.application.port.out.FuenteConfiguradaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PortalesRealesNoRegistradosTest {

    @Test
    @DisplayName("Solo SIMULADA tiene adaptador y los portales siguen pendientes")
    void soloSimuladaEstaRegistrada() {
        try (var contexto = ContextosDePersistencia.arrancar()) {
            assertThat(contexto.getBeansOfType(BuscarOfertasPort.class).values())
                    .extracting(BuscarOfertasPort::fuenteId)
                    .containsExactly("SIMULADA");
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
