package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.adapter.out.lenguaje.PropuestaTextoAdapter;
import com.gustavo.jobagent.opportunities.adapter.out.lenguaje.PropuestaVaciaAdapter;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.InMemoryEvaluacionOfertaRepository;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaCommand;
import com.gustavo.jobagent.opportunities.application.port.out.PropuestaLenguajePort;
import com.gustavo.jobagent.opportunities.domain.EstadoEvaluacion;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.ObservacionPropuesta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import com.gustavo.jobagent.opportunities.domain.ResultadoPropuesta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluarOfertaPropuestaServiceTest {

    @Test
    @DisplayName("Propuesta vacía, válida o fallida dejan el mismo cálculo y se guardan")
    void tresResultadosConservanElCalculo() {
        OfertaLaboral oferta = oferta();
        PerfilProfesional perfil = perfil();
        ObservacionPropuesta citada = new ObservacionPropuesta("La oferta pide Java", "Java");
        InMemoryEvaluacionOfertaRepository repositorio = new InMemoryEvaluacionOfertaRepository();

        EvaluacionOferta vacia = evaluar(repositorio, new PropuestaVaciaAdapter(), Duration.ofSeconds(2), oferta, perfil);
        EvaluacionOferta valida = evaluar(
                repositorio,
                (o, p, limite) -> new ResultadoPropuesta.Disponible(List.of(citada)),
                Duration.ofSeconds(2),
                oferta,
                perfil
        );
        EvaluacionOferta fallida = evaluar(
                repositorio,
                (o, p, limite) -> {
                    throw new IllegalStateException("proveedor caído");
                },
                Duration.ofSeconds(2),
                oferta,
                perfil
        );

        assertThat(vacia.estado()).isEqualTo(EstadoEvaluacion.COMPATIBLE);
        assertThat(valida.estado()).isEqualTo(vacia.estado());
        assertThat(fallida.estado()).isEqualTo(vacia.estado());
        assertThat(valida.coincidencias()).isEqualTo(vacia.coincidencias());
        assertThat(fallida.coincidencias()).isEqualTo(vacia.coincidencias());
        assertThat(valida.brechas()).isEqualTo(vacia.brechas());
        assertThat(fallida.brechas()).isEqualTo(vacia.brechas());
        assertThat(valida.datosPendientes()).isEqualTo(vacia.datosPendientes());
        assertThat(fallida.datosPendientes()).isEqualTo(vacia.datosPendientes());
        assertThat(vacia.observaciones()).isEmpty();
        assertThat(valida.observaciones()).containsExactly(citada);
        assertThat(fallida.observaciones()).isEmpty();
        assertThat(repositorio.buscarPorPerfil("perf-100")).hasSize(3);
    }

    @Test
    @DisplayName("Sustituir el adaptador no cambia el estado y un puerto lento deja la propuesta vacía")
    void intercambiarYAgotarTiempo() {
        OfertaLaboral oferta = oferta();
        PerfilProfesional perfil = perfil();
        InMemoryEvaluacionOfertaRepository repositorio = new InMemoryEvaluacionOfertaRepository();
        String estructurado = "texto: La oferta pide Java" + System.lineSeparator() + "fragmento: Java";

        EvaluacionOferta vacia = evaluar(repositorio, new PropuestaVaciaAdapter(), Duration.ofSeconds(2), oferta, perfil);
        EvaluacionOferta texto = evaluar(
                repositorio,
                new PropuestaTextoAdapter(estructurado),
                Duration.ofSeconds(2),
                oferta,
                perfil
        );
        EvaluacionOferta lenta = evaluar(
                repositorio,
                (o, p, limite) -> {
                    try {
                        Thread.sleep(2_000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return new ResultadoPropuesta.Disponible(List.of(
                            new ObservacionPropuesta("La oferta pide Java", "Java")
                    ));
                },
                Duration.ofMillis(200),
                oferta,
                perfil
        );

        assertThat(texto.estado()).isEqualTo(vacia.estado());
        assertThat(lenta.estado()).isEqualTo(vacia.estado());
        assertThat(vacia.observaciones()).isEmpty();
        assertThat(texto.observaciones()).hasSize(1);
        assertThat(lenta.observaciones()).isEmpty();
        assertThat(repositorio.buscarPorId(lenta.id())).get().extracting(EvaluacionOferta::observaciones).isEqualTo(List.of());
    }

    private static EvaluacionOferta evaluar(
            InMemoryEvaluacionOfertaRepository repositorio,
            PropuestaLenguajePort puerto,
            Duration limite,
            OfertaLaboral oferta,
            PerfilProfesional perfil
    ) {
        return new EvaluarOfertaService(repositorio, puerto, limite).evaluar(new EvaluarOfertaCommand(oferta, perfil));
    }

    private static OfertaLaboral oferta() {
        return OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Backend Java Engineer")
                .descripcion("Buscamos Java")
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
