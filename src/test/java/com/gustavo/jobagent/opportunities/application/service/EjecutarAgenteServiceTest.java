package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.BuscarOfertasUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.ConsultarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.EjecucionRealizada;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.OfertaRegistrada;
import com.gustavo.jobagent.opportunities.application.port.in.PasoRealizado;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoBusquedaOfertas;
import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import com.gustavo.jobagent.opportunities.domain.EjecucionAcotada;
import com.gustavo.jobagent.opportunities.domain.EstadoEvaluacion;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.IdentidadOferta;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class EjecutarAgenteServiceTest {

    private final AtomicInteger busquedas = new AtomicInteger();
    private final AtomicInteger evaluaciones = new AtomicInteger();
    private final List<PerfilProfesional> perfilesCreados = new ArrayList<>();
    private OfertaLaboral ofertaRegistrada;
    private EjecutarAgenteService service;

    @BeforeEach
    void setUp() {
        ofertaRegistrada = OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Backend Java Engineer")
                .descripcion("Buscamos Java")
                .modalidad(ModalidadTrabajo.REMOTE)
                .urlOriginal("https://example.com/jobs/sim-001")
                .tecnologias(Set.of("Java"))
                .build();
        OfertaRegistrada registrada = new OfertaRegistrada(
                "of-1",
                IdentidadOferta.de("SIMULADA", ofertaRegistrada.urlOriginal()),
                ofertaRegistrada,
                List.of()
        );
        BuscarOfertasUseCase buscar = criterios -> {
            busquedas.incrementAndGet();
            return new ResultadoBusquedaOfertas(List.of(ofertaRegistrada), List.of("SIMULADA"), List.of());
        };
        ConsultarOfertaUseCase consultar = new ConsultarOfertaUseCase() {
            @Override
            public Optional<OfertaRegistrada> porId(String id) {
                return "of-1".equals(id) ? Optional.of(registrada) : Optional.empty();
            }

            @Override
            public Optional<OfertaRegistrada> porDireccion(String fuenteId, String urlOriginal) {
                return Optional.empty();
            }
        };
        EvaluarOfertaUseCase evaluar = comando -> {
            evaluaciones.incrementAndGet();
            return EvaluacionOferta.evaluar(comando.oferta(), comando.perfil());
        };
        service = new EjecutarAgenteService(buscar, consultar, evaluar);
    }

    @Test
    @DisplayName("La búsqueda corre y un paso prohibido detiene el resto sin registrar ni evaluar")
    void busquedaYPasoProhibido() {
        int ofertasAntes = 0;
        int evaluacionesAntes = 0;

        EjecucionRealizada ejecucion = service.ejecutar(perfil(), List.of(
                new EjecucionAcotada.Solicitud("buscarOfertas", new CriteriosBusqueda("Backend"), null),
                new EjecucionAcotada.Solicitud("postular", null, null),
                new EjecucionAcotada.Solicitud("guardarEvaluacion", null, "of-1")
        ));

        assertThat(busquedas).hasValue(1);
        assertThat(evaluaciones).hasValue(0);
        assertThat(ofertasAntes).isZero();
        assertThat(evaluacionesAntes).isZero();
        assertThat(ejecucion.estado()).isEqualTo(EjecucionAcotada.EstadoEjecucion.HERRAMIENTA_NO_AUTORIZADA);
        assertThat(ejecucion.realizados()).extracting(PasoRealizado::herramienta).containsExactly("buscarOfertas", "postular");
        assertThat(ejecucion.realizados().get(1).autorizada()).isFalse();
        assertThat(ejecucion.noEjecutados()).containsExactly("guardarEvaluacion");
    }

    @Test
    @DisplayName("Cinco consultas ejecutan cuatro y una lista vacía no llama a nadie")
    void maximoYListaVacia() {
        List<EjecucionAcotada.Solicitud> cinco = List.of(
                new EjecucionAcotada.Solicitud("consultarPerfil", null, "perf-100"),
                new EjecucionAcotada.Solicitud("consultarPerfil", null, "perf-100"),
                new EjecucionAcotada.Solicitud("consultarPerfil", null, "perf-100"),
                new EjecucionAcotada.Solicitud("consultarPerfil", null, "perf-100"),
                new EjecucionAcotada.Solicitud("consultarPerfil", null, "perf-100")
        );

        EjecucionRealizada limitada = service.ejecutar(perfil(), cinco);

        assertThat(limitada.estado()).isEqualTo(EjecucionAcotada.EstadoEjecucion.LIMITE_ALCANZADO);
        assertThat(limitada.realizados()).hasSize(4);
        assertThat(limitada.noEjecutados()).containsExactly("consultarPerfil");
        assertThat(busquedas).hasValue(0);
        assertThat(evaluaciones).hasValue(0);

        EjecucionRealizada vacia = service.ejecutar(perfil(), List.of());

        assertThat(vacia.estado()).isEqualTo(EjecucionAcotada.EstadoEjecucion.COMPLETADA);
        assertThat(vacia.realizados()).isEmpty();
        assertThat(vacia.noEjecutados()).isEmpty();
        assertThat(busquedas).hasValue(0);
        assertThat(evaluaciones).hasValue(0);
    }

    @Test
    @DisplayName("Consulta el perfil y la oferta conocidos y guarda el estado del contraste")
    void consultaYGuardaSinInventar() {
        PerfilProfesional perfil = perfil();
        EvaluacionOferta esperada = EvaluacionOferta.evaluar(ofertaRegistrada, perfil);

        EjecucionRealizada ejecucion = service.ejecutar(perfil, List.of(
                new EjecucionAcotada.Solicitud("consultarPerfil", null, "perf-100"),
                new EjecucionAcotada.Solicitud("consultarPerfil", null, "otro"),
                new EjecucionAcotada.Solicitud("consultarOferta", null, "of-1"),
                new EjecucionAcotada.Solicitud("consultarOferta", null, "no-esta")
        ));

        assertThat(ejecucion.realizados()).extracting(PasoRealizado::resumen)
                .containsExactly("perfil encontrado", "perfil desconocido", "oferta encontrada", "oferta desconocida");
        assertThat(perfilesCreados).isEmpty();
        assertThat(evaluaciones).hasValue(0);

        EjecucionRealizada guardada = service.ejecutar(perfil, List.of(
                new EjecucionAcotada.Solicitud("guardarEvaluacion", null, "of-1")
        ));
        EjecucionRealizada sinOferta = service.ejecutar(perfil, List.of(
                new EjecucionAcotada.Solicitud("guardarEvaluacion", null, "no-esta")
        ));

        assertThat(guardada.realizados().getFirst().resumen()).isEqualTo("evaluación " + esperada.estado());
        assertThat(esperada.estado()).isEqualTo(EstadoEvaluacion.COMPATIBLE);
        assertThat(sinOferta.realizados().getFirst().resumen()).isEqualTo("oferta no registrada");
        assertThat(evaluaciones).hasValue(1);
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
