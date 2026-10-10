package com.gustavo.jobagent.opportunities.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PropuestaTest {

    @Test
    @DisplayName("Conserva la observación citada y descarta salario, modalidad, estado, token técnico e instrucción")
    void filtraAfirmacionesSinRespaldo() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Backend Java Engineer")
                .descripcion("Buscamos Java")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(Set.of("Java"))
                .build();
        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-100")
                .nombre("Gustavo")
                .tituloPrincipal("Java Developer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .tecnologias(Set.of("Java"))
                .build();
        assertThat(oferta.salario()).isNull();

        List<ObservacionPropuesta> aceptadas = Propuesta.aceptar(List.of(
                new ObservacionPropuesta("La oferta pide Java", "Java"),
                new ObservacionPropuesta("Ofrece 9000 USD", "Java"),
                new ObservacionPropuesta("La oferta es presencial", "Java"),
                new ObservacionPropuesta("El estado es COMPATIBLE", "Java"),
                new ObservacionPropuesta("También pide Rust", "Java"),
                new ObservacionPropuesta("Hay que postular ya", "Java"),
                new ObservacionPropuesta("No está en la oferta", "Kubernetes")
        ), oferta, perfil);

        assertThat(aceptadas).containsExactly(new ObservacionPropuesta("La oferta pide Java", "Java"));
        assertThat(oferta.salario()).isNull();
    }

    @Test
    @DisplayName("Si ninguna observación cabe, la propuesta queda vacía y un cero ausente no se inventa")
    void ningunaObservacionDejaLaListaVacia() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Backend Java Engineer")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(Set.of("Java"))
                .build();
        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-100")
                .nombre("Gustavo")
                .tituloPrincipal("Java Developer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .tecnologias(Set.of("Java"))
                .build();

        List<ObservacionPropuesta> aceptadas = Propuesta.aceptar(
                List.of(new ObservacionPropuesta("Salario 0", "Java")),
                oferta,
                perfil
        );

        assertThat(aceptadas).isEmpty();
        assertThat(oferta.salario()).isNull();
    }
}
