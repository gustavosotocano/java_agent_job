package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.domain.DictamenAcceso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "dictamen_acceso")
public class DictamenAccesoJpaEntity {

    @Id
    @Column(name = "fuente_id")
    private String fuenteId;

    @Column(name = "via_comprobada", nullable = false)
    private boolean viaComprobada;

    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "clase_acceso")
    private DictamenAcceso.ClaseAcceso claseAcceso;

    private String cobertura;
    private String limites;
    private String actualidad;

    @Column(name = "verificado_en", nullable = false)
    private Instant verificadoEn;

    public DictamenAcceso aDominio() {
        return new DictamenAcceso(
                fuenteId,
                viaComprobada,
                motivo,
                claseAcceso,
                cobertura,
                limites,
                actualidad,
                verificadoEn
        );
    }
}
