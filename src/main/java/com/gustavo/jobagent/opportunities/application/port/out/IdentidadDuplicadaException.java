package com.gustavo.jobagent.opportunities.application.port.out;

/**
 * La identidad fuente + dirección pública ya existe.
 */
public class IdentidadDuplicadaException extends RuntimeException {
    public IdentidadDuplicadaException() {
        super("La oferta ya está registrada para esa fuente y dirección pública");
    }
}
