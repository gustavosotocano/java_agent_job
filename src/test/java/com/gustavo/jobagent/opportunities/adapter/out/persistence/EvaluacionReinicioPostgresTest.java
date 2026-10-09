package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.application.port.in.ConsultarEvaluacionUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluacionAlmacenada;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaCommand;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaUseCase;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluacionReinicioPostgresTest {

    @Test
    @DisplayName("La evaluación y su evidencia sobreviven al reinicio y una segunda no pisa a la primera")
    void evaluacionSobreviveAlReinicio() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Senior Java Backend Engineer")
                .empresa("Empresa de ejemplo")
                .descripcion("Java y Spring Boot")
                .ubicacion("Colombia")
                .modalidad(ModalidadTrabajo.REMOTE)
                .urlOriginal("https://example.com/jobs/sim-001")
                .tecnologias(Set.of("Java", "Spring Boot"))
                .build();
        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-100")
                .nombre("Gustavo")
                .tituloPrincipal("Java Developer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .tecnologias(Set.of("Java", "Spring Boot"))
                .build();

        EvaluacionOferta primera;
        String idSegunda;
        try (ConfigurableApplicationContext contexto = ContextosDePersistencia.arrancar()) {
            EvaluarOfertaUseCase evaluar = contexto.getBean(EvaluarOfertaUseCase.class);
            primera = evaluar.evaluar(new EvaluarOfertaCommand(oferta, perfil));
            EvaluacionOferta segunda = evaluar.evaluar(new EvaluarOfertaCommand(oferta, perfil));
            idSegunda = segunda.id();
            assertThat(primera.id()).isNotEqualTo(idSegunda);
        }

        try (ConfigurableApplicationContext contexto = ContextosDePersistencia.arrancar()) {
            ConsultarEvaluacionUseCase consultar = contexto.getBean(ConsultarEvaluacionUseCase.class);
            EvaluacionAlmacenada almacenada = consultar.consultar(primera.id()).orElseThrow();
            assertThat(almacenada.evaluacion().id()).isEqualTo(primera.id());
            assertThat(almacenada.evaluacion().estado()).isEqualTo(primera.estado());
            assertThat(almacenada.evaluacion().coincidencias()).isEqualTo(primera.coincidencias());
            assertThat(almacenada.evaluacion().brechas()).isEqualTo(primera.brechas());
            assertThat(almacenada.evaluacion().datosPendientes()).isEqualTo(primera.datosPendientes());
            assertThat(almacenada.perfil().id()).isEqualTo("perf-100");
            assertThat(almacenada.oferta().salario()).isNull();
            assertThat(almacenada.oferta().titulo()).isEqualTo("Senior Java Backend Engineer");
            EvaluacionAlmacenada segunda = consultar.consultar(idSegunda).orElseThrow();
            assertThat(segunda.evaluacion().id()).isEqualTo(idSegunda);
            assertThat(consultar.consultar(primera.id()).orElseThrow().evaluacion()).isEqualTo(primera);
        }
    }
}
