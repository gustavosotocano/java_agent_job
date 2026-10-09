package com.gustavo.jobagent.opportunities.application.port.out;

import com.gustavo.jobagent.opportunities.application.port.in.FuenteConfigurada;

import java.util.List;
import java.util.Optional;

public interface FuenteConfiguradaRepository {

    Optional<FuenteConfigurada> buscarPorId(String fuenteId);

    List<FuenteConfigurada> listar();

    FuenteConfigurada guardar(FuenteConfigurada fuente);
}
