package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.application.port.in.ConsultarEvaluacionUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaCommand;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaUseCase;
import com.gustavo.jobagent.opportunities.domain.EstadoEvaluacion;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluacionPropuestaPostgresTest {

    @Test
    @DisplayName("La propuesta aceptada y la vacía se leen igual tras reiniciar, sin reescribir la primera")
    void propuestaSobreviveAlReinicio() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-propuesta")
                .fuenteId("SIMULADA")
                .titulo("Backend Java Engineer")
                .descripcion("Buscamos Java")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(Set.of("Java"))
                .urlOriginal("https://example.com/jobs/sim-propuesta")
                .build();
        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-propuesta")
                .nombre("Gustavo")
                .tituloPrincipal("Java Developer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .tecnologias(Set.of("Java"))
                .build();
        String estructurado = "texto: La oferta pide Java" + System.lineSeparator() + "fragmento: Java";

        EvaluacionOferta primera;
        EvaluacionOferta segunda;
        try (ConfigurableApplicationContext contexto = ContextosDePersistencia.arrancar(
                "--jobagent.lenguaje.adaptador=texto",
                "--jobagent.lenguaje.texto=" + estructurado
        )) {
            EvaluarOfertaUseCase evaluar = contexto.getBean(EvaluarOfertaUseCase.class);
            primera = evaluar.evaluar(new EvaluarOfertaCommand(oferta, perfil));
            segunda = evaluar.evaluar(new EvaluarOfertaCommand(oferta, perfil));
        }

        EvaluacionOferta vacia;
        try (ConfigurableApplicationContext contexto = ContextosDePersistencia.arrancar()) {
            vacia = contexto.getBean(EvaluarOfertaUseCase.class).evaluar(new EvaluarOfertaCommand(oferta, perfil));
        }

        try (ConfigurableApplicationContext contexto = ContextosDePersistencia.arrancar()) {
            ConsultarEvaluacionUseCase consultar = contexto.getBean(ConsultarEvaluacionUseCase.class);
            EvaluacionOferta leida = consultar.consultar(primera.id()).orElseThrow().evaluacion();
            EvaluacionOferta leidaSegunda = consultar.consultar(segunda.id()).orElseThrow().evaluacion();
            EvaluacionOferta leidaVacia = consultar.consultar(vacia.id()).orElseThrow().evaluacion();

            assertThat(leida.estado()).isEqualTo(EstadoEvaluacion.COMPATIBLE);
            assertThat(leida.observaciones()).hasSize(1);
            assertThat(leida.observaciones().getFirst().fragmento()).isEqualTo("Java");
            assertThat(leidaSegunda.id()).isNotEqualTo(leida.id());
            assertThat(leidaSegunda.estado()).isEqualTo(leida.estado());
            assertThat(leidaSegunda.observaciones()).hasSize(1);
            assertThat(leidaVacia.estado()).isEqualTo(leida.estado());
            assertThat(leidaVacia.observaciones()).isEmpty();
            assertThat(leida.coincidencias()).isEqualTo(primera.coincidencias());
        }
    }
}
