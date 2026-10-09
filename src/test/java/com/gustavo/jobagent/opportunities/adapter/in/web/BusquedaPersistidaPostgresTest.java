package com.gustavo.jobagent.opportunities.adapter.in.web;

import com.gustavo.jobagent.opportunities.adapter.out.persistence.ContextosDePersistencia;
import com.gustavo.jobagent.opportunities.application.port.in.BuscarOfertasUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.ConsultarRegistroBusquedaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.FuenteConfigurada;
import com.gustavo.jobagent.opportunities.application.port.in.RegistroBusqueda;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoBusquedaOfertas;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoRegistroBusqueda;
import com.gustavo.jobagent.opportunities.application.port.out.BuscarOfertasPort;
import com.gustavo.jobagent.opportunities.application.port.out.FuenteConfiguradaRepository;
import com.gustavo.jobagent.opportunities.application.port.out.RegistroBusquedaRepository;
import com.gustavo.jobagent.opportunities.application.service.BuscarOfertasService;
import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusquedaPersistidaPostgresTest {

    @Test
    @DisplayName("Buscar por HTTP no registra ofertas ni evaluaciones y deja un registro legible")
    void buscarNoPersisteOfertas() throws Exception {
        try (ConfigurableApplicationContext contexto = ContextosDePersistencia.arrancar()) {
            JdbcTemplate jdbc = contexto.getBean(JdbcTemplate.class);
            Integer ofertasAntes = jdbc.queryForObject("select count(*) from oferta", Integer.class);
            Integer evaluacionesAntes = jdbc.queryForObject("select count(*) from evaluacion", Integer.class);
            MockMvc mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) contexto).build();

            String cuerpo = mockMvc.perform(post("/api/ofertas/busquedas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"cargo":"Backend","tecnologias":["Java"],"limite":20}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ofertas").isArray())
                    .andExpect(jsonPath("$.fuentesConsultadas").isArray())
                    .andExpect(jsonPath("$.advertencias").isArray())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(cuerpo).doesNotContain("registroId");
            assertThat(jdbc.queryForObject("select count(*) from oferta", Integer.class)).isEqualTo(ofertasAntes);
            assertThat(jdbc.queryForObject("select count(*) from evaluacion", Integer.class)).isEqualTo(evaluacionesAntes);

            ResultadoBusquedaOfertas vacia = contexto.getBean(BuscarOfertasUseCase.class)
                    .buscar(new CriteriosBusqueda("Cargo-que-no-existe-zz"));
            assertThat(vacia.ofertas()).isEmpty();

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
                    throw new IllegalStateException("caida");
                }
            };
            RegistroBusquedaRepository registros = contexto.getBean(RegistroBusquedaRepository.class);
            assertThatThrownBy(() -> new BuscarOfertasService(List.of(fallida), registros)
                    .buscar(new CriteriosBusqueda("Backend")))
                    .isInstanceOf(com.gustavo.jobagent.opportunities.application.port.in.FuentesNoDisponiblesException.class);

            FuenteConfigurada simulada = contexto.getBean(FuenteConfiguradaRepository.class)
                    .buscarPorId("SIMULADA").orElseThrow();
            contexto.getBean(FuenteConfiguradaRepository.class).guardar(new FuenteConfigurada(
                    simulada.fuenteId(), simulada.nombre(), simulada.estado(), false));
        }

        try (ConfigurableApplicationContext contexto = ContextosDePersistencia.arrancar()) {
            assertThatThrownBy(() -> contexto.getBean(BuscarOfertasUseCase.class)
                    .buscar(new CriteriosBusqueda("Backend")))
                    .isInstanceOf(com.gustavo.jobagent.opportunities.application.port.in.FuentesNoDisponiblesException.class);
            List<FuenteConfigurada> fuentes = contexto.getBean(FuenteConfiguradaRepository.class).listar();
            assertThat(fuentes).anySatisfy(fuente -> {
                assertThat(fuente.fuenteId()).isEqualTo("SIMULADA");
                assertThat(fuente.estado()).isEqualTo(FuenteConfigurada.EstadoFuente.DISPONIBLE);
            });
            assertThat(fuentes).filteredOn(fuente -> List.of("LINKEDIN", "INDEED", "GLASSDOOR").contains(fuente.fuenteId()))
                    .allSatisfy(fuente -> {
                        assertThat(fuente.estado()).isEqualTo(FuenteConfigurada.EstadoFuente.PENDIENTE);
                        assertThat(fuente.habilitada()).isFalse();
                    });
            contexto.getBean(FuenteConfiguradaRepository.class).guardar(new FuenteConfigurada(
                    "SIMULADA", "Fuente simulada", FuenteConfigurada.EstadoFuente.DISPONIBLE, true));
        }
    }
}
