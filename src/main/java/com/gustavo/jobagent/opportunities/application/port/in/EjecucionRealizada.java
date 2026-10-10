package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.EjecucionAcotada;

import java.util.List;

public record EjecucionRealizada(
        EjecucionAcotada.EstadoEjecucion estado,
        List<PasoRealizado> realizados,
        List<String> noEjecutados
) {
    public EjecucionRealizada {
        realizados = realizados == null ? List.of() : List.copyOf(realizados);
        noEjecutados = noEjecutados == null ? List.of() : List.copyOf(noEjecutados);
    }
}
