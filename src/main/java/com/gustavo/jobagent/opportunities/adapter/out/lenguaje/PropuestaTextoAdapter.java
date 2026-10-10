package com.gustavo.jobagent.opportunities.adapter.out.lenguaje;

import com.gustavo.jobagent.opportunities.application.port.out.PropuestaLenguajePort;
import com.gustavo.jobagent.opportunities.domain.ObservacionPropuesta;
import com.gustavo.jobagent.opportunities.domain.OfertaLaboral;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;
import com.gustavo.jobagent.opportunities.domain.ResultadoPropuesta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Interpreta un texto local ya estructurado. No abre una conexión.
 */
public final class PropuestaTextoAdapter implements PropuestaLenguajePort {

    private final String texto;

    public PropuestaTextoAdapter(String texto) {
        this.texto = texto == null ? "" : texto;
    }

    @Override
    public ResultadoPropuesta proponer(OfertaLaboral oferta, PerfilProfesional perfil, Duration limite) {
        return leer(texto);
    }

    static ResultadoPropuesta leer(String bruto) {
        if (bruto == null || bruto.isBlank()) {
            return ilegible();
        }
        String[] bloques = bruto.strip().split("\\R\\s*\\R");
        List<ObservacionPropuesta> observaciones = new ArrayList<>();
        for (String bloque : bloques) {
            String texto = null;
            String fragmento = null;
            for (String linea : bloque.split("\\R")) {
                String limpia = linea.strip();
                if (limpia.isEmpty()) {
                    continue;
                }
                if (limpia.regionMatches(true, 0, "texto:", 0, "texto:".length())) {
                    if (texto != null) {
                        return ilegible();
                    }
                    texto = limpia.substring("texto:".length()).strip();
                } else if (limpia.regionMatches(true, 0, "fragmento:", 0, "fragmento:".length())) {
                    if (fragmento != null) {
                        return ilegible();
                    }
                    fragmento = limpia.substring("fragmento:".length()).strip();
                } else {
                    return ilegible();
                }
            }
            if (texto == null || fragmento == null || texto.isBlank() || fragmento.isBlank()) {
                return ilegible();
            }
            observaciones.add(new ObservacionPropuesta(texto, fragmento));
        }
        if (observaciones.isEmpty()) {
            return ilegible();
        }
        return new ResultadoPropuesta.Disponible(List.copyOf(observaciones));
    }

    private static ResultadoPropuesta ilegible() {
        return new ResultadoPropuesta.NoDisponible(ResultadoPropuesta.Motivo.ILEGIBLE);
    }
}
