package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.ConsultarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.OfertaRegistrada;
import com.gustavo.jobagent.opportunities.application.port.out.OfertaLaboralRepository;
import com.gustavo.jobagent.opportunities.domain.IdentidadOferta;

import java.util.Objects;
import java.util.Optional;

public class ConsultarOfertaService implements ConsultarOfertaUseCase {

    private final OfertaLaboralRepository repository;

    public ConsultarOfertaService(OfertaLaboralRepository repository) {
        this.repository = Objects.requireNonNull(repository, "El repositorio de ofertas no puede ser nulo");
    }

    @Override
    public Optional<OfertaRegistrada> porId(String id) {
        return repository.buscarPorId(id);
    }

    @Override
    public Optional<OfertaRegistrada> porDireccion(String fuenteId, String urlOriginal) {
        try {
            return repository.buscarPorIdentidad(IdentidadOferta.de(fuenteId, urlOriginal));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
