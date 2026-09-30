package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;

/**
 * Puerto de entrada para ejecutar el caso de uso UC-002 (Evaluar oferta de empleo).
 */
public interface EvaluarOfertaUseCase {

    /**
     * Evalúa la compatibilidad de una oferta laboral contra el perfil profesional informado.
     *
     * @param comando Comando con la oferta y el perfil válidos.
     * @return Aggregate Root {@link EvaluacionOferta} con el resultado estructurado y persistido.
     */
    EvaluacionOferta evaluar(EvaluarOfertaCommand comando);
}
