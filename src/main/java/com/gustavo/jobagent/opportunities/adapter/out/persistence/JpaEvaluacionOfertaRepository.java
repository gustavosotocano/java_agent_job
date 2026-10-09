package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.EvaluacionJpaEntity;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.EvaluacionOfertaEvidenciaJpaEntity;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.EvaluacionOfertaEvidenciaSpringRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.EvaluacionPerfilEvidenciaJpaEntity;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.EvaluacionPerfilEvidenciaSpringRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.EvaluacionSpringRepository;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluacionAlmacenada;
import com.gustavo.jobagent.opportunities.application.port.out.EvaluacionOfertaRepository;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JpaEvaluacionOfertaRepository implements EvaluacionOfertaRepository {

    private final EvaluacionSpringRepository evaluaciones;
    private final EvaluacionOfertaEvidenciaSpringRepository ofertas;
    private final EvaluacionPerfilEvidenciaSpringRepository perfiles;
    private final TransactionTemplate transacciones;

    public JpaEvaluacionOfertaRepository(
            EvaluacionSpringRepository evaluaciones,
            EvaluacionOfertaEvidenciaSpringRepository ofertas,
            EvaluacionPerfilEvidenciaSpringRepository perfiles,
            PlatformTransactionManager transactionManager
    ) {
        this.evaluaciones = evaluaciones;
        this.ofertas = ofertas;
        this.perfiles = perfiles;
        this.transacciones = new TransactionTemplate(transactionManager);
    }

    @Override
    public EvaluacionOferta guardar(EvaluacionOferta evaluacion) {
        Objects.requireNonNull(evaluacion, "La evaluación no puede ser nula");
        return transacciones.execute(status -> evaluaciones.save(EvaluacionJpaEntity.desde(evaluacion)).aDominio());
    }

    @Override
    public EvaluacionOferta guardarConEvidencia(EvaluacionOferta evaluacion, PerfilProfesional perfil, OfertaLaboral oferta) {
        Objects.requireNonNull(evaluacion, "La evaluación no puede ser nula");
        return transacciones.execute(status -> {
            evaluaciones.save(EvaluacionJpaEntity.desde(evaluacion));
            ofertas.save(EvaluacionOfertaEvidenciaJpaEntity.desde(evaluacion.id(), oferta));
            perfiles.save(EvaluacionPerfilEvidenciaJpaEntity.desde(evaluacion.id(), perfil));
            return evaluacion;
        });
    }

    @Override
    public Optional<EvaluacionOferta> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return evaluaciones.findById(id.strip()).map(EvaluacionJpaEntity::aDominio);
    }

    @Override
    public Optional<EvaluacionAlmacenada> buscarAlmacenadaPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String limpio = id.strip();
        return evaluaciones.findById(limpio).map(fila -> new EvaluacionAlmacenada(
                fila.aDominio(),
                perfiles.findById(limpio).orElseThrow().aDominio(),
                ofertas.findById(limpio).orElseThrow().aDominio()
        ));
    }

    @Override
    public List<EvaluacionOferta> buscarPorPerfil(String idPerfil) {
        if (idPerfil == null || idPerfil.isBlank()) {
            return List.of();
        }
        String limpio = idPerfil.strip();
        return evaluaciones.findAll().stream()
                .map(EvaluacionJpaEntity::aDominio)
                .filter(evaluacion -> evaluacion.idPerfil().equals(limpio))
                .toList();
    }

    @Override
    public List<EvaluacionOferta> buscarPorOferta(String idOferta) {
        if (idOferta == null || idOferta.isBlank()) {
            return List.of();
        }
        String limpio = idOferta.strip();
        return evaluaciones.findAll().stream()
                .map(EvaluacionJpaEntity::aDominio)
                .filter(evaluacion -> evaluacion.idOferta().equals(limpio))
                .toList();
    }
}
