package com.gustavo.jobagent.opportunities.domain;

import java.util.Objects;

/**
 * Value Object inmutable que representa un requisito exigido por la oferta laboral no satisfecho por el perfil.
 */
public record Brecha(
        TipoAspecto aspecto,
        String detalle,
        boolean esBloqueante
) {
    public Brecha {
        Objects.requireNonNull(aspecto, "El tipo de aspecto es obligatorio");
        if (detalle == null || detalle.isBlank()) {
            throw new IllegalArgumentException("El detalle de la brecha no puede estar vacío");
        }
        detalle = detalle.strip();
    }
}
