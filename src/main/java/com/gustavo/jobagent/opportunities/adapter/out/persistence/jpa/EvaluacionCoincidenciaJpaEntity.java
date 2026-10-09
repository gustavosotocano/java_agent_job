package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.domain.TipoAspecto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class EvaluacionCoincidenciaJpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAspecto aspecto;

    @Column(nullable = false)
    private String detalle;

    @Column(nullable = false)
    private String evidencia;

    public static EvaluacionCoincidenciaJpaEntity desde(com.gustavo.jobagent.opportunities.domain.Coincidencia origen) {
        EvaluacionCoincidenciaJpaEntity fila = new EvaluacionCoincidenciaJpaEntity();
        fila.aspecto = origen.aspecto();
        fila.detalle = origen.detalle();
        fila.evidencia = origen.evidencia();
        return fila;
    }

    public com.gustavo.jobagent.opportunities.domain.Coincidencia aDominio() {
        return new com.gustavo.jobagent.opportunities.domain.Coincidencia(aspecto, detalle, evidencia);
    }
}
