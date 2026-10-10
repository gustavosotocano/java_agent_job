package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.EjecucionAcotada;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;

import java.util.List;

public interface EjecutarAgenteUseCase {

    EjecucionRealizada ejecutar(PerfilProfesional perfil, List<EjecucionAcotada.Solicitud> pasos);
}
