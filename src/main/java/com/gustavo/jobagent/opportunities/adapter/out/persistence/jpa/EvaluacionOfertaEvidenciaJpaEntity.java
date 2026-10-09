package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.domain.IdentidadOferta;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
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
import java.util.LinkedHashSet;
import java.util.List;

@Entity
@Table(name = "evaluacion_oferta_evidencia")
public class EvaluacionOfertaEvidenciaJpaEntity {

    @Id
    @Column(name = "evaluacion_id")
    private String evaluacionId;

    @Column(name = "id_fuente", nullable = false)
    private String idFuente;

    @Column(name = "fuente_id", nullable = false)
    private String fuenteId;

    @Column(nullable = false)
    private String titulo;

    private String empresa;
    private String descripcion;
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    private ModalidadTrabajo modalidad;

    @Column(name = "url_original")
    private String urlOriginal;

    @Column(name = "url_normalizada")
    private String urlNormalizada;

    @Column(name = "fecha_publicacion")
    private Instant fechaPublicacion;

    @Embedded
    private SalarioEmbebido salario;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evaluacion_oferta_tecnologia", joinColumns = @JoinColumn(name = "evaluacion_id"))
    @OrderColumn(name = "orden")
    @Column(name = "tecnologia")
    private List<String> tecnologias = new ArrayList<>();

    public static EvaluacionOfertaEvidenciaJpaEntity desde(String evaluacionId, OfertaLaboral oferta) {
        EvaluacionOfertaEvidenciaJpaEntity fila = new EvaluacionOfertaEvidenciaJpaEntity();
        fila.evaluacionId = evaluacionId;
        fila.idFuente = oferta.idFuente();
        fila.fuenteId = oferta.fuenteId();
        fila.titulo = oferta.titulo();
        fila.empresa = oferta.empresa();
        fila.descripcion = oferta.descripcion();
        fila.ubicacion = oferta.ubicacion();
        fila.modalidad = oferta.modalidad();
        fila.urlOriginal = oferta.urlOriginal();
        fila.urlNormalizada = normalizar(oferta);
        fila.fechaPublicacion = oferta.fechaPublicacion();
        fila.salario = SalarioEmbebido.desde(oferta.salario());
        fila.tecnologias = new ArrayList<>(oferta.tecnologias());
        return fila;
    }

    private static String normalizar(OfertaLaboral oferta) {
        try {
            return IdentidadOferta.de(oferta.fuenteId(), oferta.urlOriginal()).urlNormalizada();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public OfertaLaboral aDominio() {
        return OfertaLaboral.builder()
                .idFuente(idFuente)
                .fuenteId(fuenteId)
                .titulo(titulo)
                .empresa(empresa)
                .descripcion(descripcion)
                .ubicacion(ubicacion)
                .modalidad(modalidad)
                .urlOriginal(urlOriginal)
                .fechaPublicacion(fechaPublicacion)
                .salario(salario == null ? null : salario.aDominio())
                .tecnologias(new LinkedHashSet<>(tecnologias))
                .build();
    }
}
