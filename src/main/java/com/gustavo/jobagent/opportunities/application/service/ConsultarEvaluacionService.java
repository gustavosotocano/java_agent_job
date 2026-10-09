package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.ConsultarEvaluacionUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluacionAlmacenada;
import com.gustavo.jobagent.opportunities.application.port.out.EvaluacionOfertaRepository;

import java.util.Objects;
import java.util.Optional;

public class ConsultarEvaluacionService implements ConsultarEvaluacionUseCase {

    private final EvaluacionOfertaRepository repository;

    public ConsultarEvaluacionService(EvaluacionOfertaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "El repositorio de evaluaciones no puede ser nulo");
    }

    @Override
    public Optional<EvaluacionAlmacenada> consultar(String id) {
        return repository.buscarAlmacenadaPorId(id);
    }
}
