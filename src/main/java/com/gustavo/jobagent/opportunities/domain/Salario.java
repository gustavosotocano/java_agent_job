package com.gustavo.jobagent.opportunities.domain;

import java.math.BigDecimal;

/**
 * Representa la remuneración asociada a una oferta laboral.
 */
public record Salario(
        BigDecimal minimo,
        BigDecimal maximo,
        String moneda,
        PeriodoSalarial periodo
) {
    public Salario {
        if (minimo != null && maximo != null && minimo.compareTo(maximo) > 0) {
            throw new IllegalArgumentException("El salario mínimo no puede ser mayor que el salario máximo");
        }
        if ((minimo != null || maximo != null) && (moneda == null || moneda.isBlank())) {
            throw new IllegalArgumentException("La moneda es obligatoria cuando se especifica un importe salarial");
        }
        moneda = (moneda != null) ? moneda.strip().toUpperCase() : null;
    }

    public static Salario mensual(BigDecimal minimo, BigDecimal maximo, String moneda) {
        return new Salario(minimo, maximo, moneda, PeriodoSalarial.MENSUAL);
    }

    public static Salario anual(BigDecimal minimo, BigDecimal maximo, String moneda) {
        return new Salario(minimo, maximo, moneda, PeriodoSalarial.ANUAL);
    }

    public static Salario hora(BigDecimal minimo, BigDecimal maximo, String moneda) {
        return new Salario(minimo, maximo, moneda, PeriodoSalarial.HORA);
    }

    public enum PeriodoSalarial {
        HORA,
        MENSUAL,
        ANUAL
    }
}
