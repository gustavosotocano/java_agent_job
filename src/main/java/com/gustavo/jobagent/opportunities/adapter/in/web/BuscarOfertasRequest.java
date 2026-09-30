package com.gustavo.jobagent.opportunities.adapter.in.web;

import com.gustavo.jobagent.opportunities.domain.ModalidadTrabajo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * DTO de solicitud para el endpoint POST /api/ofertas/busquedas.
 */
public record BuscarOfertasRequest(
        @NotBlank(message = "El cargo es obligatorio y no puede estar vacío")
        String cargo,

        List<String> tecnologias,

        String ubicacion,

        ModalidadTrabajo modalidad,

        @Min(value = 1, message = "El límite mínimo permitido es 1")
        @Max(value = 100, message = "El límite máximo permitido es 100")
        Integer limite
) {
}
