package com.gustavo.jobagent.opportunities.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfertaLaboralTest {

    @Test
    @DisplayName("Debe construir una oferta laboral válida y mantener inmutabilidad de tecnologías")
    void debeConstruirOfertaLaboralValida() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Senior Java Backend Engineer")
                .empresa("Empresa de ejemplo")
                .descripcion("Java, Spring Boot, Kafka y AWS")
                .ubicacion("Colombia")
                .modalidad(ModalidadTrabajo.REMOTE)
                .urlOriginal("https://example.com/jobs/sim-001")
                .tecnologias(List.of("Java", "Kafka", "Spring Boot", "AWS"))
                .build();

        assertThat(oferta.idFuente()).isEqualTo("sim-001");
        assertThat(oferta.fuenteId()).isEqualTo("SIMULADA");
        assertThat(oferta.titulo()).isEqualTo("Senior Java Backend Engineer");
        assertThat(oferta.empresa()).isEqualTo("Empresa de ejemplo");
        assertThat(oferta.descripcion()).isEqualTo("Java, Spring Boot, Kafka y AWS");
        assertThat(oferta.ubicacion()).isEqualTo("Colombia");
        assertThat(oferta.modalidad()).isEqualTo(ModalidadTrabajo.REMOTE);
        assertThat(oferta.urlOriginal()).isEqualTo("https://example.com/jobs/sim-001");
        assertThat(oferta.fechaPublicacion()).isNull();
        assertThat(oferta.salario()).isNull();
        assertThat(oferta.tecnologias()).containsExactly("Java", "Kafka", "Spring Boot", "AWS");

        assertThatThrownBy(() -> oferta.tecnologias().add("Docker"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Debe fallar al crear oferta con campos obligatorios vacíos")
    void debeFallarConCamposObligatoriosVacios(String valorInvalido) {
        assertThatThrownBy(() -> new OfertaLaboral(valorInvalido, "SIMULADA", "Titulo", null, null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El idFuente es obligatorio");

        assertThatThrownBy(() -> new OfertaLaboral("sim-001", valorInvalido, "Titulo", null, null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El fuenteId es obligatorio");

        assertThatThrownBy(() -> new OfertaLaboral("sim-001", "SIMULADA", valorInvalido, null, null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El título es obligatorio");
    }

    @Test
    @DisplayName("Criterio de aceptación: Buscar Backend + Java + Kafka + Colombia + REMOTE devuelve sim-001")
    void debeCoincidirOfertaSim001ConCriteriosCompletos() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Senior Java Backend Engineer")
                .empresa("Empresa de ejemplo")
                .descripcion("Java, Spring Boot, Kafka y AWS")
                .ubicacion("Colombia")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(List.of("Java", "Kafka", "Spring Boot", "AWS"))
                .build();

        CriteriosBusqueda criterios = new CriteriosBusqueda(
                "Backend",
                List.of("Java", "Kafka"),
                "Colombia",
                ModalidadTrabajo.REMOTE
        );

        assertThat(oferta.coincideCon(criterios)).isTrue();
    }

    @Test
    @DisplayName("Criterio de aceptación: Una oferta Java sin Kafka queda fuera de la búsqueda que exige Kafka")
    void debeExcluirOfertaQueNoTieneTodasLasTecnologiasSolicitadas() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-002")
                .fuenteId("SIMULADA")
                .titulo("Senior Java Backend Engineer")
                .ubicacion("Colombia")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(List.of("Java", "Spring Boot"))
                .build();

        CriteriosBusqueda criterios = new CriteriosBusqueda(
                "Backend",
                List.of("Java", "Kafka"),
                "Colombia",
                ModalidadTrabajo.REMOTE
        );

        assertThat(oferta.coincideCon(criterios)).isFalse();
    }

    @Test
    @DisplayName("Criterio de aceptación: Una oferta ONSITE queda fuera cuando se solicita REMOTE")
    void debeExcluirOfertaConModalidadDiferente() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-003")
                .fuenteId("SIMULADA")
                .titulo("Senior Java Backend Engineer")
                .ubicacion("Colombia")
                .modalidad(ModalidadTrabajo.ONSITE)
                .tecnologias(List.of("Java", "Kafka"))
                .build();

        CriteriosBusqueda criterios = new CriteriosBusqueda(
                "Backend",
                List.of("Java", "Kafka"),
                "Colombia",
                ModalidadTrabajo.REMOTE
        );

        assertThat(oferta.coincideCon(criterios)).isFalse();
    }

    @Test
    @DisplayName("Criterio de aceptación: Cambiar mayúsculas y minúsculas en cargo y tecnologías conserva el resultado")
    void debeCoincidirSinDistinguirMayusculasNiMinusculas() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Senior Java Backend Engineer")
                .ubicacion("Colombia")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(List.of("Java", "Kafka"))
                .build();

        CriteriosBusqueda criterios = new CriteriosBusqueda(
                "bAcKeNd",
                List.of("jAvA", "KAFKA"),
                "cOlOmBiA",
                ModalidadTrabajo.REMOTE
        );

        assertThat(oferta.coincideCon(criterios)).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar oferta cuando el título no coincide con el cargo")
    void debeRechazarCuandoTituloNoCoincide() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-004")
                .fuenteId("SIMULADA")
                .titulo("Frontend React Specialist")
                .tecnologias(List.of("React", "TypeScript"))
                .build();

        CriteriosBusqueda criterios = new CriteriosBusqueda("Backend");

        assertThat(oferta.coincideCon(criterios)).isFalse();
    }

    @Test
    @DisplayName("Modalidad desconocida (null) en la oferta no satisface un filtro explícito de modalidad")
    void modalidadDesconocidaNoSatisfaceFiltroExplicito() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-005")
                .fuenteId("SIMULADA")
                .titulo("Backend Developer")
                .modalidad(null)
                .build();

        CriteriosBusqueda criterios = new CriteriosBusqueda("Backend", List.of(), null, ModalidadTrabajo.REMOTE);

        assertThat(oferta.coincideCon(criterios)).isFalse();
    }

    @Test
    @DisplayName("Ubicación desconocida (null) en la oferta no satisface un filtro explícito de ubicación")
    void ubicacionDesconocidaNoSatisfaceFiltroExplicito() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-006")
                .fuenteId("SIMULADA")
                .titulo("Backend Developer")
                .ubicacion(null)
                .build();

        CriteriosBusqueda criterios = new CriteriosBusqueda("Backend", List.of(), "Colombia", null);

        assertThat(oferta.coincideCon(criterios)).isFalse();
    }

    @Test
    @DisplayName("Criterios con solo cargo coinciden con cualquier oferta que contenga el cargo sin importar modalidad ni ubicación")
    void criteriosBasicosCoincidenSinFiltrosOpcionales() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-007")
                .fuenteId("SIMULADA")
                .titulo("Lead Backend Architect")
                .ubicacion(null)
                .modalidad(null)
                .tecnologias(List.of())
                .build();

        CriteriosBusqueda criterios = new CriteriosBusqueda("Backend");

        assertThat(oferta.coincideCon(criterios)).isTrue();
    }
}
