package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;

import java.util.List;

/**
 * Resultado estructurado del caso de uso de búsqueda de ofertas laborales.
 */
public record ResultadoBusquedaOfertas(
        List<OfertaLaboral> ofertas,
        List<String> fuentesConsultadas,
        List<AdvertenciaFuente> advertencias
) {
    public ResultadoBusquedaOfertas {
        ofertas = (ofertas != null) ? List.copyOf(ofertas) : List.of();
        fuentesConsultadas = (fuentesConsultadas != null) ? List.copyOf(fuentesConsultadas) : List.of();
        advertencias = (advertencias != null) ? List.copyOf(advertencias) : List.of();
    }
}
