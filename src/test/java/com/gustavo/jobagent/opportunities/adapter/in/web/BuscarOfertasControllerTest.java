package com.gustavo.jobagent.opportunities.adapter.in.web;

import com.gustavo.jobagent.opportunities.application.port.in.BuscarOfertasUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.FuentesNoDisponiblesException;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoBusquedaOfertas;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BuscarOfertasControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BuscarOfertasUseCase buscarOfertasUseCase;

    @BeforeEach
    void setUp() {
        BuscarOfertasController controller = new BuscarOfertasController(buscarOfertasUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new OpportunitiesExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/ofertas/busquedas con criterios válidos devuelve 200 OK y resultados normalizados")
    void debeBuscarOfertasConCriteriosValidos() throws Exception {
        given(buscarOfertasUseCase.buscar(any()))
                .willReturn(new ResultadoBusquedaOfertas(
                        List.of(
                                OfertaLaboral.builder()
                                        .idFuente("sim-001")
                                        .fuenteId("SIMULADA")
                                        .titulo("Senior Java Backend Engineer")
                                        .empresa("Empresa de ejemplo")
                                        .descripcion("Java, Spring Boot, Kafka y AWS")
                                        .ubicacion("Colombia")
                                        .modalidad(ModalidadTrabajo.REMOTE)
                                        .urlOriginal("https://example.com/jobs/sim-001")
                                        .tecnologias(List.of("Java", "Spring Boot", "Kafka", "AWS"))
                                        .build()
                        ),
                        List.of("SIMULADA"),
                        List.of()
                ));

        String jsonPayload = """
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
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ofertas.length()").value(1))
                .andExpect(jsonPath("$.ofertas[0].idFuente").value("sim-001"))
                .andExpect(jsonPath("$.ofertas[0].fuenteId").value("SIMULADA"))
                .andExpect(jsonPath("$.ofertas[0].titulo").value("Senior Java Backend Engineer"))
                .andExpect(jsonPath("$.ofertas[0].modalidad").value("REMOTE"))
                .andExpect(jsonPath("$.fuentesConsultadas[0]").value("SIMULADA"))
                .andExpect(jsonPath("$.advertencias").isEmpty());
    }

    @Test
    @DisplayName("POST /api/ofertas/busquedas con cargo vacío devuelve 400 Bad Request")
    void debeRetornar400CuandoCargoEsVacio() throws Exception {
        String jsonPayload = """
                {
                  "cargo": "   ",
                  "tecnologias": ["Java"],
                  "ubicacion": "Colombia",
                  "modalidad": "REMOTE",
                  "limite": 20
                }
                """;

        mockMvc.perform(post("/api/ofertas/busquedas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/ofertas/busquedas con límite fuera de rango devuelve 400 Bad Request")
    void debeRetornar400CuandoLimiteFueraDeRango() throws Exception {
        String jsonPayload = """
                {
                  "cargo": "Backend",
                  "tecnologias": ["Java"],
                  "ubicacion": "Colombia",
                  "modalidad": "REMOTE",
                  "limite": 200
                }
                """;

        mockMvc.perform(post("/api/ofertas/busquedas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/ofertas/busquedas sin fuentes disponibles devuelve 503 Service Unavailable")
    void debeRetornar503CuandoFuentesNoDisponibles() throws Exception {
        given(buscarOfertasUseCase.buscar(any()))
                .willThrow(new FuentesNoDisponiblesException("No hay fuentes de ofertas disponibles o habilitadas"));

        String jsonPayload = """
                {
                  "cargo": "Backend",
                  "limite": 20
                }
                """;

        mockMvc.perform(post("/api/ofertas/busquedas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.mensaje").value("No hay fuentes de ofertas disponibles o habilitadas"));
    }
}
