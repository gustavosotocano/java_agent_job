package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;

import java.time.Instant;
import java.util.List;

/**
 * Registro de un intento de búsqueda, sin ofertas ni evaluaciones asociadas.
 */
public record RegistroBusqueda(
        String id,
        Instant ejecutadoEn,
        CriteriosBusqueda criterios,
        List<String> fuentesConsultadas,
        List<AdvertenciaFuente> advertencias,
        ResultadoRegistroBusqueda resultado,
        int cantidadOfertasDevueltas
) {
    public RegistroBusqueda {
        fuentesConsultadas = fuentesConsultadas == null ? List.of() : List.copyOf(fuentesConsultadas);
        advertencias = advertencias == null ? List.of() : List.copyOf(advertencias);
    }
}
