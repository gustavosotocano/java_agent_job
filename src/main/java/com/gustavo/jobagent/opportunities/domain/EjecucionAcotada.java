package com.gustavo.jobagent.opportunities.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Parte una lista de pasos antes de llamar a ninguna herramienta.
 */
public final class EjecucionAcotada {

    public static final int MAXIMO = 4;

    public static final Set<String> AUTORIZADAS = Set.of(
            "buscarOfertas",
            "consultarPerfil",
            "consultarOferta",
            "guardarEvaluacion"
    );

    private EjecucionAcotada() {
    }

    public record Solicitud(String herramienta, CriteriosBusqueda criterios, String id) {
    }

    public enum EstadoEjecucion {
        COMPLETADA,
        LIMITE_ALCANZADO,
        HERRAMIENTA_NO_AUTORIZADA
    }

    public record Plan(
            EstadoEjecucion estado,
            List<Solicitud> aEjecutar,
            Solicitud rechazada,
            List<String> noEjecutadas
    ) {
    }

    public static Plan planear(List<Solicitud> pasos) {
        if (pasos == null || pasos.isEmpty()) {
            return new Plan(EstadoEjecucion.COMPLETADA, List.of(), null, List.of());
        }
        List<Solicitud> ejecutar = new ArrayList<>();
        for (int indice = 0; indice < pasos.size(); indice++) {
            Solicitud paso = pasos.get(indice);
            String nombre = paso == null || paso.herramienta() == null ? "" : paso.herramienta();
            if (!AUTORIZADAS.contains(nombre)) {
                return new Plan(
                        EstadoEjecucion.HERRAMIENTA_NO_AUTORIZADA,
                        List.copyOf(ejecutar),
                        paso == null ? new Solicitud("", null, null) : paso,
                        nombresDesde(pasos, indice + 1)
                );
            }
            if (ejecutar.size() == MAXIMO) {
                return new Plan(
                        EstadoEjecucion.LIMITE_ALCANZADO,
                        List.copyOf(ejecutar),
                        null,
                        nombresDesde(pasos, indice)
                );
            }
            ejecutar.add(paso);
        }
        return new Plan(EstadoEjecucion.COMPLETADA, List.copyOf(ejecutar), null, List.of());
    }

    private static List<String> nombresDesde(List<Solicitud> pasos, int desde) {
        List<String> nombres = new ArrayList<>();
        for (int indice = desde; indice < pasos.size(); indice++) {
            Solicitud paso = pasos.get(indice);
            nombres.add(paso == null || paso.herramienta() == null ? "" : paso.herramienta());
        }
        return List.copyOf(nombres);
    }
}
