package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.OfertaRegistrada;
import com.gustavo.jobagent.opportunities.application.port.out.OfertaLaboralRepository;
import com.gustavo.jobagent.opportunities.domain.IdentidadOferta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class RegistrarOfertaServiceTest {

    @Test
    @DisplayName("Diez altas de la misma dirección dejan una oferta y diez procedencias")
    void diezAltasDejanUnaOferta() {
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        RegistrarOfertaService service = new RegistrarOfertaService(repositorio);
        OfertaLaboral primera = oferta("https://example.com/jobs/1", "Backend");

        OfertaRegistrada resultado = null;
        for (int i = 0; i < 10; i++) {
            resultado = service.registrar(oferta("https://example.com/jobs/1", "Backend " + i));
        }

        assertThat(repositorio.ofertas).hasSize(1);
        assertThat(resultado.procedencias()).hasSize(10);
        assertThat(resultado.vigente().titulo()).isEqualTo("Backend 9");
        assertThat(resultado.vigente().salario()).isNull();
    }

    @Test
    @DisplayName("Otra fuente u otra URL crean otra oferta")
    void identidadesDistintasNoSeFusionan() {
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        RegistrarOfertaService service = new RegistrarOfertaService(repositorio);

        service.registrar(oferta("SIMULADA", "https://example.com/jobs/1", "Backend"));
        service.registrar(oferta("OTRA", "https://example.com/jobs/1", "Backend"));
        service.registrar(oferta("SIMULADA", "https://example.com/jobs/2", "Backend"));

        assertThat(repositorio.ofertas).hasSize(3);
    }

    @Test
    @DisplayName("Sin dirección pública no escribe")
    void sinUrlNoEscribe() {
        OfertaLaboralRepository repositorio = mock(OfertaLaboralRepository.class);
        RegistrarOfertaService service = new RegistrarOfertaService(repositorio);

        assertThatThrownBy(() -> service.registrar(OfertaLaboral.builder()
                .idFuente("1")
                .fuenteId("SIMULADA")
                .titulo("Backend")
                .build()))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repositorio);
    }

    private static OfertaLaboral oferta(String url, String titulo) {
        return oferta("SIMULADA", url, titulo);
    }

    private static OfertaLaboral oferta(String fuenteId, String url, String titulo) {
        return OfertaLaboral.builder()
                .idFuente("id-" + titulo)
                .fuenteId(fuenteId)
                .titulo(titulo)
                .urlOriginal(url)
                .build();
    }

    private static final class RepositorioEnMemoria implements OfertaLaboralRepository {
        private final ConcurrentHashMap<String, List<OfertaRegistrada.ProcedenciaOferta>> procedencias = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, OfertaLaboral> ofertas = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, String> ids = new ConcurrentHashMap<>();

        @Override
        public OfertaRegistrada insertarNueva(OfertaLaboral captura, IdentidadOferta identidad, Instant capturadoEn) {
            String clave = identidad.fuenteId() + "|" + identidad.urlNormalizada();
            ids.putIfAbsent(clave, "id-" + ids.size());
            ofertas.put(clave, captura);
            procedencias.computeIfAbsent(clave, ignorada -> new ArrayList<>())
                    .add(new OfertaRegistrada.ProcedenciaOferta(capturadoEn, captura));
            return armar(clave, identidad);
        }

        @Override
        public OfertaRegistrada agregarProcedencia(IdentidadOferta identidad, OfertaLaboral captura, Instant capturadoEn) {
            return insertarNueva(captura, identidad, capturadoEn);
        }

        @Override
        public Optional<OfertaRegistrada> buscarPorId(String id) {
            return Optional.empty();
        }

        @Override
        public Optional<OfertaRegistrada> buscarPorIdentidad(IdentidadOferta identidad) {
            String clave = identidad.fuenteId() + "|" + identidad.urlNormalizada();
            if (!ofertas.containsKey(clave)) {
                return Optional.empty();
            }
            return Optional.of(armar(clave, identidad));
        }

        private OfertaRegistrada armar(String clave, IdentidadOferta identidad) {
            return new OfertaRegistrada(ids.get(clave), identidad, ofertas.get(clave), procedencias.get(clave));
        }
    }
}
