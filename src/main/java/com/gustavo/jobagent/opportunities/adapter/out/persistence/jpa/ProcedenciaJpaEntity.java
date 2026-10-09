package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
@Table(name = "procedencia")
public class ProcedenciaJpaEntity {

    @Id
    private String id;

    @Column(name = "oferta_id", nullable = false)
    private String ofertaId;

    @Column(name = "capturado_en", nullable = false)
    private Instant capturadoEn;

    @Column(name = "id_fuente", nullable = false)
    private String idFuente;

    @Column(name = "url_original", nullable = false)
    private String urlOriginal;

    @Column(nullable = false)
    private String titulo;

    private String empresa;
    private String descripcion;
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    private ModalidadTrabajo modalidad;

    @Column(name = "fecha_publicacion")
    private Instant fechaPublicacion;

    @Embedded
    private SalarioEmbebido salario;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "procedencia_tecnologia", joinColumns = @JoinColumn(name = "procedencia_id"))
    @OrderColumn(name = "orden")
    @Column(name = "tecnologia")
    private List<String> tecnologias = new ArrayList<>();

    public static ProcedenciaJpaEntity desde(String id, String ofertaId, Instant capturadoEn, OfertaLaboral captura) {
        ProcedenciaJpaEntity fila = new ProcedenciaJpaEntity();
        fila.id = id;
        fila.ofertaId = ofertaId;
        fila.capturadoEn = capturadoEn;
        fila.idFuente = captura.idFuente();
        fila.urlOriginal = captura.urlOriginal();
        fila.titulo = captura.titulo();
        fila.empresa = captura.empresa();
        fila.descripcion = captura.descripcion();
        fila.ubicacion = captura.ubicacion();
        fila.modalidad = captura.modalidad();
        fila.fechaPublicacion = captura.fechaPublicacion();
        fila.salario = SalarioEmbebido.desde(captura.salario());
        fila.tecnologias = new ArrayList<>(captura.tecnologias());
        return fila;
    }

    public OfertaLaboral aDominio(String fuenteId) {
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

    public Instant getCapturadoEn() {
        return capturadoEn;
    }
}
