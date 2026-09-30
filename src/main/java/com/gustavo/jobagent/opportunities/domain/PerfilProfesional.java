package com.gustavo.jobagent.opportunities.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad inmutable que representa el perfil profesional de un candidato.
 */
public record PerfilProfesional(
        String id,
        String nombre,
        String tituloPrincipal,
        Set<String> tecnologias,
        Set<ModalidadTrabajo> modalidadesAceptadas,
        String ubicacion,
        Salario expectativaSalarial,
        Integer anosExperiencia
) {
    public PerfilProfesional {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador del perfil es obligatorio y no puede estar vacío");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del candidato es obligatorio y no puede estar vacío");
        }
        if (tituloPrincipal == null || tituloPrincipal.isBlank()) {
            throw new IllegalArgumentException("El título principal es obligatorio y no puede estar vacío");
        }
        if (anosExperiencia != null && anosExperiencia < 0) {
            throw new IllegalArgumentException("Los años de experiencia no pueden ser negativos");
        }

        id = id.strip();
        nombre = nombre.strip();
        tituloPrincipal = tituloPrincipal.strip();
        ubicacion = (ubicacion != null) ? ubicacion.strip() : null;

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

        if (modalidadesAceptadas == null) {
            modalidadesAceptadas = Collections.emptySet();
        } else {
            Set<ModalidadTrabajo> cleanModalidades = new LinkedHashSet<>();
            for (ModalidadTrabajo mod : modalidadesAceptadas) {
                if (mod != null) {
                    cleanModalidades.add(mod);
                }
            }
            modalidadesAceptadas = Collections.unmodifiableSet(cleanModalidades);
        }
    }

    /**
     * Verifica si el candidato domina una tecnología dada, sin distinguir mayúsculas ni espacios redundantes.
     */
    public boolean dominaTecnologia(String tecnologia) {
        if (tecnologia == null || tecnologia.isBlank()) {
            return false;
        }
        String clean = tecnologia.strip();
        return this.tecnologias.stream().anyMatch(t -> t.equalsIgnoreCase(clean));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String nombre;
        private String tituloPrincipal;
        private Set<String> tecnologias = new LinkedHashSet<>();
        private Set<ModalidadTrabajo> modalidadesAceptadas = new LinkedHashSet<>();
        private String ubicacion;
        private Salario expectativaSalarial;
        private Integer anosExperiencia;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder tituloPrincipal(String tituloPrincipal) {
            this.tituloPrincipal = tituloPrincipal;
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

        public Builder modalidadesAceptadas(Collection<ModalidadTrabajo> modalidades) {
            this.modalidadesAceptadas = (modalidades != null) ? new LinkedHashSet<>(modalidades) : new LinkedHashSet<>();
            return this;
        }

        public Builder addModalidad(ModalidadTrabajo modalidad) {
            if (modalidad != null) {
                this.modalidadesAceptadas.add(modalidad);
            }
            return this;
        }

        public Builder ubicacion(String ubicacion) {
            this.ubicacion = ubicacion;
            return this;
        }

        public Builder expectativaSalarial(Salario expectativaSalarial) {
            this.expectativaSalarial = expectativaSalarial;
            return this;
        }

        public Builder anosExperiencia(Integer anosExperiencia) {
            this.anosExperiencia = anosExperiencia;
            return this;
        }

        public PerfilProfesional build() {
            return new PerfilProfesional(
                    id,
                    nombre,
                    tituloPrincipal,
                    tecnologias,
                    modalidadesAceptadas,
                    ubicacion,
                    expectativaSalarial,
                    anosExperiencia
            );
        }
    }
}
