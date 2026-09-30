package com.gustavo.jobagent.opportunities.adapter.in.web;

import com.gustavo.jobagent.opportunities.application.port.in.BuscarOfertasUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.ResultadoBusquedaOfertas;
import com.gustavo.jobagent.opportunities.domain.CriteriosBusqueda;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Adaptador de entrada HTTP para la búsqueda de ofertas laborales.
 */
@RestController
@RequestMapping("/api/ofertas")
public class BuscarOfertasController {

    private final BuscarOfertasUseCase buscarOfertasUseCase;

    public BuscarOfertasController(BuscarOfertasUseCase buscarOfertasUseCase) {
        this.buscarOfertasUseCase = buscarOfertasUseCase;
    }

    @PostMapping("/busquedas")
    public ResponseEntity<BuscarOfertasResponse> buscarOfertas(@Valid @RequestBody BuscarOfertasRequest request) {
        CriteriosBusqueda criterios = new CriteriosBusqueda(
                request.cargo(),
                request.tecnologias(),
                request.ubicacion(),
                request.modalidad(),
                request.limite()
        );

        ResultadoBusquedaOfertas resultado = buscarOfertasUseCase.buscar(criterios);

        List<BuscarOfertasResponse.OfertaLaboralResponse> ofertasDto = resultado.ofertas().stream()
                .map(BuscarOfertasResponse.OfertaLaboralResponse::fromDomain)
                .toList();

        List<BuscarOfertasResponse.AdvertenciaFuenteResponse> advertenciasDto = resultado.advertencias().stream()
                .map(adv -> new BuscarOfertasResponse.AdvertenciaFuenteResponse(adv.fuenteId(), adv.mensaje()))
                .toList();

        BuscarOfertasResponse response = new BuscarOfertasResponse(
                ofertasDto,
                resultado.fuentesConsultadas(),
                advertenciasDto
        );

        return ResponseEntity.ok(response);
    }
}
