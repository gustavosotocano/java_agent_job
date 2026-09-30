package com.gustavo.jobagent.opportunities.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CriteriosBusquedaTest {

    @Test
    @DisplayName("Debe crear criterios válidos con valores por defecto")
    void debeCrearCriteriosValidosConValoresPorDefecto() {
        CriteriosBusqueda criterios = new CriteriosBusqueda("Backend");

        assertThat(criterios.cargo()).isEqualTo("Backend");
        assertThat(criterios.tecnologias()).isEmpty();
        assertThat(criterios.ubicacion()).isNull();
        assertThat(criterios.modalidad()).isNull();
        assertThat(criterios.limite()).isEqualTo(20);
    }

    @Test
    @DisplayName("Debe normalizar y limpiar espacios en blanco en cargo, tecnologías y ubicación")
    void debeNormalizarYLimpiarEspacios() {
        CriteriosBusqueda criterios = new CriteriosBusqueda(
                "  Backend Developer  ",
                List.of("  Java  ", "Kafka", "  "),
                "  Colombia  ",
                ModalidadTrabajo.REMOTE,
                15
        );

        assertThat(criterios.cargo()).isEqualTo("Backend Developer");
        assertThat(criterios.tecnologias()).containsExactly("Java", "Kafka");
        assertThat(criterios.ubicacion()).isEqualTo("Colombia");
        assertThat(criterios.modalidad()).isEqualTo(ModalidadTrabajo.REMOTE);
        assertThat(criterios.limite()).isEqualTo(15);
    }

    @Test
    @DisplayName("Debe convertir ubicación en blanco a null")
    void debeConvertirUbicacionEnBlancoANull() {
        CriteriosBusqueda criterios = new CriteriosBusqueda(
                "Backend",
                Set.of("Java"),
                "   ",
                null,
                20
        );

        assertThat(criterios.ubicacion()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Debe rechazar cargo vacío o en blanco")
    void debeRechazarCargoVacioOEnBlanco(String cargoInvalido) {
        assertThatThrownBy(() -> new CriteriosBusqueda(cargoInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El cargo es obligatorio");
    }

    @Test
    @DisplayName("Debe rechazar cargo nulo")
    void debeRechazarCargoNulo() {
        assertThatThrownBy(() -> new CriteriosBusqueda(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El cargo es obligatorio");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -50, 101, 200})
    @DisplayName("Debe rechazar límites fuera del rango permitido de 1 a 100")
    void debeRechazarLimiteFueraDeRango(int limiteInvalido) {
        assertThatThrownBy(() -> new CriteriosBusqueda("Backend", List.of(), null, null, limiteInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El límite debe estar entre 1 y 100");
    }

    @Test
    @DisplayName("La colección de tecnologías debe ser inmutable")
    void coleccionTecnologiasDebeSerInmutable() {
        CriteriosBusqueda criterios = new CriteriosBusqueda("Backend", List.of("Java"), null, null, 20);

        assertThatThrownBy(() -> criterios.tecnologias().add("Kafka"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
