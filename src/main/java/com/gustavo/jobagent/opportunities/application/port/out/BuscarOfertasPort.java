package com.gustavo.jobagent.opportunities.application.port.out;

import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;

import java.util.List;

/**
 * Puerto de salida para consultar ofertas desde una fuente externa o simulada.
 */
public interface BuscarOfertasPort {

    /**
     * Identificador unívoco y estable de la fuente (ej. "SIMULADA", "LINKEDIN").
     */
    String fuenteId();

    /**
     * Indica si la fuente se encuentra actualmente habilitada para ser consultada.
     */
    boolean estaHabilitada();

    /**
     * Ejecuta la consulta de ofertas contra la fuente externa.
     *
     * @param criterios Criterios de búsqueda.
     * @return Lista de ofertas laborales recuperadas por la fuente.
     * @throws RuntimeException si la fuente experimenta un fallo de conectividad o ejecución.
     */
    List<OfertaLaboral> buscar(CriteriosBusqueda criterios);
}
