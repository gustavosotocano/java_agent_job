package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;

/**
 * Evaluación guardada junto con las copias del perfil y de la oferta usadas como evidencia.
 */
public record EvaluacionAlmacenada(
        EvaluacionOferta evaluacion,
        PerfilProfesional perfil,
        OfertaLaboral oferta
) {
}
