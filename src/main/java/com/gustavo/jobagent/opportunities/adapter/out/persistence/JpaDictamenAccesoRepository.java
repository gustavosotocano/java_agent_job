package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.DictamenAccesoJpaEntity;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.DictamenAccesoSpringRepository;
import com.gustavo.jobagent.opportunities.application.port.out.DictamenAccesoRepository;
import com.gustavo.jobagent.opportunities.domain.DictamenAcceso;

import java.util.List;
import java.util.Optional;

public class JpaDictamenAccesoRepository implements DictamenAccesoRepository {

    private final DictamenAccesoSpringRepository dictamenes;

    public JpaDictamenAccesoRepository(DictamenAccesoSpringRepository dictamenes) {
        this.dictamenes = dictamenes;
    }

    @Override
    public List<DictamenAcceso> listar() {
        return dictamenes.findAll().stream().map(DictamenAccesoJpaEntity::aDominio).toList();
    }

    @Override
    public Optional<DictamenAcceso> porFuente(String fuenteId) {
        if (fuenteId == null || fuenteId.isBlank()) {
            return Optional.empty();
        }
        return dictamenes.findById(fuenteId.strip()).map(DictamenAccesoJpaEntity::aDominio);
    }
}
