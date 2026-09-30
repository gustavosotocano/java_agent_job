package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaCommand;
import com.gustavo.jobagent.opportunities.application.port.out.EvaluacionOfertaRepository;
import com.gustavo.jobagent.opportunities.domain.EstadoEvaluacion;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluarOfertaServiceTest {

    @Mock
    private EvaluacionOfertaRepository repository;

    private EvaluarOfertaService service;

    @BeforeEach
    void setUp() {
        service = new EvaluarOfertaService(repository);
    }

    @Test
    @DisplayName("Debe orquestar la evaluación de dominio y persistir el aggregate root")
    void debeEvaluarYPersistir() {
        OfertaLaboral oferta = OfertaLaboral.builder()
                .idFuente("sim-001")
                .fuenteId("SIMULADA")
                .titulo("Backend Java Engineer")
                .modalidad(ModalidadTrabajo.REMOTE)
                .tecnologias(Set.of("Java", "Spring Boot"))
                .build();

        PerfilProfesional perfil = PerfilProfesional.builder()
                .id("perf-100")
                .nombre("Gustavo")
                .tituloPrincipal("Java Developer")
                .modalidadesAceptadas(Set.of(ModalidadTrabajo.REMOTE))
                .tecnologias(Set.of("Java", "Spring Boot"))
                .build();

        when(repository.guardar(any(EvaluacionOferta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvaluarOfertaCommand comando = new EvaluarOfertaCommand(oferta, perfil);
        EvaluacionOferta resultado = service.evaluar(comando);

        assertThat(resultado).isNotNull();
        assertThat(resultado.idOferta()).isEqualTo("sim-001");
        assertThat(resultado.idPerfil()).isEqualTo("perf-100");
        assertThat(resultado.estado()).isEqualTo(EstadoEvaluacion.COMPATIBLE);

        ArgumentCaptor<EvaluacionOferta> captor = ArgumentCaptor.forClass(EvaluacionOferta.class);
        verify(repository).guardar(captor.capture());
        assertThat(captor.getValue().estado()).isEqualTo(EstadoEvaluacion.COMPATIBLE);
    }

    @Test
    @DisplayName("Debe rechazar comando nulo")
    void debeRechazarComandoNulo() {
        assertThatThrownBy(() -> service.evaluar(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("comando de evaluación no puede ser nulo");
    }
}
