package com.gustavo.jobagent.opportunities.application.port.out;

import com.gustavo.jobagent.opportunities.domain.DictamenAcceso;

import java.util.List;
import java.util.Optional;

public interface DictamenAccesoRepository {

    List<DictamenAcceso> listar();

    Optional<DictamenAcceso> porFuente(String fuenteId);
}
