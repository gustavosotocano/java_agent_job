package com.gustavo.jobagent.opportunities.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalarioTest {

    @Test
    @DisplayName("Debe crear un salario válido con rango, moneda y período")
    void debeCrearSalarioValido() {
        Salario salario = new Salario(
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(5000),
                "usd",
                Salario.PeriodoSalarial.MENSUAL
        );

        assertThat(salario.minimo()).isEqualByComparingTo("3000");
        assertThat(salario.maximo()).isEqualByComparingTo("5000");
        assertThat(salario.moneda()).isEqualTo("USD");
        assertThat(salario.periodo()).isEqualTo(Salario.PeriodoSalarial.MENSUAL);
    }

    @Test
    @DisplayName("Debe rechazar salario mínimo mayor que el máximo")
    void debeRechazarMinimoMayorQueMaximo() {
        assertThatThrownBy(() -> new Salario(
                BigDecimal.valueOf(6000),
                BigDecimal.valueOf(4000),
                "USD",
                Salario.PeriodoSalarial.MENSUAL
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El salario mínimo no puede ser mayor que el salario máximo");
    }

    @Test
    @DisplayName("Debe rechazar importe sin moneda especificada")
    void debeRechazarImporteSinMoneda() {
        assertThatThrownBy(() -> new Salario(
                BigDecimal.valueOf(3000),
                null,
                "   ",
                Salario.PeriodoSalarial.MENSUAL
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La moneda es obligatoria");
    }
}
