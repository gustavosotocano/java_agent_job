package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AdvertenciaEmbebida {

    @Column(name = "fuente_id", nullable = false)
    private String fuenteId;

    @Column(nullable = false)
    private String mensaje;

    public static AdvertenciaEmbebida desde(String fuenteId, String mensaje) {
        AdvertenciaEmbebida fila = new AdvertenciaEmbebida();
        fila.fuenteId = fuenteId;
        fila.mensaje = mensaje;
        return fila;
    }

    public String getFuenteId() {
        return fuenteId;
    }

    public String getMensaje() {
        return mensaje;
    }
}
