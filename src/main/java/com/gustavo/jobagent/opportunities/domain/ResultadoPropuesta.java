package com.gustavo.jobagent.opportunities.domain;

import java.util.List;
import java.util.Objects;

/**
 * Lo que devolvió el puerto de lenguaje antes de filtrar contra la oferta y el perfil.
 */
public sealed interface ResultadoPropuesta {

    record Disponible(List<ObservacionPropuesta> observaciones) implements ResultadoPropuesta {
        public Disponible {
            observaciones = observaciones == null ? List.of() : List.copyOf(observaciones);
        }
    }

    record NoDisponible(Motivo motivo) implements ResultadoPropuesta {
        public NoDisponible {
            Objects.requireNonNull(motivo, "El motivo de la propuesta no disponible es obligatorio");
        }
    }

    enum Motivo {
        FALLO,
        TIEMPO_AGOTADO,
        ILEGIBLE
    }
}
