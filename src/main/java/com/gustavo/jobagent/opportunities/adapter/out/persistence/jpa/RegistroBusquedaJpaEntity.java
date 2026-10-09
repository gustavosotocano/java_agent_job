package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.application.port.in.ResultadoRegistroBusqueda;
import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
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
@Table(name = "registro_busqueda")
public class RegistroBusquedaJpaEntity {

    @Id
    private String id;

    @Column(name = "ejecutado_en", nullable = false)
    private Instant ejecutadoEn;

    @Column(nullable = false)
    private String cargo;

    private String ubicacion;

    @Enumerated(EnumType.STRING)
    private ModalidadTrabajo modalidad;

    @Column(nullable = false)
    private int limite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoRegistroBusqueda resultado;

    @Column(name = "cantidad_ofertas_devueltas", nullable = false)
    private int cantidadOfertasDevueltas;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "registro_busqueda_tecnologia", joinColumns = @JoinColumn(name = "registro_id"))
    @OrderColumn(name = "orden")
    @Column(name = "tecnologia")
    private List<String> tecnologias = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "registro_busqueda_fuente", joinColumns = @JoinColumn(name = "registro_id"))
    @OrderColumn(name = "orden")
    @Column(name = "fuente_id")
    private List<String> fuentesConsultadas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "registro_busqueda_advertencia", joinColumns = @JoinColumn(name = "registro_id"))
    @OrderColumn(name = "orden")
    private List<AdvertenciaEmbebida> advertencias = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getEjecutadoEn() {
        return ejecutadoEn;
    }

    public void setEjecutadoEn(Instant ejecutadoEn) {
        this.ejecutadoEn = ejecutadoEn;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public ModalidadTrabajo getModalidad() {
        return modalidad;
    }

    public void setModalidad(ModalidadTrabajo modalidad) {
        this.modalidad = modalidad;
    }

    public int getLimite() {
        return limite;
    }

    public void setLimite(int limite) {
        this.limite = limite;
    }

    public ResultadoRegistroBusqueda getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoRegistroBusqueda resultado) {
        this.resultado = resultado;
    }

    public int getCantidadOfertasDevueltas() {
        return cantidadOfertasDevueltas;
    }

    public void setCantidadOfertasDevueltas(int cantidadOfertasDevueltas) {
        this.cantidadOfertasDevueltas = cantidadOfertasDevueltas;
    }

    public List<String> getTecnologias() {
        return tecnologias;
    }

    public List<String> getFuentesConsultadas() {
        return fuentesConsultadas;
    }

    public List<AdvertenciaEmbebida> getAdvertencias() {
        return advertencias;
    }
}
