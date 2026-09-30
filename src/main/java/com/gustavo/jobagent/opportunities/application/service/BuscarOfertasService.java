package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.AdvertenciaFuente;
import com.gustavo.jobagent.opportunities.application.port.in.BuscarOfertasUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.FuentesNoDisponiblesException;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoBusquedaOfertas;
import com.gustavo.jobagent.opportunities.application.port.out.BuscarOfertasPort;
import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Servicio de aplicación que implementa el caso de uso UC-001 (Buscar ofertas de empleo).
 */
public class BuscarOfertasService implements BuscarOfertasUseCase {

    private final List<BuscarOfertasPort> fuentes;

    public BuscarOfertasService(List<BuscarOfertasPort> fuentes) {
        this.fuentes = (fuentes != null) ? List.copyOf(fuentes) : List.of();
    }

    @Override
    public ResultadoBusquedaOfertas buscar(CriteriosBusqueda criterios) {
        Objects.requireNonNull(criterios, "Los criterios de búsqueda no pueden ser nulos");

        List<BuscarOfertasPort> fuentesHabilitadas = fuentes.stream()
                .filter(BuscarOfertasPort::estaHabilitada)
                .toList();

        if (fuentesHabilitadas.isEmpty()) {
            throw new FuentesNoDisponiblesException("No hay fuentes de ofertas disponibles o habilitadas");
        }

        List<String> fuentesConsultadas = new ArrayList<>();
        List<AdvertenciaFuente> advertencias = new ArrayList<>();
        Map<String, OfertaLaboral> ofertasDeduplicadas = new LinkedHashMap<>();

        for (BuscarOfertasPort fuente : fuentesHabilitadas) {
            try {
                List<OfertaLaboral> ofertasFuente = fuente.buscar(criterios);
                fuentesConsultadas.add(fuente.fuenteId());

                if (ofertasFuente != null) {
                    for (OfertaLaboral oferta : ofertasFuente) {
                        if (oferta != null && oferta.coincideCon(criterios)) {
                            String claveUnica = oferta.fuenteId() + ":" + oferta.idFuente();
                            ofertasDeduplicadas.putIfAbsent(claveUnica, oferta);
                        }
                    }
                }
            } catch (Exception e) {
                String detalleError = (e.getMessage() != null && !e.getMessage().isBlank())
                        ? e.getMessage()
                        : e.getClass().getSimpleName();
                advertencias.add(new AdvertenciaFuente(
                        fuente.fuenteId(),
                        "Fallo al consultar la fuente " + fuente.fuenteId() + ": " + detalleError
                ));
            }
        }

        if (fuentesConsultadas.isEmpty() && !advertencias.isEmpty()) {
            throw new FuentesNoDisponiblesException("Todas las fuentes consultadas fallaron: " + advertencias);
        }

        List<OfertaLaboral> ofertasOrdenadas = ofertasDeduplicadas.values().stream()
                .sorted(Comparator.comparing(OfertaLaboral::fuenteId)
                        .thenComparing(OfertaLaboral::idFuente))
                .limit(criterios.limite())
                .toList();

        return new ResultadoBusquedaOfertas(ofertasOrdenadas, fuentesConsultadas, advertencias);
    }
}
