package com.gustavo.jobagent.opportunities.application.port.out;

import com.gustavo.jobagent.opportunities.application.port.in.RegistroBusqueda;

import java.util.Optional;

public interface RegistroBusquedaRepository {

    RegistroBusqueda guardar(RegistroBusqueda registro);

    Optional<RegistroBusqueda> buscarPorId(String id);

    static RegistroBusquedaRepository nulo() {
        return new RegistroBusquedaRepository() {
            @Override
            public RegistroBusqueda guardar(RegistroBusqueda registro) {
                return registro;
            }

            @Override
            public Optional<RegistroBusqueda> buscarPorId(String id) {
                return Optional.empty();
            }
        };
    }
}
