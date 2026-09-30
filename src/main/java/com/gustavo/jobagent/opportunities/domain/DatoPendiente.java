package com.gustavo.jobagent.opportunities.domain;

import java.util.Objects;

/**
 * Value Object inmutable que representa un dato ausente o ambiguo que requiere clarificación.
 */
public record DatoPendiente(
        TipoAspecto aspecto,
        String pregunta
) {
    public DatoPendiente {
        Objects.requireNonNull(aspecto, "El tipo de aspecto es obligatorio");
        if (pregunta == null || pregunta.isBlank()) {
            throw new IllegalArgumentException("La pregunta o detalle del dato pendiente no puede estar vacía");
        }
        pregunta = pregunta.strip();
    }
}
