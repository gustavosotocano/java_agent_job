package com.gustavo.jobagent.opportunities.domain;

import java.util.Objects;

/**
 * Observación citada que acompaña a una evaluación sin formar parte del cálculo.
 */
public record ObservacionPropuesta(String texto, String fragmento) {

    public ObservacionPropuesta {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto de la observación no puede estar vacío");
        }
        if (fragmento == null || fragmento.isBlank()) {
            throw new IllegalArgumentException("El fragmento de la observación no puede estar vacío");
        }
        texto = texto.strip();
        fragmento = fragmento.strip();
    }
}
