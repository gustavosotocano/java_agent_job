package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.OfertaJpaEntity;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.OfertaSpringRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.ProcedenciaJpaEntity;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.ProcedenciaSpringRepository;
import com.gustavo.jobagent.opportunities.application.port.in.OfertaRegistrada;
import com.gustavo.jobagent.opportunities.application.port.out.IdentidadDuplicadaException;
import com.gustavo.jobagent.opportunities.application.port.out.OfertaLaboralRepository;
import com.gustavo.jobagent.opportunities.domain.IdentidadOferta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class JpaOfertaLaboralRepository implements OfertaLaboralRepository {

    private final OfertaSpringRepository ofertas;
    private final ProcedenciaSpringRepository procedencias;
    private final TransactionTemplate transacciones;

    public JpaOfertaLaboralRepository(
            OfertaSpringRepository ofertas,
            ProcedenciaSpringRepository procedencias,
            PlatformTransactionManager transactionManager
    ) {
        this.ofertas = ofertas;
        this.procedencias = procedencias;
        this.transacciones = new TransactionTemplate(transactionManager);
    }

    @Override
    public OfertaRegistrada insertarNueva(OfertaLaboral captura, IdentidadOferta identidad, Instant capturadoEn) {
        try {
            return transacciones.execute(status -> {
                OfertaJpaEntity oferta = new OfertaJpaEntity();
                oferta.setId(UUID.randomUUID().toString());
                oferta.reemplazarCon(captura, identidad.fuenteId(), identidad.urlNormalizada());
                ofertas.saveAndFlush(oferta);
                procedencias.saveAndFlush(ProcedenciaJpaEntity.desde(
                        UUID.randomUUID().toString(), oferta.getId(), capturadoEn, captura));
                return armar(oferta);
            });
        } catch (DataIntegrityViolationException e) {
            if (esDuplicado(e)) {
                throw new IdentidadDuplicadaException();
            }
            throw e;
        }
    }

    @Override
    public OfertaRegistrada agregarProcedencia(IdentidadOferta identidad, OfertaLaboral captura, Instant capturadoEn) {
        return transacciones.execute(status -> {
            OfertaJpaEntity oferta = ofertas.findByFuenteIdAndUrlNormalizada(identidad.fuenteId(), identidad.urlNormalizada())
                    .orElseThrow(IdentidadDuplicadaException::new);
            oferta.reemplazarCon(captura, identidad.fuenteId(), identidad.urlNormalizada());
            ofertas.save(oferta);
            procedencias.save(ProcedenciaJpaEntity.desde(
                    UUID.randomUUID().toString(), oferta.getId(), capturadoEn, captura));
            return armar(oferta);
        });
    }

    @Override
    public Optional<OfertaRegistrada> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return ofertas.findById(id.strip()).map(this::armar);
    }

    @Override
    public Optional<OfertaRegistrada> buscarPorIdentidad(IdentidadOferta identidad) {
        return ofertas.findByFuenteIdAndUrlNormalizada(identidad.fuenteId(), identidad.urlNormalizada()).map(this::armar);
    }

    private OfertaRegistrada armar(OfertaJpaEntity oferta) {
        var capturas = procedencias.findByOfertaIdOrderByCapturadoEnAsc(oferta.getId()).stream()
                .map(fila -> new OfertaRegistrada.ProcedenciaOferta(
                        fila.getCapturadoEn(),
                        fila.aDominio(oferta.getFuenteId())))
                .toList();
        return new OfertaRegistrada(
                oferta.getId(),
                new IdentidadOferta(oferta.getFuenteId(), oferta.getUrlNormalizada()),
                oferta.aDominio(),
                capturas
        );
    }

    private static boolean esDuplicado(DataIntegrityViolationException e) {
        Throwable causa = e.getMostSpecificCause();
        return causa instanceof SQLException sql && "23505".equals(sql.getSQLState());
    }
}
