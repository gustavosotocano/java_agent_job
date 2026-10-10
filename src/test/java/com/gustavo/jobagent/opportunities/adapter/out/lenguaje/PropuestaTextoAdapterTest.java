package com.gustavo.jobagent.opportunities.adapter.out.lenguaje;

import com.gustavo.jobagent.opportunities.domain.ResultadoPropuesta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PropuestaTextoAdapterTest {

    @Test
    @DisplayName("Lee un bloque completo y descarta el texto ilegible entero")
    void leeODescarta() throws Exception {
        String valido = "texto: La oferta pide Java" + System.lineSeparator() + "fragmento: Java";
        ResultadoPropuesta leido = PropuestaTextoAdapter.leer(valido);

        assertThat(leido).isInstanceOf(ResultadoPropuesta.Disponible.class);
        assertThat(((ResultadoPropuesta.Disponible) leido).observaciones()).hasSize(1);

        assertThat(PropuestaTextoAdapter.leer("   ")).isEqualTo(new ResultadoPropuesta.NoDisponible(ResultadoPropuesta.Motivo.ILEGIBLE));
        assertThat(PropuestaTextoAdapter.leer("texto: solo esto")).isEqualTo(
                new ResultadoPropuesta.NoDisponible(ResultadoPropuesta.Motivo.ILEGIBLE)
        );
        String aMedias = valido + System.lineSeparator() + System.lineSeparator() + "texto: trozo";
        ResultadoPropuesta ilegible = PropuestaTextoAdapter.leer(aMedias);
        assertThat(ilegible).isEqualTo(new ResultadoPropuesta.NoDisponible(ResultadoPropuesta.Motivo.ILEGIBLE));
        assertThat(ilegible).isNotInstanceOf(ResultadoPropuesta.Disponible.class);

        String fuente = Files.readString(Path.of(
                "src/main/java/com/gustavo/jobagent/opportunities/adapter/out/lenguaje/PropuestaTextoAdapter.java"
        ));
        assertThat(fuente).doesNotContain("java.net");
        assertThat(fuente).doesNotContain("http");
    }
}
