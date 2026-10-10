package com.gustavo.jobagent.opportunities.application.port.out;

import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import com.gustavo.jobagent.opportunities.domain.ResultadoPropuesta;

import java.time.Duration;

/**
 * Puerto de salida para pedir una propuesta. El cálculo de la evaluación no depende de él.
 */
public interface PropuestaLenguajePort {

    ResultadoPropuesta proponer(OfertaLaboral oferta, PerfilProfesional perfil, Duration limite);
}
