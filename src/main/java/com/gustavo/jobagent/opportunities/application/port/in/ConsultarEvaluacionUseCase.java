package com.gustavo.jobagent.opportunities.application.port.in;

import java.util.Optional;

public interface ConsultarEvaluacionUseCase {
    Optional<EvaluacionAlmacenada> consultar(String id);
}
