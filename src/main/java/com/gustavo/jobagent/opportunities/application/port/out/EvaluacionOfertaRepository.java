package com.gustavo.jobagent.opportunities.application.port.out;

import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y consultar evaluaciones de ofertas laborales.
 */
public interface EvaluacionOfertaRepository {

    /**
     * Guarda o actualiza una evaluación de oferta.
     */
    EvaluacionOferta guardar(EvaluacionOferta evaluacion);

    /**
     * Busca una evaluación por su identificador único.
     */
    Optional<EvaluacionOferta> buscarPorId(String id);

    /**
     * Recupera todas las evaluaciones realizadas para un perfil determinado.
     */
    List<EvaluacionOferta> buscarPorPerfil(String idPerfil);

    /**
     * Recupera todas las evaluaciones asociadas a una oferta determinada.
     */
    List<EvaluacionOferta> buscarPorOferta(String idOferta);
}
