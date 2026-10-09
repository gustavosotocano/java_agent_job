package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.ConsultarRegistroBusquedaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.RegistroBusqueda;
import com.gustavo.jobagent.opportunities.application.port.out.RegistroBusquedaRepository;

import java.util.Objects;
import java.util.Optional;

public class ConsultarRegistroBusquedaService implements ConsultarRegistroBusquedaUseCase {

    private final RegistroBusquedaRepository repository;

    public ConsultarRegistroBusquedaService(RegistroBusquedaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "El repositorio de búsquedas no puede ser nulo");
    }

    @Override
    public Optional<RegistroBusqueda> consultar(String id) {
        return repository.buscarPorId(id);
    }
}
