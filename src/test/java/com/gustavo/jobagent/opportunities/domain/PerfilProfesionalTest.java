package com.gustavo.jobagent.opportunities.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerfilProfesionalTest {

    @Test
    @DisplayName("Debe construir un perfil profesional válido con campos normalizados")
    void debeConstruirPerfilValido() {
        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("  perf-001  ")
                .nombre("  Gustavo Soto  ")
                .tituloPrincipal("  Senior Backend Developer  ")
                .tecnologias(Set.of(" Java ", "SPRING BOOT", "Kafka "))
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE, ModalidadTrabajo.HYBRID))
                .ubicacion("  Colombia  ")
                .anosExperiencia(8)
                .expectativaSalarial(Salario.mensual(new BigDecimal("4000"), new BigDecimal("5500"), "USD"))
                .build();

        assertThat(perfil.id()).isEqualTo("perf-001");
        assertThat(perfil.nombre()).isEqualTo("Gustavo Soto");
        assertThat(perfil.tituloPrincipal()).isEqualTo("Senior Backend Developer");
        assertThat(perfil.ubicacion()).isEqualTo("Colombia");
        assertThat(perfil.anosExperiencia()).isEqualTo(8);
        assertThat(perfil.tecnologias()).containsExactlyInAnyOrder("Java", "SPRING BOOT", "Kafka");
        assertThat(perfil.modalidadesAceptadas()).containsExactlyInAnyOrder(ModalidadTrabajo.REMOTE, ModalidadTrabajo.HYBRID);
        assertThat(perfil.expectativaSalarial()).isNotNull();
    }

    @Test
    @DisplayName("Debe rechazar identificador, nombre o título nulos o vacíos")
    void debeRechazarCamposObligatoriosInvalidos() {
        assertThatThrownBy(() -> new PerfilProfesional(null, "Nombre", "Titulo", null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("identificador del perfil es obligatorio");

        assertThatThrownBy(() -> new PerfilProfesional("   ", "Nombre", "Titulo", null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new PerfilProfesional("id", null, "Titulo", null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre del candidato es obligatorio");

        assertThatThrownBy(() -> new PerfilProfesional("id", "Nombre", "", null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("título principal es obligatorio");

        assertThatThrownBy(() -> new PerfilProfesional("id", "Nombre", "Titulo", null, null, null, null, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("años de experiencia no pueden ser negativos");
    }

    @Test
    @DisplayName("dominaTecnologia debe retornar true con matching insensible a mayúsculas y espacios")
    void dominaTecnologiaInsensibleMayusculas() {
        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-001")
                .nombre("Gustavo")
                .tituloPrincipal("Developer")
                .tecnologias(Set.of("Java", "Spring Boot", "Kafka"))
                .build();

        assertThat(perfil.dominaTecnologia("java")).isTrue();
        assertThat(perfil.dominaTecnologia("  JAVA  ")).isTrue();
        assertThat(perfil.dominaTecnologia("Spring Boot")).isTrue();
        assertThat(perfil.dominaTecnologia("spring boot")).isTrue();
        assertThat(perfil.dominaTecnologia("Python")).isFalse();
        assertThat(perfil.dominaTecnologia("")).isFalse();
        assertThat(perfil.dominaTecnologia(null)).isFalse();
    }
}
