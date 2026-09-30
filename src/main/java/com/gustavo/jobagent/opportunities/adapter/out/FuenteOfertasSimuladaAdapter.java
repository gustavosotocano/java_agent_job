package com.gustavo.jobagent.opportunities.adapter.out;

import com.gustavo.jobagent.opportunities.application.port.out.BuscarOfertasPort;
import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;

import java.util.List;
import java.util.Set;

/**
 * Adaptador de salida que provee ofertas laborales reproducibles desde un catálogo simulado en memoria.
 */
public class FuenteOfertasSimuladaAdapter implements BuscarOfertasPort {

    public static final String FUENTE_ID = "SIMULADA";

    private final boolean habilitada;
    private final List<OfertaLaboral> catalogo;

    public FuenteOfertasSimuladaAdapter() {
        this(true, crearCatalogoPredeterminado());
    }

    public FuenteOfertasSimuladaAdapter(boolean habilitada) {
        this(habilitada, crearCatalogoPredeterminado());
    }

    public FuenteOfertasSimuladaAdapter(boolean habilitada, List<OfertaLaboral> catalogo) {
        this.habilitada = habilitada;
        this.catalogo = (catalogo != null) ? List.copyOf(catalogo) : List.of();
    }

    @Override
    public String fuenteId() {
        return FUENTE_ID;
    }

    @Override
    public boolean estaHabilitada() {
        return habilitada;
    }

    @Override
    public List<OfertaLaboral> buscar(CriteriosBusqueda criterios) {
        if (!habilitada) {
            return List.of();
        }
        return catalogo.stream()
                .filter(oferta -> oferta.coincideCon(criterios))
                .toList();
    }

    private static List<OfertaLaboral> crearCatalogoPredeterminado() {
        return List.of(
                OfertaLaboral.builder()
                        .idFuente("sim-001")
                        .fuenteId(FUENTE_ID)
                        .titulo("Senior Java Backend Engineer")
                        .empresa("Empresa de ejemplo")
                        .descripcion("Java, Spring Boot, Kafka y AWS")
                        .ubicacion("Colombia")
                        .modalidad(ModalidadTrabajo.REMOTE)
                        .urlOriginal("https://example.com/jobs/sim-001")
                        .tecnologias(Set.of("Java", "Spring Boot", "Kafka", "AWS"))
                        .build(),
                OfertaLaboral.builder()
                        .idFuente("sim-002")
                        .fuenteId(FUENTE_ID)
                        .titulo("Java Backend Developer")
                        .empresa("Tech Solutions")
                        .descripcion("Desarrollo con Java y Spring Boot")
                        .ubicacion("Colombia")
                        .modalidad(ModalidadTrabajo.REMOTE)
                        .urlOriginal("https://example.com/jobs/sim-002")
                        .tecnologias(Set.of("Java", "Spring Boot"))
                        .build(),
                OfertaLaboral.builder()
                        .idFuente("sim-003")
                        .fuenteId(FUENTE_ID)
                        .titulo("Backend Software Engineer")
                        .empresa("Bank Corp")
                        .descripcion("Sistemas transaccionales con Java y Kafka")
                        .ubicacion("Colombia")
                        .modalidad(ModalidadTrabajo.ONSITE)
                        .urlOriginal("https://example.com/jobs/sim-003")
                        .tecnologias(Set.of("Java", "Kafka"))
                        .build(),
                OfertaLaboral.builder()
                        .idFuente("sim-004")
                        .fuenteId(FUENTE_ID)
                        .titulo("Fullstack Java & React Developer")
                        .empresa("Global Digital")
                        .descripcion("Microservicios en Java, Kafka y Frontend en React")
                        .ubicacion("España")
                        .modalidad(ModalidadTrabajo.REMOTE)
                        .urlOriginal("https://example.com/jobs/sim-004")
                        .tecnologias(Set.of("Java", "Kafka", "React"))
                        .build(),
                OfertaLaboral.builder()
                        .idFuente("sim-005")
                        .fuenteId(FUENTE_ID)
                        .titulo("Frontend Engineer")
                        .empresa("Design Studio")
                        .descripcion("Desarrollo de interfaces en React y TypeScript")
                        .ubicacion("Colombia")
                        .modalidad(ModalidadTrabajo.REMOTE)
                        .urlOriginal("https://example.com/jobs/sim-005")
                        .tecnologias(Set.of("React", "TypeScript"))
                        .build()
        );
    }
}
