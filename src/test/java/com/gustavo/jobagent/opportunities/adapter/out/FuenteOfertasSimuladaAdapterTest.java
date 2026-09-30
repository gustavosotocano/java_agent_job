package com.gustavo.jobagent.opportunities.adapter.out;

import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FuenteOfertasSimuladaAdapterTest {

    @Test
    @DisplayName("Debe consultar ofertas coincidentes en el catálogo simulado predeterminado")
    void debeConsultarOfertasEnCatalogoPredeterminado() {
        FuenteOfertasSimuladaAdapter adapter = new FuenteOfertasSimuladaAdapter();

        CriteriosBusqueda criterios = new CriteriosBusqueda(
                "Backend",
                List.of("Java", "Kafka"),
                "Colombia",
                ModalidadTrabajo.REMOTE,
                20
        );

        List<OfertaLaboral> ofertas = adapter.buscar(criterios);

        assertThat(ofertas).hasSize(1);
        OfertaLaboral sim001 = ofertas.getFirst();
        assertThat(sim001.idFuente()).isEqualTo("sim-001");
        assertThat(sim001.fuenteId()).isEqualTo("SIMULADA");
        assertThat(sim001.titulo()).isEqualTo("Senior Java Backend Engineer");
        assertThat(sim001.ubicacion()).isEqualTo("Colombia");
        assertThat(sim001.modalidad()).isEqualTo(ModalidadTrabajo.REMOTE);
        assertThat(sim001.fechaPublicacion()).isNull();
        assertThat(sim001.salario()).isNull();
    }

    @Test
    @DisplayName("Una fuente deshabilitada devuelve lista vacía y reporta estaHabilitada=false")
    void fuenteDeshabilitadaDevuelveListaVacia() {
        FuenteOfertasSimuladaAdapter adapter = new FuenteOfertasSimuladaAdapter(false);

        assertThat(adapter.estaHabilitada()).isFalse();

        List<OfertaLaboral> ofertas = adapter.buscar(new CriteriosBusqueda("Backend"));
        assertThat(ofertas).isEmpty();
    }
}
