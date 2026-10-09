package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;

public interface RegistrarOfertaUseCase {
    OfertaRegistrada registrar(OfertaLaboral oferta);
}
