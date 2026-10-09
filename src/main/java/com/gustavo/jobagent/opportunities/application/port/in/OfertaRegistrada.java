package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.IdentidadOferta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;

import java.time.Instant;
import java.util.List;

/**
 * Oferta vigente y todas las capturas conservadas para su identidad.
 */
public record OfertaRegistrada(
        String id,
        IdentidadOferta identidad,
        OfertaLaboral vigente,
        List<ProcedenciaOferta> procedencias
) {
    public OfertaRegistrada {
        procedencias = procedencias == null ? List.of() : List.copyOf(procedencias);
    }

    public record ProcedenciaOferta(Instant capturadoEn, OfertaLaboral captura) {
    }
}
