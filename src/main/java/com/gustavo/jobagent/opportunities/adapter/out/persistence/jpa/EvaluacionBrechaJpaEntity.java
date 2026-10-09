package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.domain.TipoAspecto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class EvaluacionBrechaJpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAspecto aspecto;

    @Column(nullable = false)
    private String detalle;

    @Column(name = "es_bloqueante", nullable = false)
    private boolean esBloqueante;

    public static EvaluacionBrechaJpaEntity desde(com.gustavo.jobagent.opportunities.domain.Brecha origen) {
        EvaluacionBrechaJpaEntity fila = new EvaluacionBrechaJpaEntity();
        fila.aspecto = origen.aspecto();
        fila.detalle = origen.detalle();
        fila.esBloqueante = origen.esBloqueante();
        return fila;
    }

    public com.gustavo.jobagent.opportunities.domain.Brecha aDominio() {
        return new com.gustavo.jobagent.opportunities.domain.Brecha(aspecto, detalle, esBloqueante);
    }
}
