package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.FuentesNoDisponiblesException;
import com.gustavo.jobagent.opportunities.application.port.in.RegistroBusqueda;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoBusquedaOfertas;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoRegistroBusqueda;
import com.gustavo.jobagent.opportunities.application.port.out.BuscarOfertasPort;
import com.gustavo.jobagent.opportunities.application.port.out.RegistroBusquedaRepository;
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

    @Test
    @DisplayName("Registra el resultado de la búsqueda y no depende de un repositorio de ofertas")
    void registraBusquedaSinRegistrarOfertas() {
        RegistroCaptura registro = new RegistroCaptura();
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("1").fuenteId("SIM").titulo("Backend Java").urlOriginal("https://example.com/1").build();
        BuscarOfertasService service = new BuscarOfertasService(
                List.of(new FakeBuscarOfertasPort("SIM", true, List.of(oferta))),
                registro
        );

        service.buscar(new CriteriosBusqueda("Backend"));
        service.buscar(new CriteriosBusqueda("Nada-que-no-existe"));

        assertThat(registro.guardados).extracting(RegistroBusqueda::resultado)
                .containsExactly(ResultadoRegistroBusqueda.CON_RESULTADOS, ResultadoRegistroBusqueda.RESULTADO_VACIO);
    }

    @Test
    @DisplayName("Clasifica fallo parcial, fallo total y ausencia de fuentes")
    void clasificaResultadosDeRegistro() {
        RegistroCaptura registro = new RegistroCaptura();
        BuscarOfertasPort exitosa = new FakeBuscarOfertasPort("OK", true, List.of(
                OfertaLaboral.builder().idFuente("1").fuenteId("OK").titulo("Backend").build()));
        BuscarOfertasPort fallida = new BuscarOfertasPort() {
            @Override
            public String fuenteId() {
                return "FALLA";
            }

            @Override
            public boolean estaHabilitada() {
                return true;
            }

            @Override
            public List<OfertaLaboral> buscar(CriteriosBusqueda criterios) {
                throw new RuntimeException("caida");
            }
        };

        new BuscarOfertasService(List.of(exitosa, fallida), registro).buscar(new CriteriosBusqueda("Backend"));
        assertThatThrownBy(() -> new BuscarOfertasService(List.of(fallida), registro).buscar(new CriteriosBusqueda("Backend")))
                .isInstanceOf(FuentesNoDisponiblesException.class);
        assertThatThrownBy(() -> new BuscarOfertasService(List.of(new FakeBuscarOfertasPort("OFF", false, List.of())), registro)
                .buscar(new CriteriosBusqueda("Backend")))
                .isInstanceOf(FuentesNoDisponiblesException.class);

        assertThat(registro.guardados).extracting(RegistroBusqueda::resultado).containsExactly(
                ResultadoRegistroBusqueda.FALLO_PARCIAL,
                ResultadoRegistroBusqueda.BUSQUEDA_FALLIDA,
                ResultadoRegistroBusqueda.SIN_FUENTES_HABILITADAS
        );
        assertThat(registro.guardados.get(2).advertencias()).isEmpty();
        assertThat(registro.guardados.get(2).fuentesConsultadas()).isEmpty();
    }

    @Test
    @DisplayName("Un fallo parcial deja las ofertas válidas y exactamente una advertencia")
    void aislamientoDejaResultadosYUnaAdvertencia() {
        RegistroCaptura registro = new RegistroCaptura();
        BuscarOfertasPort exitosa = new FakeBuscarOfertasPort("OK", true, List.of(
                OfertaLaboral.builder().idFuente("1").fuenteId("OK").titulo("Backend").build()));
        BuscarOfertasPort fallida = new BuscarOfertasPort() {
            @Override
            public String fuenteId() {
                return "FALLA";
            }

            @Override
            public boolean estaHabilitada() {
                return true;
            }

            @Override
            public List<OfertaLaboral> buscar(CriteriosBusqueda criterios) {
                throw new RuntimeException("caida");
            }
        };

        ResultadoBusquedaOfertas resultado = new BuscarOfertasService(List.of(exitosa, fallida), registro)
                .buscar(new CriteriosBusqueda("Backend"));

        assertThat(resultado.ofertas()).extracting(OfertaLaboral::fuenteId).containsExactly("OK");
        assertThat(resultado.advertencias()).hasSize(1);
        assertThat(resultado.advertencias().getFirst().fuenteId()).isEqualTo("FALLA");
        assertThat(registro.guardados).hasSize(1);
        assertThat(registro.guardados.getFirst().resultado()).isEqualTo(ResultadoRegistroBusqueda.FALLO_PARCIAL);
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

    private static final class RegistroCaptura implements RegistroBusquedaRepository {
        private final List<RegistroBusqueda> guardados = new java.util.ArrayList<>();

        @Override
        public RegistroBusqueda guardar(RegistroBusqueda registro) {
            guardados.add(registro);
            return registro;
        }

        @Override
        public java.util.Optional<RegistroBusqueda> buscarPorId(String id) {
            return guardados.stream().filter(registro -> registro.id().equals(id)).findFirst();
        }
    }
}
