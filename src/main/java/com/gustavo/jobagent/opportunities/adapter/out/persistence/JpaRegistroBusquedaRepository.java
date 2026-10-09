package com.gustavo.jobagent.opportunities.adapter.out.persistence;

import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.AdvertenciaEmbebida;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.RegistroBusquedaJpaEntity;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.RegistroBusquedaSpringRepository;
import com.gustavo.jobagent.opportunities.application.port.in.AdvertenciaFuente;
import com.gustavo.jobagent.opportunities.application.port.in.RegistroBusqueda;
import com.gustavo.jobagent.opportunities.application.port.out.RegistroBusquedaRepository;
import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

public class JpaRegistroBusquedaRepository implements RegistroBusquedaRepository {

    private final RegistroBusquedaSpringRepository registros;

    public JpaRegistroBusquedaRepository(RegistroBusquedaSpringRepository registros) {
        this.registros = registros;
    }

    @Override
    @Transactional
    public RegistroBusqueda guardar(RegistroBusqueda registro) {
        RegistroBusquedaJpaEntity fila = new RegistroBusquedaJpaEntity();
        fila.setId(registro.id());
        fila.setEjecutadoEn(registro.ejecutadoEn());
        fila.setCargo(registro.criterios().cargo());
        fila.setUbicacion(registro.criterios().ubicacion());
        fila.setModalidad(registro.criterios().modalidad());
        fila.setLimite(registro.criterios().limite());
        fila.setResultado(registro.resultado());
        fila.setCantidadOfertasDevueltas(registro.cantidadOfertasDevueltas());
        fila.getTecnologias().addAll(registro.criterios().tecnologias());
        fila.getFuentesConsultadas().addAll(registro.fuentesConsultadas());
        registro.advertencias().forEach(advertencia ->
                fila.getAdvertencias().add(AdvertenciaEmbebida.desde(advertencia.fuenteId(), advertencia.mensaje())));
        registros.saveAndFlush(fila);
        return registro;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegistroBusqueda> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return registros.findById(id.strip()).map(this::aDominio);
    }

    private RegistroBusqueda aDominio(RegistroBusquedaJpaEntity fila) {
        return new RegistroBusqueda(
                fila.getId(),
                fila.getEjecutadoEn(),
                new CriteriosBusqueda(
                        fila.getCargo(),
                        new ArrayList<>(fila.getTecnologias()),
                        fila.getUbicacion(),
                        fila.getModalidad(),
                        fila.getLimite()
                ),
                new ArrayList<>(fila.getFuentesConsultadas()),
                fila.getAdvertencias().stream()
                        .map(advertencia -> new AdvertenciaFuente(advertencia.getFuenteId(), advertencia.getMensaje()))
                        .toList(),
                fila.getResultado(),
                fila.getCantidadOfertasDevueltas()
        );
    }
}
