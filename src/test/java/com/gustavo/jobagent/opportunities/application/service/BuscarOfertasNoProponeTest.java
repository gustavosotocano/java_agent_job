package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.out.PropuestaLenguajePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BuscarOfertasNoProponeTest {

    @Test
    @DisplayName("Buscar no pide una propuesta, no postula y no inicia sesión")
    void buscarNoPropone() throws Exception {
        assertThat(BuscarOfertasService.class.getDeclaredMethods())
                .extracting(Method::getName)
                .noneMatch(nombre -> {
                    String normalizado = nombre.toLowerCase();
                    return normalizado.contains("postul") || normalizado.contains("login") || normalizado.contains("aplicar");
                });
        assertThat(BuscarOfertasService.class.getDeclaredFields())
                .noneMatch(campo -> campo.getType().equals(PropuestaLenguajePort.class));
        String fuente = Files.readString(Path.of(
                "src/main/java/com/gustavo/jobagent/opportunities/application/service/BuscarOfertasService.java"
        ));
        assertThat(fuente).doesNotContain("PropuestaLenguajePort");
    }
}
