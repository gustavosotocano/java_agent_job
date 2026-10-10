package com.gustavo.jobagent.opportunities.application.service;

import com.gustavo.jobagent.opportunities.application.port.in.ConsultarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.EjecucionRealizada;
import com.gustavo.jobagent.opportunities.application.port.in.EjecutarAgenteUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaCommand;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.FuentesNoDisponiblesException;
import com.gustavo.jobagent.opportunities.application.port.in.OfertaRegistrada;
import com.gustavo.jobagent.opportunities.application.port.in.PasoRealizado;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoBusquedaOfertas;
import com.gustavo.jobagent.opportunities.application.port.in.BuscarOfertasUseCase;
import com.gustavo.jobagent.opportunities.domain.EjecucionAcotada;
import com.gustavo.jobagent.opportunities.domain.EvaluacionOferta;
import com.gustavo.jobagent.opportunities.domain.PerfilProfesional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Ejecuta una lista ya acotada. No elige el siguiente paso y no acepta un estado de evaluación.
 */
public class EjecutarAgenteService implements EjecutarAgenteUseCase {

    private final BuscarOfertasUseCase buscarOfertas;
    private final ConsultarOfertaUseCase consultarOferta;
    private final EvaluarOfertaUseCase evaluarOferta;

    public EjecutarAgenteService(
            BuscarOfertasUseCase buscarOfertas,
            ConsultarOfertaUseCase consultarOferta,
            EvaluarOfertaUseCase evaluarOferta
    ) {
        this.buscarOfertas = Objects.requireNonNull(buscarOfertas, "El caso de uso de búsqueda no puede ser nulo");
        this.consultarOferta = Objects.requireNonNull(consultarOferta, "El caso de uso de consulta de oferta no puede ser nulo");
        this.evaluarOferta = Objects.requireNonNull(evaluarOferta, "El caso de uso de evaluación no puede ser nulo");
    }

    @Override
    public EjecucionRealizada ejecutar(PerfilProfesional perfil, List<EjecucionAcotada.Solicitud> pasos) {
        Objects.requireNonNull(perfil, "El perfil de la ejecución no puede ser nulo");
        EjecucionAcotada.Plan plan = EjecucionAcotada.planear(pasos);
        List<PasoRealizado> realizados = new ArrayList<>();
        for (EjecucionAcotada.Solicitud paso : plan.aEjecutar()) {
            realizados.add(ejecutarPaso(perfil, paso));
        }
        if (plan.rechazada() != null) {
            String nombre = plan.rechazada().herramienta() == null ? "" : plan.rechazada().herramienta();
            realizados.add(new PasoRealizado(nombre, false, "no autorizada"));
        }
        return new EjecucionRealizada(plan.estado(), realizados, plan.noEjecutadas());
    }

    private PasoRealizado ejecutarPaso(PerfilProfesional perfil, EjecucionAcotada.Solicitud paso) {
        return switch (paso.herramienta()) {
            case "buscarOfertas" -> buscar(paso);
            case "consultarPerfil" -> consultarPerfil(perfil, paso);
            case "consultarOferta" -> consultarOferta(paso);
            case "guardarEvaluacion" -> guardarEvaluacion(perfil, paso);
            default -> new PasoRealizado(paso.herramienta(), false, "no autorizada");
        };
    }

    private PasoRealizado buscar(EjecucionAcotada.Solicitud paso) {
        if (paso.criterios() == null) {
            return new PasoRealizado("buscarOfertas", true, "criterios ausentes");
        }
        try {
            ResultadoBusquedaOfertas resultado = buscarOfertas.buscar(paso.criterios());
            return new PasoRealizado("buscarOfertas", true, resultado.ofertas().size() + " ofertas");
        } catch (FuentesNoDisponiblesException e) {
            return new PasoRealizado("buscarOfertas", true, "búsqueda fallida");
        }
    }

    private PasoRealizado consultarPerfil(PerfilProfesional perfil, EjecucionAcotada.Solicitud paso) {
        if (paso.id() != null && paso.id().equals(perfil.id())) {
            return new PasoRealizado("consultarPerfil", true, "perfil encontrado");
        }
        return new PasoRealizado("consultarPerfil", true, "perfil desconocido");
    }

    private PasoRealizado consultarOferta(EjecucionAcotada.Solicitud paso) {
        if (paso.id() == null || paso.id().isBlank() || consultarOferta.porId(paso.id()).isEmpty()) {
            return new PasoRealizado("consultarOferta", true, "oferta desconocida");
        }
        return new PasoRealizado("consultarOferta", true, "oferta encontrada");
    }

    private PasoRealizado guardarEvaluacion(PerfilProfesional perfil, EjecucionAcotada.Solicitud paso) {
        if (paso.id() == null || paso.id().isBlank()) {
            return new PasoRealizado("guardarEvaluacion", true, "oferta no registrada");
        }
        Optional<OfertaRegistrada> oferta = consultarOferta.porId(paso.id());
        if (oferta.isEmpty()) {
            return new PasoRealizado("guardarEvaluacion", true, "oferta no registrada");
        }
        EvaluacionOferta evaluacion = evaluarOferta.evaluar(new EvaluarOfertaCommand(oferta.get().vigente(), perfil));
        return new PasoRealizado("guardarEvaluacion", true, "evaluación " + evaluacion.estado());
    }
}
