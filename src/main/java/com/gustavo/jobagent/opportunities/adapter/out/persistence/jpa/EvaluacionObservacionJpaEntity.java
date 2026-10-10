package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.domain.ObservacionPropuesta;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EvaluacionObservacionJpaEntity {

    @Column(nullable = false)
    private String texto;

    @Column(nullable = false)
    private String fragmento;

    public static EvaluacionObservacionJpaEntity desde(ObservacionPropuesta origen) {
        EvaluacionObservacionJpaEntity fila = new EvaluacionObservacionJpaEntity();
        fila.texto = origen.texto();
        fila.fragmento = origen.fragmento();
        return fila;
    }

    public ObservacionPropuesta aDominio() {
        return new ObservacionPropuesta(texto, fragmento);
    }
}
