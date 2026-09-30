package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;

/**
 * Puerto de entrada para ejecutar búsquedas de ofertas laborales.
 */
public interface BuscarOfertasUseCase {

    /**
     * Busca ofertas laborales coincidentes según los criterios explícitos informados.
     *
     * @param criterios Criterios validados de búsqueda.
     * @return Resultado que consolida las ofertas, fuentes consultadas y advertencias de fuentes con fallos parciales.
     * @throws FuentesNoDisponiblesException si no hay fuentes habilitadas o todas fallan.
     */
    ResultadoBusquedaOfertas buscar(CriteriosBusqueda criterios);
}
