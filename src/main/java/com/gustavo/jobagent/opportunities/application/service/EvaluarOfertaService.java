package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaCommand;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.out.EvaluacionOfertaRepository;
import com.gustavo.jobagent.opportunities.application.port.out.PropuestaLenguajePort;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.ObservacionPropuesta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import com.gustavo.jobagent.opportunities.domain.Propuesta;
import com.gustavo.jobagent.opportunities.domain.ResultadoPropuesta;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Servicio de aplicación que implementa el caso de uso UC-002 (Evaluar oferta de empleo).
 */
public class EvaluarOfertaService implements EvaluarOfertaUseCase {

    private final EvaluacionOfertaRepository repository;
    private final PropuestaLenguajePort propuestas;
    private final Duration limite;

    public EvaluarOfertaService(
            EvaluacionOfertaRepository repository,
            PropuestaLenguajePort propuestas,
            Duration limite
    ) {
        this.repository = Objects.requireNonNull(repository, "El repositorio de evaluaciones no puede ser nulo");
        this.propuestas = Objects.requireNonNull(propuestas, "El puerto de propuesta no puede ser nulo");
        if (limite == null || limite.isZero() || limite.isNegative()) {
            throw new IllegalArgumentException("El límite de la propuesta tiene que ser positivo");
        }
        this.limite = limite;
    }

    @Override
    public EvaluacionOferta evaluar(EvaluarOfertaCommand comando) {
        Objects.requireNonNull(comando, "El comando de evaluación no puede ser nulo");

        EvaluacionOferta calculada = EvaluacionOferta.evaluar(comando.oferta(), comando.perfil());
        List<ObservacionPropuesta> observaciones = pedirObservaciones(comando.oferta(), comando.perfil());
        EvaluacionOferta evaluacion = calculada.conObservaciones(observaciones);
        return repository.guardarConEvidencia(evaluacion, comando.perfil(), comando.oferta());
    }

    private List<ObservacionPropuesta> pedirObservaciones(OfertaLaboral oferta, PerfilProfesional perfil) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            Future<ResultadoPropuesta> futuro = executor.submit(() -> propuestas.proponer(oferta, perfil, limite));
            ResultadoPropuesta resultado;
            try {
                resultado = futuro.get(limite.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                futuro.cancel(true);
                return List.of();
            } catch (ExecutionException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                futuro.cancel(true);
                return List.of();
            }
            if (!(resultado instanceof ResultadoPropuesta.Disponible disponible)) {
                return List.of();
            }
            return Propuesta.aceptar(disponible.observaciones(), oferta, perfil);
        } finally {
            executor.shutdownNow();
        }
    }
}
