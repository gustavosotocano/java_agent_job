package com.gustavo.jobagent.opportunities.adapter.out.lenguaje;

import com.gustavo.jobagent.opportunities.application.port.out.PropuestaLenguajePort;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import com.gustavo.jobagent.opportunities.domain.ResultadoPropuesta;

import java.time.Duration;

/**
 * Adaptador predeterminado. No consulta ningún servicio.
 */
public final class PropuestaVaciaAdapter implements PropuestaLenguajePort {

    @Override
    public ResultadoPropuesta proponer(OfertaLaboral oferta, PerfilProfesional perfil, Duration limite) {
        return new ResultadoPropuesta.Disponible(java.util.List.of());
    }
}
