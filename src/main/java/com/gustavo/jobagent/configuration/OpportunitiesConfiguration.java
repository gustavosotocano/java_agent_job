package com.gustavo.jobagent.configuration;

import com.gustavo.jobagent.opportunities.adapter.out.FuenteOfertasSimuladaAdapter;
import com.gustavo.jobagent.opportunities.application.port.in.BuscarOfertasUseCase;
import com.gustavo.jobagent.opportunities.application.port.out.BuscarOfertasPort;
import com.gustavo.jobagent.opportunities.application.service.BuscarOfertasService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Spring para el módulo opportunities.
 * Conecta los adaptadores de salida con los casos de uso sin acoplamiento a frameworks en el núcleo.
 */
@Configuration
public class OpportunitiesConfiguration {

    @Bean
    @ConditionalOnMissingBean(BuscarOfertasPort.class)
    public BuscarOfertasPort fuenteOfertasSimuladaAdapter() {
        return new FuenteOfertasSimuladaAdapter(true);
    }

    @Bean
    public BuscarOfertasUseCase buscarOfertasUseCase(List<BuscarOfertasPort> fuentes) {
        return new BuscarOfertasService(fuentes);
    }
}
