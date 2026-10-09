package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.FuenteConfiguradaJpaEntity;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.FuenteConfiguradaSpringRepository;
import com.gustavo.jobagent.opportunities.application.port.in.FuenteConfigurada;
import com.gustavo.jobagent.opportunities.application.port.out.FuenteConfiguradaRepository;

import java.util.List;
import java.util.Optional;

public class JpaFuenteConfiguradaRepository implements FuenteConfiguradaRepository {

    private final FuenteConfiguradaSpringRepository fuentes;

    public JpaFuenteConfiguradaRepository(FuenteConfiguradaSpringRepository fuentes) {
        this.fuentes = fuentes;
    }

    @Override
    public Optional<FuenteConfigurada> buscarPorId(String fuenteId) {
        if (fuenteId == null || fuenteId.isBlank()) {
            return Optional.empty();
        }
        return fuentes.findById(fuenteId.strip()).map(FuenteConfiguradaJpaEntity::aDominio);
    }

    @Override
    public List<FuenteConfigurada> listar() {
        return fuentes.findAll().stream().map(FuenteConfiguradaJpaEntity::aDominio).toList();
    }

    @Override
    public FuenteConfigurada guardar(FuenteConfigurada fuente) {
        return fuentes.save(FuenteConfiguradaJpaEntity.desde(fuente)).aDominio();
    }
}
