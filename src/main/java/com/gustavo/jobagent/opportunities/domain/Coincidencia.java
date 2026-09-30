package com.gustavo.jobagent.opportunities.domain;

import java.util.Objects;

/**
 * Value Object inmutable que representa un aspecto en el cual el perfil cumple o supera la oferta laboral.
 */
public record Coincidencia(
        TipoAspecto aspecto,
        String detalle,
        String evidencia
) {
    public Coincidencia {
        Objects.requireNonNull(aspecto, "El tipo de aspecto es obligatorio");
        if (detalle == null || detalle.isBlank()) {
            throw new IllegalArgumentException("El detalle de la coincidencia no puede estar vacío");
        }
        if (evidencia == null || evidencia.isBlank()) {
            throw new IllegalArgumentException("La evidencia de la coincidencia no puede estar vacía");
        }
        detalle = detalle.strip();
        evidencia = evidencia.strip();
    }
}
