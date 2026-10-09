package com.gustavo.jobagent.opportunities.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentidadOfertaTest {

    @Test
    @DisplayName("Normaliza esquema, host, puertos por defecto, barra final y fragmento")
    void normalizaDireccionPublica() {
        IdentidadOferta identidad = IdentidadOferta.de(
                " SIMULADA ",
                " HTTPS://Example.COM:443/Jobs/a/#frag "
        );

        assertThat(identidad.fuenteId()).isEqualTo("SIMULADA");
        assertThat(identidad.urlNormalizada()).isEqualTo("https://example.com/Jobs/a");
    }

    @Test
    @DisplayName("Conserva path raíz, query y puerto no estándar")
    void conservaPathRaizQueryYPuerto() {
        assertThat(IdentidadOferta.de("SIM", "http://example.com:80/").urlNormalizada())
                .isEqualTo("http://example.com/");
        assertThat(IdentidadOferta.de("SIM", "https://example.com/Jobs/Ab?q=Ka").urlNormalizada())
                .isEqualTo("https://example.com/Jobs/Ab?q=Ka");
        assertThat(IdentidadOferta.de("SIM", "http://example.com:8080/a").urlNormalizada())
                .isEqualTo("http://example.com:8080/a");
    }

    @Test
    @DisplayName("Rechaza vacío, esquema distinto y dirección sin host")
    void rechazaDireccionesInvalidas() {
        assertThatThrownBy(() -> IdentidadOferta.de("SIM", "  "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IdentidadOferta.de("SIM", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IdentidadOferta.de("SIM", "ftp://example.com/a"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> IdentidadOferta.de(" ", "https://example.com/a"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
