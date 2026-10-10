package com.gustavo.jobagent.opportunities.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluacionOfertaPropuestaTest {

    @Test
    @DisplayName("evaluar deja la propuesta vacía y conObservaciones no cambia el cálculo")
    void calcularNoDependeDeLaPropuesta() {
        OfertaLaboral oferta = oferta();
        PerfilProfesional perfil = perfil();

        EvaluacionOferta calculada = EvaluacionOferta.evaluar("eval-1", oferta, perfil, java.time.Instant.parse("2026-10-10T00:00:00Z"));

        assertThat(calculada.observaciones()).isEmpty();

        EvaluacionOferta conPropuesta = calculada.conObservaciones(List.of(
                new ObservacionPropuesta("La oferta pide Java", "Java")
        ));

        assertThat(conPropuesta.estado()).isEqualTo(calculada.estado());
        assertThat(conPropuesta.coincidencias()).isEqualTo(calculada.coincidencias());
        assertThat(conPropuesta.brechas()).isEqualTo(calculada.brechas());
        assertThat(conPropuesta.datosPendientes()).isEqualTo(calculada.datosPendientes());
        assertThat(conPropuesta.id()).isEqualTo(calculada.id());
        assertThat(conPropuesta.fechaEvaluacion()).isEqualTo(calculada.fechaEvaluacion());
        assertThat(conPropuesta.observaciones()).hasSize(1);
        assertThat(calculada.observaciones()).isEmpty();
    }

    private static OfertaLaboral oferta() {
        return OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Backend Java Engineer")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(Set.of("Java"))
                .build();
    }

    private static PerfilProfesional perfil() {
        return PerfilProfesional.builder()
                .id("perf-100")
                .nombre("Gustavo")
                .tituloPrincipal("Java Developer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .tecnologias(Set.of("Java"))
                .build();
    }
}
