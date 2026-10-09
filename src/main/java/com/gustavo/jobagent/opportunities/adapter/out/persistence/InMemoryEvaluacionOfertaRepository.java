package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.application.port.in.EvaluacionAlmacenada;
import com.gustavo.jobagent.opportunities.application.port.out.EvaluacionOfertaRepository;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador de salida en memoria para almacenar y consultar evaluaciones de ofertas laborales.
 */
public class InMemoryEvaluacionOfertaRepository implements EvaluacionOfertaRepository {

    private final Map<String, EvaluacionOferta> storage = new ConcurrentHashMap<>();
    private final Map<String, EvaluacionAlmacenada> evidencias = new ConcurrentHashMap<>();

    @Override
    public EvaluacionOferta guardar(EvaluacionOferta evaluacion) {
        Objects.requireNonNull(evaluacion, "La evaluación no puede ser nula");
        storage.put(evaluacion.id(), evaluacion);
        return evaluacion;
    }

    @Override
    public EvaluacionOferta guardarConEvidencia(EvaluacionOferta evaluacion, PerfilProfesional perfil, OfertaLaboral oferta) {
        guardar(evaluacion);
        evidencias.put(evaluacion.id(), new EvaluacionAlmacenada(evaluacion, perfil, oferta));
        return evaluacion;
    }

    @Override
    public Optional<EvaluacionAlmacenada> buscarAlmacenadaPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(evidencias.get(id.strip()));
    }

    @Override
    public Optional<EvaluacionOferta> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id.strip()));
    }

    @Override
    public List<EvaluacionOferta> buscarPorPerfil(String idPerfil) {
        if (idPerfil == null || idPerfil.isBlank()) {
            return List.of();
        }
        String cleanId = idPerfil.strip();
        return storage.values().stream()
                .filter(e -> e.idPerfil().equals(cleanId))
                .toList();
    }

    @Override
    public List<EvaluacionOferta> buscarPorOferta(String idOferta) {
        if (idOferta == null || idOferta.isBlank()) {
            return List.of();
        }
        String cleanId = idOferta.strip();
        return storage.values().stream()
                .filter(e -> e.idOferta().equals(cleanId))
                .toList();
    }

    public void limpiar() {
        storage.clear();
        evidencias.clear();
    }
}
