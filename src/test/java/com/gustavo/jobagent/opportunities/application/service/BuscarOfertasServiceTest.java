package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.FuentesNoDisponiblesException;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoBusquedaOfertas;
import com.gustavo.jobagent.opportunities.application.port.out.BuscarOfertasPort;
import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuscarOfertasServiceTest {

    @Test
    @DisplayName("Debe orquestar búsqueda en fuentes habilitadas, deduplicar y ordenar determinísticamente")
    void debeOrquestarBusquedaDeduplicarYOrdenar() {
        OfertaLaboral oferta1 = OfertaLaboral.builder()
                .idFuente("100")
                .fuenteId("FUENTE_B")
                .titulo("Backend Developer")
                .empresa("Corp B")
                .build();

        OfertaLaboral oferta2 = OfertaLaboral.builder()
                .idFuente("050")
                .fuenteId("FUENTE_A")
                .titulo("Backend Developer")
                .empresa("Corp A")
                .build();

        OfertaLaboral ofertaDuplicada = OfertaLaboral.builder()
                .idFuente("050")
                .fuenteId("FUENTE_A")
                .titulo("Backend Developer")
                .empresa("Corp A (duplicada)")
                .build();

        BuscarOfertasPort fuenteA = new FakeBuscarOfertasPort("FUENTE_A", true, List.of(oferta2, ofertaDuplicada));
        BuscarOfertasPort fuenteB = new FakeBuscarOfertasPort("FUENTE_B", true, List.of(oferta1));
        BuscarOfertasPort fuenteDeshabilitada = new FakeBuscarOfertasPort("FUENTE_OFF", false, List.of());

        BuscarOfertasService service = new BuscarOfertasService(List.of(fuenteA, fuenteB, fuenteDeshabilitada));

        ResultadoBusquedaOfertas resultado = service.buscar(new CriteriosBusqueda("Backend", List.of(), null, null, 10));

        assertThat(resultado.fuentesConsultadas()).containsExactly("FUENTE_A", "FUENTE_B");
        assertThat(resultado.advertencias()).isEmpty();
        assertThat(resultado.ofertas()).hasSize(2);

        // Ordenado por fuenteId y luego por idFuente
        assertThat(resultado.ofertas().get(0).fuenteId()).isEqualTo("FUENTE_A");
        assertThat(resultado.ofertas().get(0).idFuente()).isEqualTo("050");
        assertThat(resultado.ofertas().get(1).fuenteId()).isEqualTo("FUENTE_B");
        assertThat(resultado.ofertas().get(1).idFuente()).isEqualTo("100");
    }

    @Test
    @DisplayName("Debe manejar fallos parciales agregando advertencias y devolviendo las fuentes exitosas")
    void debeManejarFallosParcialesConAdvertencias() {
        OfertaLaboral ofertaA = OfertaLaboral.builder()
                .idFuente("001")
                .fuenteId("FUENTE_A")
                .titulo("Java Backend Engineer")
                .build();

        BuscarOfertasPort fuenteA = new FakeBuscarOfertasPort("FUENTE_A", true, List.of(ofertaA));
        BuscarOfertasPort fuenteFallida = new BuscarOfertasPort() {
            @Override
            public String fuenteId() {
                return "FUENTE_FALLIDA";
            }

            @Override
            public boolean estaHabilitada() {
                return true;
            }

            @Override
            public List<OfertaLaboral> buscar(CriteriosBusqueda criterios) {
                throw new RuntimeException("Error de conexión timeout");
            }
        };

        BuscarOfertasService service = new BuscarOfertasService(List.of(fuenteA, fuenteFallida));

        ResultadoBusquedaOfertas resultado = service.buscar(new CriteriosBusqueda("Backend"));

        assertThat(resultado.fuentesConsultadas()).containsExactly("FUENTE_A");
        assertThat(resultado.advertencias()).hasSize(1);
        assertThat(resultado.advertencias().getFirst().fuenteId()).isEqualTo("FUENTE_FALLIDA");
        assertThat(resultado.advertencias().getFirst().mensaje()).contains("timeout");
        assertThat(resultado.ofertas()).hasSize(1);
    }

    @Test
    @DisplayName("Debe lanzar FuentesNoDisponiblesException si no hay fuentes habilitadas")
    void debeLanzarExcepcionSiNoHayFuentesHabilitadas() {
        BuscarOfertasPort fuenteOff = new FakeBuscarOfertasPort("FUENTE_OFF", false, List.of());
        BuscarOfertasService service = new BuscarOfertasService(List.of(fuenteOff));

        assertThatThrownBy(() -> service.buscar(new CriteriosBusqueda("Backend")))
                .isInstanceOf(FuentesNoDisponiblesException.class)
                .hasMessageContaining("No hay fuentes de ofertas disponibles o habilitadas");
    }

    @Test
    @DisplayName("Debe lanzar FuentesNoDisponiblesException si todas las fuentes consultadas fallan")
    void debeLanzarExcepcionSiTodasLasFuentesFallan() {
        BuscarOfertasPort fuenteFallida = new BuscarOfertasPort() {
            @Override
            public String fuenteId() {
                return "FUENTE_FALLIDA";
            }

            @Override
            public boolean estaHabilitada() {
                return true;
            }

            @Override
            public List<OfertaLaboral> buscar(CriteriosBusqueda criterios) {
                throw new RuntimeException("Error 500");
            }
        };

        BuscarOfertasService service = new BuscarOfertasService(List.of(fuenteFallida));

        assertThatThrownBy(() -> service.buscar(new CriteriosBusqueda("Backend")))
                .isInstanceOf(FuentesNoDisponiblesException.class)
                .hasMessageContaining("Todas las fuentes consultadas fallaron");
    }

    @Test
    @DisplayName("Debe respetar el límite de resultados especificado en los criterios")
    void debeRespetarLimiteDeResultados() {
        List<OfertaLaboral> ofertas = List.of(
                OfertaLaboral.builder().idFuente("1").fuenteId("SIM").titulo("Backend 1").build(),
                OfertaLaboral.builder().idFuente("2").fuenteId("SIM").titulo("Backend 2").build(),
                OfertaLaboral.builder().idFuente("3").fuenteId("SIM").titulo("Backend 3").build()
        );

        BuscarOfertasPort fuente = new FakeBuscarOfertasPort("SIM", true, ofertas);
        BuscarOfertasService service = new BuscarOfertasService(List.of(fuente));

        ResultadoBusquedaOfertas resultado = service.buscar(new CriteriosBusqueda("Backend", List.of(), null, null, 2));

        assertThat(resultado.ofertas()).hasSize(2);
    }

    private static class FakeBuscarOfertasPort implements BuscarOfertasPort {
        private final String fuenteId;
        private final boolean habilitada;
        private final List<OfertaLaboral> catalogo;

        public FakeBuscarOfertasPort(String fuenteId, boolean habilitada, List<OfertaLaboral> catalogo) {
            this.fuenteId = fuenteId;
            this.habilitada = habilitada;
            this.catalogo = catalogo;
        }

        @Override
        public String fuenteId() {
            return fuenteId;
        }

        @Override
        public boolean estaHabilitada() {
            return habilitada;
        }

        @Override
        public List<OfertaLaboral> buscar(CriteriosBusqueda criterios) {
            return catalogo;
        }
    }
}
