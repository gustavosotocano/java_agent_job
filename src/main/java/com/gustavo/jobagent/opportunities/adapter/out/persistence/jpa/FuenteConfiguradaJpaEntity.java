package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.application.port.in.FuenteConfigurada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fuente_configurada")
public class FuenteConfiguradaJpaEntity {

    @Id
    @Column(name = "fuente_id")
    private String fuenteId;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuenteConfigurada.EstadoFuente estado;

    @Column(nullable = false)
    private boolean habilitada;

    public static FuenteConfiguradaJpaEntity desde(FuenteConfigurada fuente) {
        FuenteConfiguradaJpaEntity fila = new FuenteConfiguradaJpaEntity();
        fila.fuenteId = fuente.fuenteId();
        fila.nombre = fuente.nombre();
        fila.estado = fuente.estado();
        fila.habilitada = fuente.habilitada();
        return fila;
    }

    public FuenteConfigurada aDominio() {
        return new FuenteConfigurada(fuenteId, nombre, estado, habilitada);
    }
}
