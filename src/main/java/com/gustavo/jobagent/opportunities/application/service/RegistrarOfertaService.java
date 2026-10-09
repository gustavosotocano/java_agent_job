package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.OfertaRegistrada;
import com.gustavo.jobagent.opportunities.application.port.in.RegistrarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.out.IdentidadDuplicadaException;
import com.gustavo.jobagent.opportunities.application.port.out.OfertaLaboralRepository;
import com.gustavo.jobagent.opportunities.domain.IdentidadOferta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class RegistrarOfertaService implements RegistrarOfertaUseCase {

    private static final int REINTENTOS = 3;

    private final OfertaLaboralRepository repository;
    private final Clock clock;

    public RegistrarOfertaService(OfertaLaboralRepository repository) {
        this(repository, Clock.systemUTC());
    }

    public RegistrarOfertaService(OfertaLaboralRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "El repositorio de ofertas no puede ser nulo");
        this.clock = Objects.requireNonNull(clock, "El reloj no puede ser nulo");
    }

    @Override
    public OfertaRegistrada registrar(OfertaLaboral oferta) {
        Objects.requireNonNull(oferta, "La oferta no puede ser nula");
        IdentidadOferta identidad = IdentidadOferta.de(oferta.fuenteId(), oferta.urlOriginal());
        Instant capturadoEn = clock.instant();
        IdentidadDuplicadaException ultima = null;
        for (int intento = 0; intento < REINTENTOS; intento++) {
            try {
                Optional<OfertaRegistrada> existente = repository.buscarPorIdentidad(identidad);
                if (existente.isPresent()) {
                    return repository.agregarProcedencia(identidad, oferta, capturadoEn);
                }
                return repository.insertarNueva(oferta, identidad, capturadoEn);
            } catch (IdentidadDuplicadaException e) {
                ultima = e;
            }
        }
        throw ultima;
    }
}
