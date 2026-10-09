package com.gustavo.jobagent.opportunities.domain;

import java.net.URI;
import java.util.Locale;

/**
 * Identidad de una oferta registrada: fuente más dirección pública normalizada.
 */
public record IdentidadOferta(String fuenteId, String urlNormalizada) {

    public IdentidadOferta {
        if (fuenteId == null || fuenteId.isBlank()) {
            throw new IllegalArgumentException("El fuenteId es obligatorio y no puede estar vacío");
        }
        if (urlNormalizada == null || urlNormalizada.isBlank()) {
            throw new IllegalArgumentException("La dirección pública normalizada es obligatoria");
        }
        fuenteId = fuenteId.strip();
        urlNormalizada = urlNormalizada.strip();
    }

    public static IdentidadOferta de(String fuenteId, String urlOriginal) {
        if (urlOriginal == null || urlOriginal.isBlank()) {
            throw new IllegalArgumentException("La dirección pública es obligatoria para registrar la oferta");
        }
        URI uri;
        try {
            uri = new URI(urlOriginal.strip());
        } catch (Exception e) {
            throw new IllegalArgumentException("La dirección pública no se puede normalizar", e);
        }
        String esquema = uri.getScheme();
        if (esquema == null || !(esquema.equalsIgnoreCase("http") || esquema.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("La dirección pública debe usar http o https");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("La dirección pública no tiene host");
        }
        String esquemaNormalizado = esquema.toLowerCase(Locale.ROOT);
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        int puerto = uri.getPort();
        if (("http".equals(esquemaNormalizado) && puerto == 80) || ("https".equals(esquemaNormalizado) && puerto == 443)) {
            puerto = -1;
        }
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        StringBuilder normalizada = new StringBuilder();
        normalizada.append(esquemaNormalizado).append("://").append(host);
        if (puerto != -1) {
            normalizada.append(':').append(puerto);
        }
        normalizada.append(path);
        if (uri.getRawQuery() != null) {
            normalizada.append('?').append(uri.getRawQuery());
        }
        return new IdentidadOferta(fuenteId, normalizada.toString());
    }
}
