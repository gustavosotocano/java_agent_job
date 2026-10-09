package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Entity
@Table(name = "evaluacion_perfil_evidencia")
public class EvaluacionPerfilEvidenciaJpaEntity {

    @Id
    @Column(name = "evaluacion_id")
    private String evaluacionId;

    @Column(name = "perfil_id", nullable = false)
    private String perfilId;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "titulo_principal", nullable = false)
    private String tituloPrincipal;

    private String ubicacion;

    @Column(name = "anos_experiencia")
    private Integer anosExperiencia;

    @Embedded
    private SalarioEmbebido salario;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evaluacion_perfil_tecnologia", joinColumns = @JoinColumn(name = "evaluacion_id"))
    @OrderColumn(name = "orden")
    @Column(name = "tecnologia")
    private List<String> tecnologias = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evaluacion_perfil_modalidad", joinColumns = @JoinColumn(name = "evaluacion_id"))
    @OrderColumn(name = "orden")
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad")
    private List<ModalidadTrabajo> modalidades = new ArrayList<>();

    public static EvaluacionPerfilEvidenciaJpaEntity desde(String evaluacionId, PerfilProfesional perfil) {
        EvaluacionPerfilEvidenciaJpaEntity fila = new EvaluacionPerfilEvidenciaJpaEntity();
        fila.evaluacionId = evaluacionId;
        fila.perfilId = perfil.id();
        fila.nombre = perfil.nombre();
        fila.tituloPrincipal = perfil.tituloPrincipal();
        fila.ubicacion = perfil.ubicacion();
        fila.anosExperiencia = perfil.anosExperiencia();
        fila.salario = SalarioEmbebido.desde(perfil.expectativaSalarial());
        fila.tecnologias = new ArrayList<>(perfil.tecnologias());
        fila.modalidades = new ArrayList<>(perfil.modalidadesAceptadas());
        return fila;
    }

    public PerfilProfesional aDominio() {
        return PerfilProfesional.builder()
                .id(perfilId)
                .nombre(nombre)
                .tituloPrincipal(tituloPrincipal)
                .ubicacion(ubicacion)
                .anosExperiencia(anosExperiencia)
                .expectativaSalarial(salario == null ? null : salario.aDominio())
                .tecnologias(new LinkedHashSet<>(tecnologias))
                .modalidadesAceptadas(new LinkedHashSet<>(modalidades))
                .build();
    }
}
