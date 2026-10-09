package com.gustavo.jobagent.opportunities.application.port.in;

import java.util.Optional;

public interface ConsultarRegistroBusquedaUseCase {
    Optional<RegistroBusqueda> consultar(String id);
}
