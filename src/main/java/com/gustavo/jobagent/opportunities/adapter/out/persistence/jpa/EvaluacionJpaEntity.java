package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.domain.EstadoEvaluacion;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluacion")
public class EvaluacionJpaEntity {

    @Id
    private String id;

    @Column(name = "id_oferta", nullable = false)
    private String idOferta;

    @Column(name = "id_perfil", nullable = false)
    private String idPerfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvaluacion estado;

    @Column(name = "fecha_evaluacion", nullable = false)
    private Instant fechaEvaluacion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evaluacion_coincidencia", joinColumns = @JoinColumn(name = "evaluacion_id"))
    @OrderColumn(name = "orden")
    private List<EvaluacionCoincidenciaJpaEntity> coincidencias = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evaluacion_brecha", joinColumns = @JoinColumn(name = "evaluacion_id"))
    @OrderColumn(name = "orden")
    private List<EvaluacionBrechaJpaEntity> brechas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evaluacion_dato_pendiente", joinColumns = @JoinColumn(name = "evaluacion_id"))
    @OrderColumn(name = "orden")
    private List<EvaluacionDatoPendienteJpaEntity> datosPendientes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evaluacion_observacion", joinColumns = @JoinColumn(name = "evaluacion_id"))
    @OrderColumn(name = "orden")
    private List<EvaluacionObservacionJpaEntity> observaciones = new ArrayList<>();

    public static EvaluacionJpaEntity desde(EvaluacionOferta evaluacion) {
        EvaluacionJpaEntity fila = new EvaluacionJpaEntity();
        fila.id = evaluacion.id();
        fila.idOferta = evaluacion.idOferta();
        fila.idPerfil = evaluacion.idPerfil();
        fila.estado = evaluacion.estado();
        fila.fechaEvaluacion = evaluacion.fechaEvaluacion();
        fila.coincidencias = new ArrayList<>(evaluacion.coincidencias().stream().map(EvaluacionCoincidenciaJpaEntity::desde).toList());
        fila.brechas = new ArrayList<>(evaluacion.brechas().stream().map(EvaluacionBrechaJpaEntity::desde).toList());
        fila.datosPendientes = new ArrayList<>(evaluacion.datosPendientes().stream().map(EvaluacionDatoPendienteJpaEntity::desde).toList());
        fila.observaciones = new ArrayList<>(evaluacion.observaciones().stream().map(EvaluacionObservacionJpaEntity::desde).toList());
        return fila;
    }

    public EvaluacionOferta aDominio() {
        return new EvaluacionOferta(
                id,
                idOferta,
                idPerfil,
                estado,
                coincidencias.stream().map(EvaluacionCoincidenciaJpaEntity::aDominio).toList(),
                brechas.stream().map(EvaluacionBrechaJpaEntity::aDominio).toList(),
                datosPendientes.stream().map(EvaluacionDatoPendienteJpaEntity::aDominio).toList(),
                fechaEvaluacion,
                observaciones == null
                        ? List.of()
                        : observaciones.stream().map(EvaluacionObservacionJpaEntity::aDominio).toList()
        );
    }
}
