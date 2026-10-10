package com.gustavo.jobagent.opportunities.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root inmutable que representa el resultado determinista y explicable
 * de evaluar una oferta laboral frente al perfil profesional de un candidato.
 */
public record EvaluacionOferta(
        String id,
        String idOferta,
        String idPerfil,
        EstadoEvaluacion estado,
        List<Coincidencia> coincidencias,
        List<Brecha> brechas,
        List<DatoPendiente> datosPendientes,
        Instant fechaEvaluacion,
        List<ObservacionPropuesta> observaciones
) {
    public EvaluacionOferta {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El identificador de la evaluación no puede estar vacío");
        }
        if (idOferta == null || idOferta.isBlank()) {
            throw new IllegalArgumentException("El identificador de la oferta no puede estar vacío");
        }
        if (idPerfil == null || idPerfil.isBlank()) {
            throw new IllegalArgumentException("El identificador del perfil no puede estar vacío");
        }
        Objects.requireNonNull(estado, "El estado de la evaluación es obligatorio");
        Objects.requireNonNull(fechaEvaluacion, "La fecha de evaluación es obligatoria");

        id = id.strip();
        idOferta = idOferta.strip();
        idPerfil = idPerfil.strip();
        coincidencias = (coincidencias != null) ? List.copyOf(coincidencias) : List.of();
        brechas = (brechas != null) ? List.copyOf(brechas) : List.of();
        datosPendientes = (datosPendientes != null) ? List.copyOf(datosPendientes) : List.of();
        observaciones = (observaciones != null) ? List.copyOf(observaciones) : List.of();
    }

    /**
     * Copia la evaluación calculada y sustituye solo la lista de observaciones.
     */
    public EvaluacionOferta conObservaciones(List<ObservacionPropuesta> nuevas) {
        return new EvaluacionOferta(
                id,
                idOferta,
                idPerfil,
                estado,
                coincidencias,
                brechas,
                datosPendientes,
                fechaEvaluacion,
                nuevas
        );
    }

    /**
     * Evalúa de forma pura y determinista la compatibilidad entre una oferta laboral y un perfil profesional.
     *
     * @param oferta Oferta laboral analizada.
     * @param perfil Perfil profesional del candidato.
     * @return Aggregate Root {@link EvaluacionOferta} con las coincidencias, brechas, datos pendientes y estado final.
     */
    public static EvaluacionOferta evaluar(OfertaLaboral oferta, PerfilProfesional perfil) {
        return evaluar(UUID.randomUUID().toString(), oferta, perfil, Instant.now());
    }

    /**
     * Evalúa deterministamente permitiendo fijar identificador y fecha (útil para reproducibilidad y pruebas).
     */
    public static EvaluacionOferta evaluar(String id, OfertaLaboral oferta, PerfilProfesional perfil, Instant fechaEvaluacion) {
        Objects.requireNonNull(oferta, "La oferta laboral no puede ser nula");
        Objects.requireNonNull(perfil, "El perfil profesional no puede ser nulo");
        Objects.requireNonNull(fechaEvaluacion, "La fecha de evaluación no puede ser nula");

        List<Coincidencia> coincidencias = new ArrayList<>();
        List<Brecha> brechas = new ArrayList<>();
        List<DatoPendiente> datosPendientes = new ArrayList<>();

        // 1. Evaluación de tecnologías
        if (oferta.tecnologias().isEmpty()) {
            datosPendientes.add(new DatoPendiente(
                    TipoAspecto.TECNOLOGIA,
                    "La oferta no especifica tecnologías requeridas"
            ));
        } else {
            for (String techRequerida : oferta.tecnologias()) {
                if (perfil.dominaTecnologia(techRequerida)) {
                    coincidencias.add(new Coincidencia(
                            TipoAspecto.TECNOLOGIA,
                            "Tecnología coincidente: " + techRequerida,
                            "Requerida por la oferta y dominada por el candidato"
                    ));
                } else {
                    brechas.add(new Brecha(
                            TipoAspecto.TECNOLOGIA,
                            "Tecnología faltante: " + techRequerida,
                            false
                    ));
                }
            }
        }

        // 2. Evaluación de modalidad
        if (oferta.modalidad() != null) {
            if (perfil.modalidadesAceptadas().isEmpty() || perfil.modalidadesAceptadas().contains(oferta.modalidad())) {
                coincidencias.add(new Coincidencia(
                        TipoAspecto.MODALIDAD,
                        "Modalidad compatible: " + oferta.modalidad(),
                        "La oferta ofrece modalidad " + oferta.modalidad() + ", aceptada en el perfil del candidato"
                ));
            } else {
                brechas.add(new Brecha(
                        TipoAspecto.MODALIDAD,
                        "Incompatibilidad de modalidad: la oferta exige " + oferta.modalidad() + " pero el perfil solo acepta " + perfil.modalidadesAceptadas(),
                        true
                ));
            }
        } else {
            datosPendientes.add(new DatoPendiente(
                    TipoAspecto.MODALIDAD,
                    "La oferta no especifica modalidad de trabajo"
            ));
        }

        // 3. Evaluación de ubicación
        if (oferta.modalidad() == ModalidadTrabajo.REMOTE) {
            coincidencias.add(new Coincidencia(
                    TipoAspecto.UBICACION,
                    "Modalidad remota",
                    "La posición es 100% remota y no exige presencialidad"
            ));
        } else if (oferta.ubicacion() != null && perfil.ubicacion() != null) {
            String ofUbic = oferta.ubicacion().toLowerCase(Locale.ROOT);
            String perfUbic = perfil.ubicacion().toLowerCase(Locale.ROOT);
            if (ofUbic.contains(perfUbic) || perfUbic.contains(ofUbic)) {
                coincidencias.add(new Coincidencia(
                        TipoAspecto.UBICACION,
                        "Ubicación compatible: " + oferta.ubicacion(),
                        "La ubicación de la oferta coincide con la ubicación del candidato"
                ));
            } else {
                brechas.add(new Brecha(
                        TipoAspecto.UBICACION,
                        "Incompatibilidad de ubicación: la oferta requiere presencia en " + oferta.ubicacion() + " y el candidato reside en " + perfil.ubicacion(),
                        true
                ));
            }
        } else if (oferta.ubicacion() == null) {
            datosPendientes.add(new DatoPendiente(
                    TipoAspecto.UBICACION,
                    "La oferta no especifica la ubicación presencial o híbrida requerida"
            ));
        } else {
            datosPendientes.add(new DatoPendiente(
                    TipoAspecto.UBICACION,
                    "El candidato no especificó su ubicación de residencia"
            ));
        }

        // 4. Evaluación de salario
        if (oferta.salario() != null && perfil.expectativaSalarial() != null) {
            Salario ofSal = oferta.salario();
            Salario perfSal = perfil.expectativaSalarial();

            if (ofSal.moneda().equalsIgnoreCase(perfSal.moneda()) && ofSal.periodo() == perfSal.periodo()) {
                BigDecimal maxOf = ofSal.maximo() != null ? ofSal.maximo() : ofSal.minimo();
                BigDecimal minPerf = perfSal.minimo();

                if (maxOf != null && minPerf != null) {
                    if (maxOf.compareTo(minPerf) >= 0) {
                        coincidencias.add(new Coincidencia(
                                TipoAspecto.SALARIO,
                                "Rango salarial compatible",
                                "El salario de la oferta (" + ofSal + ") cubre la expectativa mínima (" + perfSal + ")"
                        ));
                    } else {
                        brechas.add(new Brecha(
                                TipoAspecto.SALARIO,
                                "Salario por debajo de la expectativa mínima: oferta máx " + maxOf + " vs expectativa mín " + minPerf,
                                false
                        ));
                    }
                } else {
                    coincidencias.add(new Coincidencia(
                            TipoAspecto.SALARIO,
                            "Salario compatible",
                            "El rango salarial informado coincide con las condiciones"
                    ));
                }
            } else {
                datosPendientes.add(new DatoPendiente(
                        TipoAspecto.SALARIO,
                        "Diferencia de moneda o período entre oferta (" + ofSal.moneda() + "/" + ofSal.periodo() + ") y perfil (" + perfSal.moneda() + "/" + perfSal.periodo() + ")"
                ));
            }
        } else if (oferta.salario() == null) {
            datosPendientes.add(new DatoPendiente(
                    TipoAspecto.SALARIO,
                    "La oferta no publica rango salarial; se sugiere consultar en la entrevista"
            ));
        } else {
            datosPendientes.add(new DatoPendiente(
                    TipoAspecto.SALARIO,
                    "El perfil no define expectativa salarial explícita"
            ));
        }

        // 5. Cálculo del estado global
        boolean hayBloqueante = brechas.stream().anyMatch(Brecha::esBloqueante);
        long techMatches = coincidencias.stream().filter(c -> c.aspecto() == TipoAspecto.TECNOLOGIA).count();
        boolean sinCoincidenciasTech = !oferta.tecnologias().isEmpty() && techMatches == 0;

        EstadoEvaluacion estado;
        if (hayBloqueante || sinCoincidenciasTech) {
            estado = EstadoEvaluacion.NO_COMPATIBLE;
        } else if (!brechas.isEmpty()) {
            estado = EstadoEvaluacion.COMPATIBLE_CON_BRECHAS;
        } else {
            estado = EstadoEvaluacion.COMPATIBLE;
        }

        return new EvaluacionOferta(
                id,
                oferta.idFuente(),
                perfil.id(),
                estado,
                coincidencias,
                brechas,
                datosPendientes,
                fechaEvaluacion,
                List.of()
        );
    }
}
