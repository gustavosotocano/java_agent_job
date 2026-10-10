package com.gustavo.jobagent.opportunities.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DictamenAccesoTest {

    private static final Instant FECHA = Instant.parse("2026-10-10T00:00:00Z");

    @Test
    @DisplayName("Rechaza identificadores, motivos y descriptores que no cumplen la regla")
    void rechazaDictamenesInvalidos() {
        assertThatThrownBy(() -> DictamenAcceso.noComprobada(" ", "motivo", FECHA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DictamenAcceso.noComprobada("OTRA", "motivo", FECHA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DictamenAcceso.noComprobada("LINKEDIN", "  ", FECHA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DictamenAcceso.noComprobada("LINKEDIN", null, FECHA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DictamenAcceso(
                "LINKEDIN", false, "sin vía", DictamenAcceso.ClaseAcceso.FEED, null, null, null, FECHA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DictamenAcceso(
                "LINKEDIN", false, "sin vía", null, "cobertura", null, null, FECHA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DictamenAcceso(
                "LINKEDIN", true, "pública", DictamenAcceso.ClaseAcceso.FEED, "cobertura", null, "hoy", FECHA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DictamenAcceso(
                "LINKEDIN", true, "Exige iniciar sesión", DictamenAcceso.ClaseAcceso.FEED,
                "cobertura", "límites", "hoy", FECHA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new DictamenAcceso(
                "INDEED", true, "Usa cuenta personal del candidato", DictamenAcceso.ClaseAcceso.LISTADO_PUBLICO,
                "cobertura", "límites", "hoy", FECHA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Una vía no comprobada conserva el motivo y deja nulos los descriptores")
    void noComprobadaDejaDescriptoresNulos() {
        DictamenAcceso dictamen = DictamenAcceso.noComprobada("GLASSDOOR", "  sin API  ", FECHA);

        assertThat(dictamen.viaComprobada()).isFalse();
        assertThat(dictamen.motivo()).isEqualTo("sin API");
        assertThat(dictamen.claseAcceso()).isNull();
        assertThat(dictamen.cobertura()).isNull();
        assertThat(dictamen.limites()).isNull();
        assertThat(dictamen.actualidad()).isNull();
    }
}
