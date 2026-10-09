package com.gustavo.jobagent.opportunities.application.port.in;

import java.util.Optional;

public interface ConsultarOfertaUseCase {
    Optional<OfertaRegistrada> porId(String id);

    Optional<OfertaRegistrada> porDireccion(String fuenteId, String urlOriginal);
}
