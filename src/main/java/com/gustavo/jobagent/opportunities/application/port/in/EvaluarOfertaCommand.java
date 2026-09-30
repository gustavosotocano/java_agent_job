package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;

import java.util.Objects;

/**
 * Comando que encapsula los datos requeridos para evaluar una oferta laboral contra un perfil profesional.
 */
public record EvaluarOfertaCommand(
        OfertaLaboral oferta,
        PerfilProfesional perfil
) {
    public EvaluarOfertaCommand {
        Objects.requireNonNull(oferta, "La oferta laboral es obligatoria para la evaluación");
        Objects.requireNonNull(perfil, "El perfil profesional es obligatorio para la evaluación");
    }
}
