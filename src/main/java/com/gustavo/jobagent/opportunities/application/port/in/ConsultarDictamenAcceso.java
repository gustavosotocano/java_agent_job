package com.gustavo.jobagent.opportunities.application.port.in;

import com.gustavo.jobagent.opportunities.domain.DictamenAcceso;

import java.util.List;
import java.util.Optional;

public interface ConsultarDictamenAcceso {

    List<DictamenAcceso> listar();

    Optional<DictamenAcceso> porFuente(String fuenteId);
}
