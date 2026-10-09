package com.gustavo.jobagent.opportunities.application.port.out;

import com.gustavo.jobagent.opportunities.application.port.in.EvaluacionAlmacenada;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;

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
     * Inserta la evaluación y la copia inmutable del perfil y de la oferta usados.
     */
    EvaluacionOferta guardarConEvidencia(EvaluacionOferta evaluacion, PerfilProfesional perfil, OfertaLaboral oferta);

    Optional<EvaluacionAlmacenada> buscarAlmacenadaPorId(String id);

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
