package com.gustavo.jobagent.opportunities.application.port.in;

/**
 * Representa un aviso o fallo no crítico ocurrido al consultar una fuente externa.
 */
public record AdvertenciaFuente(
        String fuenteId,
        String mensaje
) {
    public AdvertenciaFuente {
        if (fuenteId == null || fuenteId.isBlank()) {
            throw new IllegalArgumentException("El identificador de la fuente no puede estar vacío");
        }
        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("El mensaje de advertencia no puede estar vacío");
        }
        fuenteId = fuenteId.strip();
        mensaje = mensaje.strip();
    }
}
