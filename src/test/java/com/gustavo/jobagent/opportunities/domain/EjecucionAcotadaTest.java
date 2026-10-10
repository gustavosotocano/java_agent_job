package com.gustavo.jobagent.opportunities.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EjecucionAcotadaTest {

    @Test
    @DisplayName("Un nombre distinto corta la lista y no ejecuta lo posterior")
    void nombreDistintoCorta() {
        EjecucionAcotada.Plan plan = EjecucionAcotada.planear(List.of(
                paso("buscarOfertas"),
                paso("postular"),
                paso("consultarOferta")
        ));

        assertThat(plan.estado()).isEqualTo(EjecucionAcotada.EstadoEjecucion.HERRAMIENTA_NO_AUTORIZADA);
        assertThat(plan.aEjecutar()).extracting(EjecucionAcotada.Solicitud::herramienta).containsExactly("buscarOfertas");
        assertThat(plan.rechazada().herramienta()).isEqualTo("postular");
        assertThat(plan.noEjecutadas()).containsExactly("consultarOferta");
        assertThat(plan.aEjecutar()).extracting(EjecucionAcotada.Solicitud::herramienta)
                .doesNotContain("postular", "iniciarSesion");
    }

    @Test
    @DisplayName("Iniciar sesión tampoco queda entre los pasos a ejecutar")
    void iniciarSesionNoSeEjecuta() {
        EjecucionAcotada.Plan plan = EjecucionAcotada.planear(List.of(paso("iniciarSesion")));

        assertThat(plan.aEjecutar()).isEmpty();
        assertThat(plan.estado()).isEqualTo(EjecucionAcotada.EstadoEjecucion.HERRAMIENTA_NO_AUTORIZADA);
    }

    @Test
    @DisplayName("Cinco pasos autorizados ejecutan cuatro; cuatro o menos completan; la lista vacía no hace nada")
    void respetaElMaximo() {
        EjecucionAcotada.Plan cinco = EjecucionAcotada.planear(List.of(
                paso("consultarPerfil"),
                paso("consultarPerfil"),
                paso("consultarPerfil"),
                paso("consultarPerfil"),
                paso("consultarOferta")
        ));
        assertThat(cinco.estado()).isEqualTo(EjecucionAcotada.EstadoEjecucion.LIMITE_ALCANZADO);
        assertThat(cinco.aEjecutar()).hasSize(4);
        assertThat(cinco.noEjecutadas()).containsExactly("consultarOferta");

        EjecucionAcotada.Plan cuatro = EjecucionAcotada.planear(List.of(
                paso("buscarOfertas"),
                paso("consultarPerfil"),
                paso("consultarOferta"),
                paso("guardarEvaluacion")
        ));
        assertThat(cuatro.estado()).isEqualTo(EjecucionAcotada.EstadoEjecucion.COMPLETADA);
        assertThat(cuatro.aEjecutar()).hasSize(4);
        assertThat(cuatro.noEjecutadas()).isEmpty();

        EjecucionAcotada.Plan uno = EjecucionAcotada.planear(List.of(paso("buscarOfertas")));
        assertThat(uno.estado()).isEqualTo(EjecucionAcotada.EstadoEjecucion.COMPLETADA);
        assertThat(uno.noEjecutadas()).isEmpty();

        EjecucionAcotada.Plan vacia = EjecucionAcotada.planear(List.of());
        assertThat(vacia.estado()).isEqualTo(EjecucionAcotada.EstadoEjecucion.COMPLETADA);
        assertThat(vacia.aEjecutar()).isEmpty();
        assertThat(vacia.noEjecutadas()).isEmpty();
    }

    private static EjecucionAcotada.Solicitud paso(String herramienta) {
        return new EjecucionAcotada.Solicitud(herramienta, null, null);
    }
}
