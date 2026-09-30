package com.gustavo.jobagent.opportunities.adapter.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BuscarOfertasIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    @DisplayName("Debe ejecutar el flujo completo de búsqueda contra la fuente simulada de forma reproducible")
    void debeEjecutarFlujoCompletoConExito() throws Exception {
        String requestJson = """
                {
                  "cargo": "Backend",
                  "tecnologias": ["Java", "Kafka"],
                  "ubicacion": "Colombia",
                  "modalidad": "REMOTE",
                  "limite": 20
                }
                """;

        mockMvc.perform(post("/api/ofertas/busquedas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ofertas.length()").value(1))
                .andExpect(jsonPath("$.ofertas[0].idFuente").value("sim-001"))
                .andExpect(jsonPath("$.ofertas[0].fuenteId").value("SIMULADA"))
                .andExpect(jsonPath("$.ofertas[0].titulo").value("Senior Java Backend Engineer"))
                .andExpect(jsonPath("$.ofertas[0].empresa").value("Empresa de ejemplo"))
                .andExpect(jsonPath("$.ofertas[0].ubicacion").value("Colombia"))
                .andExpect(jsonPath("$.ofertas[0].modalidad").value("REMOTE"))
                .andExpect(jsonPath("$.ofertas[0].urlOriginal").value("https://example.com/jobs/sim-001"))
                .andExpect(jsonPath("$.fuentesConsultadas[0]").value("SIMULADA"))
                .andExpect(jsonPath("$.advertencias").isEmpty());
    }
}
