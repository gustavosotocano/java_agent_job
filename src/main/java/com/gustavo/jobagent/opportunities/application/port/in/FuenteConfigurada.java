package com.gustavo.jobagent.opportunities.application.port.in;

public record FuenteConfigurada(
        String fuenteId,
        String nombre,
        EstadoFuente estado,
        boolean habilitada
) {
    public enum EstadoFuente {
        DISPONIBLE,
        PENDIENTE,
        DESHABILITADA
    }
}
