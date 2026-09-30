package com.gustavo.jobagent.opportunities.application.port.in;

/**
 * Excepción lanzada cuando no es posible consultar ninguna fuente de ofertas.
 */
public class FuentesNoDisponiblesException extends RuntimeException {
    public FuentesNoDisponiblesException(String mensaje) {
        super(mensaje);
    }

    public FuentesNoDisponiblesException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
