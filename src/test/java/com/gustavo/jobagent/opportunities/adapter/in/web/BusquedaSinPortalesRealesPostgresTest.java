package com.gustavo.jobagent.opportunities.adapter.in.web;

import com.gustavo.jobagent.opportunities.adapter.out.persistence.ContextosDePersistencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusquedaSinPortalesRealesPostgresTest {

    @Test
    @DisplayName("La búsqueda HTTP consulta SIMULADA y no los portales pendientes")
    void buscarNoConsultaPortalesReales() throws Exception {
        try (var contexto = ContextosDePersistencia.arrancar()) {
            JdbcTemplate jdbc = contexto.getBean(JdbcTemplate.class);
            Integer ofertasAntes = jdbc.queryForObject("select count(*) from oferta", Integer.class);
            Integer evaluacionesAntes = jdbc.queryForObject("select count(*) from evaluacion", Integer.class);
            MockMvc mockMvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) contexto).build();

            String cuerpo = mockMvc.perform(post("/api/ofertas/busquedas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"cargo":"Backend","tecnologias":["Java","Kafka"],"ubicacion":"Colombia","modalidad":"REMOTE","limite":20}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ofertas").isArray())
                    .andExpect(jsonPath("$.fuentesConsultadas[0]").value("SIMULADA"))
                    .andExpect(jsonPath("$.advertencias").isEmpty())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(cuerpo).doesNotContain("LINKEDIN");
            assertThat(cuerpo).doesNotContain("INDEED");
            assertThat(cuerpo).doesNotContain("GLASSDOOR");
            assertThat(jdbc.queryForObject("select count(*) from oferta", Integer.class)).isEqualTo(ofertasAntes);
            assertThat(jdbc.queryForObject("select count(*) from evaluacion", Integer.class)).isEqualTo(evaluacionesAntes);
        }
    }
}
