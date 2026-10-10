package com.gustavo.jobagent.opportunities.domain;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

/**
 * Dictamen de si un portal tiene una vía pública permitida para buscar ofertas.
 */
public record DictamenAcceso(
        String fuenteId,
        boolean viaComprobada,
        String motivo,
        ClaseAcceso claseAcceso,
        String cobertura,
        String limites,
        String actualidad,
        Instant verificadoEn
) {
    public static final Set<String> PORTALES = Set.of("LINKEDIN", "INDEED", "GLASSDOOR");

    public enum ClaseAcceso {
        LISTADO_PUBLICO,
        FEED,
        PROVEEDOR_AUTORIZADO,
        BUSQUEDA_PUBLICA_INDEXADA
    }

    public DictamenAcceso {
        if (fuenteId == null || fuenteId.isBlank() || !PORTALES.contains(fuenteId.strip())) {
            throw new IllegalArgumentException("El fuenteId debe ser LINKEDIN, INDEED o GLASSDOOR");
        }
        fuenteId = fuenteId.strip();
        if (verificadoEn == null) {
            throw new IllegalArgumentException("La fecha de verificación es obligatoria");
        }
        if (!viaComprobada) {
            if (motivo == null || motivo.isBlank()) {
                throw new IllegalArgumentException("Una vía no comprobada exige motivo");
            }
            if (claseAcceso != null || informado(cobertura) || informado(limites) || informado(actualidad)) {
                throw new IllegalArgumentException("Una vía no comprobada deja vacíos la clase, la cobertura, los límites y la actualidad");
            }
            motivo = motivo.strip();
            cobertura = null;
            limites = null;
            actualidad = null;
        } else {
            if (claseAcceso == null || !informado(cobertura) || !informado(limites) || !informado(actualidad)) {
                throw new IllegalArgumentException("Una vía comprobada exige clase, cobertura, límites y actualidad");
            }
            if (exigeCuentaOLogin(motivo)) {
                throw new IllegalArgumentException("Una vía que exige cuenta personal o iniciar sesión no puede estar comprobada");
            }
            motivo = motivo == null ? null : motivo.strip();
            cobertura = cobertura.strip();
            limites = limites.strip();
            actualidad = actualidad.strip();
        }
    }

    public static DictamenAcceso noComprobada(String fuenteId, String motivo, Instant verificadoEn) {
        return new DictamenAcceso(fuenteId, false, motivo, null, null, null, null, verificadoEn);
    }

    private static boolean informado(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static boolean exigeCuentaOLogin(String motivo) {
        if (motivo == null) {
            return false;
        }
        String normalizado = motivo.toLowerCase(Locale.ROOT);
        return normalizado.contains("cuenta personal") || normalizado.contains("iniciar sesión")
                || normalizado.contains("inicio de sesión");
    }
}
