package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.AdvertenciaFuente;
import com.gustavo.jobagent.opportunities.application.port.in.BuscarOfertasUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.FuentesNoDisponiblesException;
import com.gustavo.jobagent.opportunities.application.port.in.RegistroBusqueda;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoBusquedaOfertas;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoRegistroBusqueda;
import com.gustavo.jobagent.opportunities.application.port.out.BuscarOfertasPort;
import com.gustavo.jobagent.opportunities.application.port.out.RegistroBusquedaRepository;
import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de aplicación que implementa el caso de uso UC-001 (Buscar ofertas de empleo).
 */
public class BuscarOfertasService implements BuscarOfertasUseCase {

    private final List<BuscarOfertasPort> fuentes;
    private final RegistroBusquedaRepository registros;

    public BuscarOfertasService(List<BuscarOfertasPort> fuentes) {
        this(fuentes, RegistroBusquedaRepository.nulo());
    }

    public BuscarOfertasService(List<BuscarOfertasPort> fuentes, RegistroBusquedaRepository registros) {
        this.fuentes = (fuentes != null) ? List.copyOf(fuentes) : List.of();
        this.registros = registros != null ? registros : RegistroBusquedaRepository.nulo();
    }

    @Override
    public ResultadoBusquedaOfertas buscar(CriteriosBusqueda criterios) {
        Objects.requireNonNull(criterios, "Los criterios de búsqueda no pueden ser nulos");

        List<BuscarOfertasPort> fuentesHabilitadas = fuentes.stream()
                .filter(BuscarOfertasPort::estaHabilitada)
                .toList();

        if (fuentesHabilitadas.isEmpty()) {
            guardarRegistro(criterios, List.of(), List.of(), ResultadoRegistroBusqueda.SIN_FUENTES_HABILITADAS, 0);
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

        List<OfertaLaboral> ofertasOrdenadas = ofertasDeduplicadas.values().stream()
                .sorted(Comparator.comparing(OfertaLaboral::fuenteId)
                        .thenComparing(OfertaLaboral::idFuente))
                .limit(criterios.limite())
                .toList();

        ResultadoRegistroBusqueda resultado = clasificar(fuentesConsultadas, advertencias, ofertasOrdenadas);
        guardarRegistro(criterios, fuentesConsultadas, advertencias, resultado, ofertasOrdenadas.size());

        if (resultado == ResultadoRegistroBusqueda.BUSQUEDA_FALLIDA) {
            throw new FuentesNoDisponiblesException("Todas las fuentes consultadas fallaron: " + advertencias);
        }

        return new ResultadoBusquedaOfertas(ofertasOrdenadas, fuentesConsultadas, advertencias);
    }

    private RegistroBusqueda guardarRegistro(
            CriteriosBusqueda criterios,
            List<String> fuentesConsultadas,
            List<AdvertenciaFuente> advertencias,
            ResultadoRegistroBusqueda resultado,
            int cantidad
    ) {
        return registros.guardar(new RegistroBusqueda(
                UUID.randomUUID().toString(),
                Instant.now(),
                criterios,
                fuentesConsultadas,
                advertencias,
                resultado,
                cantidad
        ));
    }

    static ResultadoRegistroBusqueda clasificar(
            List<String> fuentesConsultadas,
            List<AdvertenciaFuente> advertencias,
            List<OfertaLaboral> ofertas
    ) {
        if (fuentesConsultadas.isEmpty() && !advertencias.isEmpty()) {
            return ResultadoRegistroBusqueda.BUSQUEDA_FALLIDA;
        }
        if (!fuentesConsultadas.isEmpty() && !advertencias.isEmpty()) {
            return ResultadoRegistroBusqueda.FALLO_PARCIAL;
        }
        if (ofertas.isEmpty()) {
            return ResultadoRegistroBusqueda.RESULTADO_VACIO;
        }
        return ResultadoRegistroBusqueda.CON_RESULTADOS;
    }
}
