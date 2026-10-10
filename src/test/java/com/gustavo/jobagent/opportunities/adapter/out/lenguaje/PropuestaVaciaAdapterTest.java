package com.gustavo.jobagent.opportunities.adapter.out.lenguaje;

import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import com.gustavo.jobagent.opportunities.domain.ResultadoPropuesta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PropuestaVaciaAdapterTest {

    @Test
    @DisplayName("Devuelve una propuesta vacía y no abre una conexión")
    void vaciaSinConexion() throws Exception {
        ResultadoPropuesta resultado = new PropuestaVaciaAdapter().proponer(oferta(), perfil(), Duration.ofSeconds(2));

        assertThat(resultado).isEqualTo(new ResultadoPropuesta.Disponible(java.util.List.of()));
        String fuente = Files.readString(Path.of(
                "src/main/java/com/gustavo/jobagent/opportunities/adapter/out/lenguaje/PropuestaVaciaAdapter.java"
        ));
        assertThat(fuente).doesNotContain("java.net");
        assertThat(fuente).doesNotContain("http");
    }

    private static OfertaLaboral oferta() {
        return OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Backend Java Engineer")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(Set.of("Java"))
                .build();
    }

    private static PerfilProfesional perfil() {
        return PerfilProfesional.builder()
                .id("perf-100")
                .nombre("Gustavo")
                .tituloPrincipal("Java Developer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .tecnologias(Set.of("Java"))
                .build();
    }
}
