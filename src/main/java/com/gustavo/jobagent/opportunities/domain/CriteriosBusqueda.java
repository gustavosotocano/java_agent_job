package com.gustavo.jobagent.opportunities.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Criterios de búsqueda explícitos para consultar ofertas laborales.
 */
public record CriteriosBusqueda(
        String cargo,
        Set<String> tecnologias,
        String ubicacion,
        ModalidadTrabajo modalidad,
        int limite
) {
    public static final int LIMITE_PREDETERMINADO = 20;
    public static final int LIMITE_MINIMO = 1;
    public static final int LIMITE_MAXIMO = 100;

    public CriteriosBusqueda {
        if (cargo == null || cargo.isBlank()) {
            throw new IllegalArgumentException("El cargo es obligatorio y no puede estar vacío");
        }
        cargo = cargo.strip();

        if (tecnologias == null) {
            tecnologias = Collections.emptySet();
        } else {
            Set<String> cleanTechs = new LinkedHashSet<>();
            for (String tech : tecnologias) {
                if (tech != null && !tech.isBlank()) {
                    cleanTechs.add(tech.strip());
                }
            }
            tecnologias = Collections.unmodifiableSet(cleanTechs);
        }

        if (ubicacion != null) {
            ubicacion = ubicacion.strip();
            if (ubicacion.isEmpty()) {
                ubicacion = null;
            }
        }

        if (limite < LIMITE_MINIMO || limite > LIMITE_MAXIMO) {
            throw new IllegalArgumentException("El límite debe estar entre " + LIMITE_MINIMO + " y " + LIMITE_MAXIMO);
        }
    }

    public CriteriosBusqueda(String cargo, Collection<String> tecnologias, String ubicacion, ModalidadTrabajo modalidad, Integer limite) {
        this(cargo,
                tecnologias != null ? new LinkedHashSet<>(tecnologias) : Collections.emptySet(),
                ubicacion,
                modalidad,
                limite != null ? limite : LIMITE_PREDETERMINADO);
    }

    public CriteriosBusqueda(String cargo, Collection<String> tecnologias, String ubicacion, ModalidadTrabajo modalidad) {
        this(cargo, tecnologias, ubicacion, modalidad, LIMITE_PREDETERMINADO);
    }

    public CriteriosBusqueda(String cargo) {
        this(cargo, Collections.emptySet(), null, null, LIMITE_PREDETERMINADO);
    }
}
