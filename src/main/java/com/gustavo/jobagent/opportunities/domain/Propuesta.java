package com.gustavo.jobagent.opportunities.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filtra observaciones candidatas. No modifica el estado ni el desglose calculado.
 */
public final class Propuesta {

    private static final Pattern NUMERO = Pattern.compile("\\d+");
    private static final Pattern TOKEN_TECNICO = Pattern.compile("[A-Z][A-Za-z0-9+#.]{2,}");
    private static final List<String> MODALIDADES = List.of(
            "remote", "remoto", "hybrid", "hibrido", "híbrido", "onsite", "presencial"
    );
    private static final List<Pattern> ESTADOS = List.of(
            Pattern.compile("(?i)COMPATIBLE_CON_BRECHAS"),
            Pattern.compile("(?i)NO_COMPATIBLE"),
            Pattern.compile("(?i)\\bCOMPATIBLE\\b")
    );
    private static final List<String> INSTRUCCIONES = List.of(
            "postul", "iniciar sesión", "inicio de sesión", "login"
    );

    private Propuesta() {
    }

    public static List<ObservacionPropuesta> aceptar(
            List<ObservacionPropuesta> candidatas,
            OfertaLaboral oferta,
            PerfilProfesional perfil
    ) {
        if (oferta == null || perfil == null) {
            throw new IllegalArgumentException("La oferta y el perfil son obligatorios para aceptar una propuesta");
        }
        if (candidatas == null || candidatas.isEmpty()) {
            return List.of();
        }
        String corpus = corpus(oferta, perfil);
        List<ObservacionPropuesta> aceptadas = new ArrayList<>();
        for (ObservacionPropuesta candidata : candidatas) {
            if (candidata != null && cabe(candidata, corpus)) {
                aceptadas.add(candidata);
            }
        }
        return List.copyOf(aceptadas);
    }

    private static boolean cabe(ObservacionPropuesta candidata, String corpus) {
        String texto = candidata.texto();
        String textoMin = texto.toLowerCase(Locale.ROOT);
        if (!corpus.contains(candidata.fragmento().toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (INSTRUCCIONES.stream().anyMatch(textoMin::contains)) {
            return false;
        }
        if (ESTADOS.stream().anyMatch(patron -> patron.matcher(texto).find())) {
            return false;
        }
        Matcher numeros = NUMERO.matcher(texto);
        while (numeros.find()) {
            if (!corpus.contains(numeros.group())) {
                return false;
            }
        }
        for (String modalidad : MODALIDADES) {
            if (textoMin.contains(modalidad) && !corpus.contains(modalidad)) {
                return false;
            }
        }
        Matcher tecnicos = TOKEN_TECNICO.matcher(texto);
        while (tecnicos.find()) {
            if (!corpus.contains(tecnicos.group().toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }

    private static String corpus(OfertaLaboral oferta, PerfilProfesional perfil) {
        StringBuilder corpus = new StringBuilder();
        agregar(corpus, oferta.titulo());
        agregar(corpus, oferta.empresa());
        agregar(corpus, oferta.descripcion());
        agregar(corpus, oferta.ubicacion());
        if (oferta.modalidad() != null) {
            agregarModalidad(corpus, oferta.modalidad());
        }
        agregarSalario(corpus, oferta.salario());
        agregarTecnologias(corpus, oferta.tecnologias());
        agregar(corpus, perfil.nombre());
        agregar(corpus, perfil.tituloPrincipal());
        agregarTecnologias(corpus, perfil.tecnologias());
        if (perfil.modalidadesAceptadas() != null) {
            perfil.modalidadesAceptadas().forEach(modalidad -> agregarModalidad(corpus, modalidad));
        }
        agregar(corpus, perfil.ubicacion());
        agregarSalario(corpus, perfil.expectativaSalarial());
        if (perfil.anosExperiencia() != null) {
            agregar(corpus, perfil.anosExperiencia().toString());
        }
        return corpus.toString().toLowerCase(Locale.ROOT);
    }

    private static void agregarModalidad(StringBuilder corpus, ModalidadTrabajo modalidad) {
        agregar(corpus, modalidad.name());
        switch (modalidad) {
            case REMOTE -> {
                agregar(corpus, "remoto");
                agregar(corpus, "remote");
            }
            case HYBRID -> {
                agregar(corpus, "hibrido");
                agregar(corpus, "híbrido");
                agregar(corpus, "hybrid");
            }
            case ONSITE -> {
                agregar(corpus, "presencial");
                agregar(corpus, "onsite");
            }
        }
    }

    private static void agregarSalario(StringBuilder corpus, Salario salario) {
        if (salario == null) {
            return;
        }
        agregarImporte(corpus, salario.minimo());
        agregarImporte(corpus, salario.maximo());
        agregar(corpus, salario.moneda());
        if (salario.periodo() != null) {
            agregar(corpus, salario.periodo().name());
        }
    }

    private static void agregarImporte(StringBuilder corpus, java.math.BigDecimal importe) {
        if (importe == null) {
            return;
        }
        agregar(corpus, importe.stripTrailingZeros().toPlainString());
    }

    private static void agregarTecnologias(StringBuilder corpus, Set<String> tecnologias) {
        if (tecnologias == null) {
            return;
        }
        tecnologias.forEach(tecnologia -> agregar(corpus, tecnologia));
    }

    private static void agregar(StringBuilder corpus, String texto) {
        if (texto == null || texto.isBlank()) {
            return;
        }
        corpus.append(' ').append(texto.strip());
    }
}
