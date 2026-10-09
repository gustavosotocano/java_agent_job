package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import com.gustavo.jobagent.opportunities.domain.Salario;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

@Embeddable
public class SalarioEmbebido {

    @Column(name = "salario_minimo")
    private BigDecimal minimo;

    @Column(name = "salario_maximo")
    private BigDecimal maximo;

    @Column(name = "salario_moneda")
    private String moneda;

    @Enumerated(EnumType.STRING)
    @Column(name = "salario_periodo")
    private Salario.PeriodoSalarial periodo;

    public static SalarioEmbebido desde(Salario salario) {
        if (salario == null) {
            return null;
        }
        SalarioEmbebido embebido = new SalarioEmbebido();
        embebido.minimo = salario.minimo();
        embebido.maximo = salario.maximo();
        embebido.moneda = salario.moneda();
        embebido.periodo = salario.periodo();
        return embebido;
    }

    public Salario aDominio() {
        if (minimo == null && maximo == null && moneda == null && periodo == null) {
            return null;
        }
        return new Salario(minimo, maximo, moneda, periodo);
    }
}
