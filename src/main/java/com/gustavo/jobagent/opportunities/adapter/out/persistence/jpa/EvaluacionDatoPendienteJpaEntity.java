package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.domain.TipoAspecto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class EvaluacionDatoPendienteJpaEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAspecto aspecto;

    @Column(nullable = false)
    private String pregunta;

    public static EvaluacionDatoPendienteJpaEntity desde(com.gustavo.jobagent.opportunities.domain.DatoPendiente origen) {
        EvaluacionDatoPendienteJpaEntity fila = new EvaluacionDatoPendienteJpaEntity();
        fila.aspecto = origen.aspecto();
        fila.pregunta = origen.pregunta();
        return fila;
    }

    public com.gustavo.jobagent.opportunities.domain.DatoPendiente aDominio() {
        return new com.gustavo.jobagent.opportunities.domain.DatoPendiente(aspecto, pregunta);
    }
}
