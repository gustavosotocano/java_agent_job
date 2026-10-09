package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.application.port.in.ConsultarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.OfertaRegistrada;
import com.gustavo.jobagent.opportunities.application.port.in.RegistrarOfertaUseCase;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class OfertaRegistradaPostgresTest {

    @Test
    @DisplayName("Diez capturas dejan una oferta tras reiniciar y dos portales siguen siendo dos")
    void reimportarNoDuplicaYSobreviveAlReinicio() {
        String idUnica;
        try (ConfigurableApplicationContext contexto = ContextosDePersistencia.arrancar()) {
            RegistrarOfertaUseCase registrar = contexto.getBean(RegistrarOfertaUseCase.class);
            for (int i = 0; i < 10; i++) {
                registrar.registrar(oferta("SIMULADA", "https://example.com/jobs/1", "Backend " + i));
            }
            OfertaRegistrada otraFuente = registrar.registrar(oferta("OTRA", "https://example.com/jobs/1", "Backend 0"));
            OfertaRegistrada unica = contexto.getBean(ConsultarOfertaUseCase.class)
                    .porDireccion("SIMULADA", "https://example.com/jobs/1")
                    .orElseThrow();
            idUnica = unica.id();
            assertThat(unica.procedencias()).hasSize(10);
            assertThat(unica.vigente().salario()).isNull();
            assertThat(otraFuente.id()).isNotEqualTo(idUnica);
        }

        try (ConfigurableApplicationContext contexto = ContextosDePersistencia.arrancar()) {
            ConsultarOfertaUseCase consultar = contexto.getBean(ConsultarOfertaUseCase.class);
            OfertaRegistrada unica = consultar.porId(idUnica).orElseThrow();
            assertThat(unica.procedencias()).hasSize(10);
            assertThat(unica.vigente().titulo()).isEqualTo("Backend 9");
            assertThat(consultar.porDireccion("OTRA", "https://example.com/jobs/1")).isPresent();
            assertThat(consultar.porDireccion("SIMULADA", "https://example.com/jobs/1").orElseThrow().id())
                    .isNotEqualTo(consultar.porDireccion("OTRA", "https://example.com/jobs/1").orElseThrow().id());
        }
    }

    @Test
    @DisplayName("Dos altas simultáneas de la misma identidad dejan una oferta y dos procedencias")
    void altaSimultaneaDejaUnaOferta() throws Exception {
        try (ConfigurableApplicationContext contexto = ContextosDePersistencia.arrancar()) {
            RegistrarOfertaUseCase registrar = contexto.getBean(RegistrarOfertaUseCase.class);
            OfertaLaboral captura = oferta("SIMULADA", "https://example.com/jobs/simultanea", "Backend");
            ExecutorService pool = Executors.newFixedThreadPool(2);
            CountDownLatch inicio = new CountDownLatch(1);
            try {
                Future<OfertaRegistrada> primera = pool.submit(() -> {
                    inicio.await();
                    return registrar.registrar(captura);
                });
                Future<OfertaRegistrada> segunda = pool.submit(() -> {
                    inicio.await();
                    return registrar.registrar(captura);
                });
                inicio.countDown();
                assertThat(primera.get().id()).isEqualTo(segunda.get().id());
            } finally {
                pool.shutdownNow();
            }
            OfertaRegistrada guardada = contexto.getBean(ConsultarOfertaUseCase.class)
                    .porDireccion("SIMULADA", "https://example.com/jobs/simultanea")
                    .orElseThrow();
            assertThat(guardada.procedencias()).hasSize(2);
        }
    }

    private static OfertaLaboral oferta(String fuenteId, String url, String titulo) {
        return OfertaLaboral.builder()
                .idFuente("src-" + titulo)
                .fuenteId(fuenteId)
                .titulo(titulo)
                .urlOriginal(url)
                .build();
    }
}
