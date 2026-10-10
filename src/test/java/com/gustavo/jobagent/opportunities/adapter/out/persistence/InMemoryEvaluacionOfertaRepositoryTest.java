package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.domain.EstadoEvaluacion;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryEvaluacionOfertaRepositoryTest {

    private InMemoryEvaluacionOfertaRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEvaluacionOfertaRepository();
    }

    @Test
    @DisplayName("Debe guardar y recuperar una evaluación por ID")
    void debeGuardarYBuscarPorId() {
        EvaluacionOferta evaluacion = new EvaluacionOferta(
                "eval-1",
                "of-1",
                "perf-1",
                EstadoEvaluacion.COMPATIBLE,
                List.of(),
                List.of(),
                List.of(),
                Instant.now(),
                List.of()
        );

        repository.guardar(evaluacion);
        Optional<EvaluacionOferta> encontrada = repository.buscarPorId("eval-1");

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().id()).isEqualTo("eval-1");
        assertThat(encontrada.get().idOferta()).isEqualTo("of-1");
    }

    @Test
    @DisplayName("Debe buscar evaluaciones por perfil y por oferta")
    void debeBuscarPorPerfilYOferta() {
        EvaluacionOferta ev1 = new EvaluacionOferta("e1", "of-1", "perf-A", EstadoEvaluacion.COMPATIBLE, List.of(), List.of(), List.of(), Instant.now(), List.of());
        EvaluacionOferta ev2 = new EvaluacionOferta("e2", "of-2", "perf-A", EstadoEvaluacion.COMPATIBLE_CON_BRECHAS, List.of(), List.of(), List.of(), Instant.now(), List.of());
        EvaluacionOferta ev3 = new EvaluacionOferta("e3", "of-1", "perf-B", EstadoEvaluacion.NO_COMPATIBLE, List.of(), List.of(), List.of(), Instant.now(), List.of());

        repository.guardar(ev1);
        repository.guardar(ev2);
        repository.guardar(ev3);

        List<EvaluacionOferta> porPerfilA = repository.buscarPorPerfil("perf-A");
        assertThat(porPerfilA).hasSize(2).extracting(EvaluacionOferta::id).containsExactlyInAnyOrder("e1", "e2");

        List<EvaluacionOferta> porOferta1 = repository.buscarPorOferta("of-1");
        assertThat(porOferta1).hasSize(2).extracting(EvaluacionOferta::id).containsExactlyInAnyOrder("e1", "e3");
    }

    @Test
    @DisplayName("Debe retornar vacío si id o filtros son nulos o inexistentes")
    void debeManejarCasosVacios() {
        assertThat(repository.buscarPorId("inexistente")).isEmpty();
        assertThat(repository.buscarPorId(null)).isEmpty();
        assertThat(repository.buscarPorPerfil(null)).isEmpty();
        assertThat(repository.buscarPorOferta(null)).isEmpty();

        assertThatThrownBy(() -> repository.guardar(null))
                .isInstanceOf(NullPointerException.class);
    }
}
