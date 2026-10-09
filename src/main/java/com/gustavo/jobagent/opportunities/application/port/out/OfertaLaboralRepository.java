package com.gustavo.jobagent.opportunities.application.port.out;

import com.gustavo.jobagent.opportunities.application.port.in.OfertaRegistrada;
import com.gustavo.jobagent.opportunities.domain.IdentidadOferta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;

import java.time.Instant;
import java.util.Optional;

public interface OfertaLaboralRepository {

    OfertaRegistrada insertarNueva(OfertaLaboral captura, IdentidadOferta identidad, Instant capturadoEn);

    OfertaRegistrada agregarProcedencia(IdentidadOferta identidad, OfertaLaboral captura, Instant capturadoEn);

    Optional<OfertaRegistrada> buscarPorId(String id);

    Optional<OfertaRegistrada> buscarPorIdentidad(IdentidadOferta identidad);
}
