package com.gustavo.jobagent.opportunities.adapter.in.web;

import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.Salario;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * DTO de respuesta para el endpoint POST /api/ofertas/busquedas.
 */
public record BuscarOfertasResponse(
        List<OfertaLaboralResponse> ofertas,
        List<String> fuentesConsultadas,
        List<AdvertenciaFuenteResponse> advertencias
) {

    public record OfertaLaboralResponse(
            String idFuente,
            String fuenteId,
            String titulo,
            String empresa,
            String descripcion,
            String ubicacion,
            ModalidadTrabajo modalidad,
            String urlOriginal,
            Instant fechaPublicacion,
            SalarioResponse salario,
            Set<String> tecnologias
    ) {
        public static OfertaLaboralResponse fromDomain(OfertaLaboral domain) {
            return new OfertaLaboralResponse(
                    domain.idFuente(),
                    domain.fuenteId(),
                    domain.titulo(),
                    domain.empresa(),
                    domain.descripcion(),
                    domain.ubicacion(),
                    domain.modalidad(),
                    domain.urlOriginal(),
                    domain.fechaPublicacion(),
                    domain.salario() != null ? SalarioResponse.fromDomain(domain.salario()) : null,
                    domain.tecnologias()
            );
        }
    }

    public record SalarioResponse(
            BigDecimal minimo,
            BigDecimal maximo,
            String moneda,
            String periodo
    ) {
        public static SalarioResponse fromDomain(Salario domain) {
            return new SalarioResponse(
                    domain.minimo(),
                    domain.maximo(),
                    domain.moneda(),
                    domain.periodo() != null ? domain.periodo().name() : null
            );
        }
    }

    public record AdvertenciaFuenteResponse(
            String fuenteId,
            String mensaje
    ) {
    }
}
