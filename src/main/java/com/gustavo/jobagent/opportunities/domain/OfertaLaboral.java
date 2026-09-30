package com.gustavo.jobagent.opportunities.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Representa una oferta laboral normalizada capturada desde una fuente.
 */
public record OfertaLaboral(
        String idFuente,
        String fuenteId,
        String titulo,
        String empresa,
        String descripcion,
        String ubicacion,
        ModalidadTrabajo modalidad,
        String urlOriginal,
        Instant fechaPublicacion,
        Salario salario,
        Set<String> tecnologias
) {
    public OfertaLaboral {
        if (idFuente == null || idFuente.isBlank()) {
            throw new IllegalArgumentException("El idFuente es obligatorio y no puede estar vacío");
        }
        if (fuenteId == null || fuenteId.isBlank()) {
            throw new IllegalArgumentException("El fuenteId es obligatorio y no puede estar vacío");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio y no puede estar vacío");
        }

        idFuente = idFuente.strip();
        fuenteId = fuenteId.strip();
        titulo = titulo.strip();
        empresa = (empresa != null) ? empresa.strip() : null;
        descripcion = (descripcion != null) ? descripcion.strip() : null;
        ubicacion = (ubicacion != null) ? ubicacion.strip() : null;
        urlOriginal = (urlOriginal != null) ? urlOriginal.strip() : null;

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
    }

    /**
     * Evalúa si esta oferta coincide con los criterios de búsqueda según las reglas de negocio de UC-001.
     *
     * @param criterios Criterios de búsqueda a evaluar.
     * @return true si cumple todos los filtros combinados con AND; false en caso contrario.
     */
    public boolean coincideCon(CriteriosBusqueda criterios) {
        Objects.requireNonNull(criterios, "Los criterios de búsqueda no pueden ser nulos");

        // 1. Cargo: coincidencia parcial en título, sin distinguir mayúsculas
        if (!this.titulo.toLowerCase(Locale.ROOT).contains(criterios.cargo().toLowerCase(Locale.ROOT))) {
            return false;
        }

        // 2. Tecnologías: todas las solicitadas deben figurar en el conjunto de tecnologías de la oferta
        if (!criterios.tecnologias().isEmpty()) {
            for (String techRequerida : criterios.tecnologias()) {
                boolean encontrada = this.tecnologias.stream()
                        .anyMatch(t -> t.equalsIgnoreCase(techRequerida));
                if (!encontrada) {
                    return false;
                }
            }
        }

        // 3. Ubicación: coincidencia parcial, sin distinguir mayúsculas (si se especificó filtro)
        if (criterios.ubicacion() != null) {
            if (this.ubicacion == null || !this.ubicacion.toLowerCase(Locale.ROOT).contains(criterios.ubicacion().toLowerCase(Locale.ROOT))) {
                return false;
            }
        }

        // 4. Modalidad: igualdad exacta (modalidad desconocida no satisface un filtro explícito)
        if (criterios.modalidad() != null) {
            if (this.modalidad != criterios.modalidad()) {
                return false;
            }
        }

        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String idFuente;
        private String fuenteId;
        private String titulo;
        private String empresa;
        private String descripcion;
        private String ubicacion;
        private ModalidadTrabajo modalidad;
        private String urlOriginal;
        private Instant fechaPublicacion;
        private Salario salario;
        private Set<String> tecnologias = new LinkedHashSet<>();

        public Builder idFuente(String idFuente) {
            this.idFuente = idFuente;
            return this;
        }

        public Builder fuenteId(String fuenteId) {
            this.fuenteId = fuenteId;
            return this;
        }

        public Builder titulo(String titulo) {
            this.titulo = titulo;
            return this;
        }

        public Builder empresa(String empresa) {
            this.empresa = empresa;
            return this;
        }

        public Builder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Builder ubicacion(String ubicacion) {
            this.ubicacion = ubicacion;
            return this;
        }

        public Builder modalidad(ModalidadTrabajo modalidad) {
            this.modalidad = modalidad;
            return this;
        }

        public Builder urlOriginal(String urlOriginal) {
            this.urlOriginal = urlOriginal;
            return this;
        }

        public Builder fechaPublicacion(Instant fechaPublicacion) {
            this.fechaPublicacion = fechaPublicacion;
            return this;
        }

        public Builder salario(Salario salario) {
            this.salario = salario;
            return this;
        }

        public Builder tecnologias(Collection<String> tecnologias) {
            this.tecnologias = (tecnologias != null) ? new LinkedHashSet<>(tecnologias) : new LinkedHashSet<>();
            return this;
        }

        public Builder addTecnologia(String tecnologia) {
            if (tecnologia != null && !tecnologia.isBlank()) {
                this.tecnologias.add(tecnologia.strip());
            }
            return this;
        }

        public OfertaLaboral build() {
            return new OfertaLaboral(
                    idFuente,
                    fuenteId,
                    titulo,
                    empresa,
                    descripcion,
                    ubicacion,
                    modalidad,
                    urlOriginal,
                    fechaPublicacion,
                    salario,
                    tecnologias
            );
        }
    }
}
