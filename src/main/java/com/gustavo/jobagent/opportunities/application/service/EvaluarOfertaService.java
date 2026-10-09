package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaCommand;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.out.EvaluacionOfertaRepository;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;

import java.util.Objects;

/**
 * Servicio de aplicación que implementa el caso de uso UC-002 (Evaluar oferta de empleo).
 */
public class EvaluarOfertaService implements EvaluarOfertaUseCase {

    private final EvaluacionOfertaRepository repository;

    public EvaluarOfertaService(EvaluacionOfertaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "El repositorio de evaluaciones no puede ser nulo");
    }

    @Override
    public EvaluacionOferta evaluar(EvaluarOfertaCommand comando) {
        Objects.requireNonNull(comando, "El comando de evaluación no puede ser nulo");

        EvaluacionOferta evaluacion = EvaluacionOferta.evaluar(comando.oferta(), comando.perfil());
        return repository.guardarConEvidencia(evaluacion, comando.perfil(), comando.oferta());
    }
}
