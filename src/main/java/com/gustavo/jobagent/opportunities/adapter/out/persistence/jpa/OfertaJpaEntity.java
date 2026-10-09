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
@Table(name = "oferta")
public class OfertaJpaEntity {

    @Id
    private String id;

    @Column(name = "fuente_id", nullable = false)
    private String fuenteId;

    @Column(name = "url_normalizada", nullable = false)
    private String urlNormalizada;

    @Column(name = "id_fuente", nullable = false)
    private String idFuente;

    @Column(nullable = false)
    private String titulo;

    private String empresa;
    private String descripcion;
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    private ModalidadTrabajo modalidad;

    @Column(name = "url_original", nullable = false)
    private String urlOriginal;

    @Column(name = "fecha_publicacion")
    private Instant fechaPublicacion;

    @Embedded
    private SalarioEmbebido salario;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "oferta_tecnologia", joinColumns = @JoinColumn(name = "oferta_id"))
    @OrderColumn(name = "orden")
    @Column(name = "tecnologia")
    private List<String> tecnologias = new ArrayList<>();

    public void reemplazarCon(OfertaLaboral captura, String fuenteId, String urlNormalizada) {
        this.fuenteId = fuenteId;
        this.urlNormalizada = urlNormalizada;
        this.idFuente = captura.idFuente();
        this.titulo = captura.titulo();
        this.empresa = captura.empresa();
        this.descripcion = captura.descripcion();
        this.ubicacion = captura.ubicacion();
        this.modalidad = captura.modalidad();
        this.urlOriginal = captura.urlOriginal();
        this.fechaPublicacion = captura.fechaPublicacion();
        this.salario = SalarioEmbebido.desde(captura.salario());
        this.tecnologias.clear();
        this.tecnologias.addAll(captura.tecnologias());
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFuenteId() {
        return fuenteId;
    }

    public String getUrlNormalizada() {
        return urlNormalizada;
    }
}
