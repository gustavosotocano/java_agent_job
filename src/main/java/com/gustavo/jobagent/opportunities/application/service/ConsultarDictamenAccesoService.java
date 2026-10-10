package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.ConsultarDictamenAcceso;
import com.gustavo.jobagent.opportunities.application.port.out.DictamenAccesoRepository;
import com.gustavo.jobagent.opportunities.domain.DictamenAcceso;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConsultarDictamenAccesoService implements ConsultarDictamenAcceso {

    private static final List<String> ORDEN = List.of("LINKEDIN", "INDEED", "GLASSDOOR");

    private final DictamenAccesoRepository repository;

    public ConsultarDictamenAccesoService(DictamenAccesoRepository repository) {
        this.repository = Objects.requireNonNull(repository, "El repositorio de dictámenes no puede ser nulo");
    }

    @Override
    public List<DictamenAcceso> listar() {
        var porFuente = repository.listar().stream()
                .collect(Collectors.toMap(DictamenAcceso::fuenteId, dictamen -> dictamen));
        return ORDEN.stream().map(porFuente::get).filter(Objects::nonNull).toList();
    }

    @Override
    public Optional<DictamenAcceso> porFuente(String fuenteId) {
        if (fuenteId == null || fuenteId.isBlank() || !ORDEN.contains(fuenteId.strip())) {
            return Optional.empty();
        }
        return repository.porFuente(fuenteId.strip());
    }
}
