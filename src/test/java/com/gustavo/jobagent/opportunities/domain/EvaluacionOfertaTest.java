package com.gustavo.jobagent.opportunities.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvaluacionOfertaTest {

    @Test
    @DisplayName("Escenario 1: Coincidencia total en tecnologías, modalidad remota y salario cubierto -> COMPATIBLE")
    void evaluacionTotalmenteCompatible() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Senior Java Developer")
                .modalidad(ModalidadTrabajo.REMOTE)
                .ubicacion("Colombia")
                .tecnologias(Set.of("Java", "Spring Boot", "Kafka"))
                .salario(Salario.mensual(new BigDecimal("4500"), new BigDecimal("6000"), "USD"))
                .build();

        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-001")
                .nombre("Gustavo")
                .tituloPrincipal("Senior Backend Engineer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE, ModalidadTrabajo.HYBRID))
                .ubicacion("Colombia")
                .tecnologias(Set.of("Java", "Spring Boot", "Kafka", "Docker", "AWS"))
                .expectativaSalarial(Salario.mensual(new BigDecimal("4000"), new BigDecimal("5500"), "USD"))
                .build();

        EvaluacionOferta evaluacion = EvaluacionOferta.evaluar(oferta, perfil);

        assertThat(evaluacion.idOferta()).isEqualTo("sim-001");
        assertThat(evaluacion.idPerfil()).isEqualTo("perf-001");
        assertThat(evaluacion.estado()).isEqualTo(EstadoEvaluacion.COMPATIBLE);
        assertThat(evaluacion.brechas()).isEmpty();
        assertThat(evaluacion.coincidencias()).isNotEmpty();
        assertThat(evaluacion.coincidencias()).anyMatch(c -> c.aspecto() == TipoAspecto.TECNOLOGIA && c.detalle().contains("Java"));
        assertThat(evaluacion.coincidencias()).anyMatch(c -> c.aspecto() == TipoAspecto.MODALIDAD);
        assertThat(evaluacion.coincidencias()).anyMatch(c -> c.aspecto() == TipoAspecto.SALARIO);
    }

    @Test
    @DisplayName("Escenario 2: Faltan tecnologías requeridas pero hay coincidencias -> COMPATIBLE_CON_BRECHAS")
    void evaluacionCompatibleConBrechasTecnicas() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-002")
                .fuenteId("SIMULADA")
                .titulo("Java & Cloud Architect")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(Set.of("Java", "Kubernetes", "GCP"))
                .build();

        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-001")
                .nombre("Gustavo")
                .tituloPrincipal("Java Engineer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .tecnologias(Set.of("Java", "Spring Boot"))
                .build();

        EvaluacionOferta evaluacion = EvaluacionOferta.evaluar(oferta, perfil);

        assertThat(evaluacion.estado()).isEqualTo(EstadoEvaluacion.COMPATIBLE_CON_BRECHAS);
        assertThat(evaluacion.brechas()).hasSize(2);
        assertThat(evaluacion.brechas()).allMatch(b -> !b.esBloqueante());
        assertThat(evaluacion.coincidencias()).anyMatch(c -> c.detalle().contains("Java"));
    }

    @Test
    @DisplayName("Escenario 3: Incompatibilidad de modalidad -> NO_COMPATIBLE por brecha bloqueante")
    void evaluacionNoCompatiblePorModalidad() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-003")
                .fuenteId("SIMULADA")
                .titulo("Backend Developer")
                .modalidad(ModalidadTrabajo.ONSITE)
                .ubicacion("Bogotá")
                .tecnologias(Set.of("Java"))
                .build();

        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-001")
                .nombre("Gustavo")
                .tituloPrincipal("Java Engineer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .ubicacion("Medellín")
                .tecnologias(Set.of("Java"))
                .build();

        EvaluacionOferta evaluacion = EvaluacionOferta.evaluar(oferta, perfil);

        assertThat(evaluacion.estado()).isEqualTo(EstadoEvaluacion.NO_COMPATIBLE);
        assertThat(evaluacion.brechas()).anyMatch(b -> b.aspecto() == TipoAspecto.MODALIDAD && b.esBloqueante());
    }

    @Test
    @DisplayName("Escenario 4: Incompatibilidad de ubicación en puesto ONSITE/HYBRID -> NO_COMPATIBLE por brecha bloqueante")
    void evaluacionNoCompatiblePorUbicacion() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-004")
                .fuenteId("SIMULADA")
                .titulo("Java Developer Presencial")
                .modalidad(ModalidadTrabajo.HYBRID)
                .ubicacion("Madrid, España")
                .tecnologias(Set.of("Java"))
                .build();

        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-001")
                .nombre("Gustavo")
                .tituloPrincipal("Java Engineer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.HYBRID, ModalidadTrabajo.REMOTE))
                .ubicacion("Bogotá, Colombia")
                .tecnologias(Set.of("Java"))
                .build();

        EvaluacionOferta evaluacion = EvaluacionOferta.evaluar(oferta, perfil);

        assertThat(evaluacion.estado()).isEqualTo(EstadoEvaluacion.NO_COMPATIBLE);
        assertThat(evaluacion.brechas()).anyMatch(b -> b.aspecto() == TipoAspecto.UBICACION && b.esBloqueante());
    }

    @Test
    @DisplayName("Escenario 5: Oferta sin salario ni perfil con expectativa salarial -> genera DatoPendiente sin inventar valores")
    void evaluacionConDatosPendientesSalariales() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-005")
                .fuenteId("SIMULADA")
                .titulo("Java Developer")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(Set.of("Java"))
                .salario(null)
                .build();

        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-001")
                .nombre("Gustavo")
                .tituloPrincipal("Java Engineer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .tecnologias(Set.of("Java"))
                .expectativaSalarial(null)
                .build();

        EvaluacionOferta evaluacion = EvaluacionOferta.evaluar(oferta, perfil);

        assertThat(evaluacion.estado()).isEqualTo(EstadoEvaluacion.COMPATIBLE);
        assertThat(evaluacion.datosPendientes()).anyMatch(d -> d.aspecto() == TipoAspecto.SALARIO);
    }

    @Test
    @DisplayName("Escenario 6: Cero coincidencias en tecnologías requeridas -> NO_COMPATIBLE")
    void evaluacionSinCoincidenciasTecnologicas() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-006")
                .fuenteId("SIMULADA")
                .titulo("Python Developer")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(Set.of("Python", "Django", "FastAPI"))
                .build();

        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-001")
                .nombre("Gustavo")
                .tituloPrincipal("Java Engineer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .tecnologias(Set.of("Java", "Spring Boot"))
                .build();

        EvaluacionOferta evaluacion = EvaluacionOferta.evaluar(oferta, perfil);

        assertThat(evaluacion.estado()).isEqualTo(EstadoEvaluacion.NO_COMPATIBLE);
        assertThat(evaluacion.coincidencias()).noneMatch(c -> c.aspecto() == TipoAspecto.TECNOLOGIA);
    }

    @Test
    @DisplayName("Invariantes: constructor rechaza campos obligatorios nulos o vacíos")
    void validacionInvariantes() {
        assertThatThrownBy(() -> new EvaluacionOferta(null, "sim-001", "perf-001", EstadoEvaluacion.COMPATIBLE, null, null, null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> EvaluacionOferta.evaluar(null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
