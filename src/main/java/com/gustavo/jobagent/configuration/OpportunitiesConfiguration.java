package com.gustavo.jobagent.configuration;

import com.gustavo.jobagent.opportunities.adapter.out.FuenteOfertasSimuladaAdapter;
import com.gustavo.jobagent.opportunities.adapter.out.lenguaje.PropuestaTextoAdapter;
import com.gustavo.jobagent.opportunities.adapter.out.lenguaje.PropuestaVaciaAdapter;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.InMemoryEvaluacionOfertaRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.JpaDictamenAccesoRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.JpaEvaluacionOfertaRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.JpaFuenteConfiguradaRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.JpaOfertaLaboralRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.JpaRegistroBusquedaRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.DictamenAccesoSpringRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.EvaluacionOfertaEvidenciaSpringRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.EvaluacionPerfilEvidenciaSpringRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.EvaluacionSpringRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.FuenteConfiguradaSpringRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.OfertaSpringRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.ProcedenciaSpringRepository;
import com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa.RegistroBusquedaSpringRepository;
import com.gustavo.jobagent.opportunities.application.port.in.BuscarOfertasUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.ConsultarDictamenAcceso;
import com.gustavo.jobagent.opportunities.application.port.in.ConsultarEvaluacionUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.ConsultarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.ConsultarRegistroBusquedaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.EjecutarAgenteUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.EvaluarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.in.RegistrarOfertaUseCase;
import com.gustavo.jobagent.opportunities.application.port.out.BuscarOfertasPort;
import com.gustavo.jobagent.opportunities.application.port.out.DictamenAccesoRepository;
import com.gustavo.jobagent.opportunities.application.port.out.EvaluacionOfertaRepository;
import com.gustavo.jobagent.opportunities.application.port.out.FuenteConfiguradaRepository;
import com.gustavo.jobagent.opportunities.application.port.out.OfertaLaboralRepository;
import com.gustavo.jobagent.opportunities.application.port.out.PropuestaLenguajePort;
import com.gustavo.jobagent.opportunities.application.port.out.RegistroBusquedaRepository;
import com.gustavo.jobagent.opportunities.application.service.BuscarOfertasService;
import com.gustavo.jobagent.opportunities.application.service.ConsultarDictamenAccesoService;
import com.gustavo.jobagent.opportunities.application.service.ConsultarEvaluacionService;
import com.gustavo.jobagent.opportunities.application.service.ConsultarOfertaService;
import com.gustavo.jobagent.opportunities.application.service.ConsultarRegistroBusquedaService;
import com.gustavo.jobagent.opportunities.application.service.EjecutarAgenteService;
import com.gustavo.jobagent.opportunities.application.service.EvaluarOfertaService;
import com.gustavo.jobagent.opportunities.application.service.RegistrarOfertaService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.util.List;

/**
 * Configuración de Spring para el módulo opportunities.
 * Conecta los adaptadores de salida con los casos de uso sin acoplamiento a frameworks en el núcleo.
 */
@Configuration
public class OpportunitiesConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public FuenteConfiguradaRepository fuenteConfiguradaRepository(FuenteConfiguradaSpringRepository fuentes) {
        return new JpaFuenteConfiguradaRepository(fuentes);
    }

    @Bean
    @ConditionalOnMissingBean(BuscarOfertasPort.class)
    public BuscarOfertasPort fuenteOfertasSimuladaAdapter(ObjectProvider<FuenteConfiguradaRepository> fuentes) {
        boolean habilitada = true;
        FuenteConfiguradaRepository repositorio = fuentes.getIfAvailable();
        if (repositorio != null) {
            habilitada = repositorio.buscarPorId(FuenteOfertasSimuladaAdapter.FUENTE_ID)
                    .map(fuente -> fuente.habilitada() && fuente.estado().name().equals("DISPONIBLE"))
                    .orElse(false);
        }
        return new FuenteOfertasSimuladaAdapter(habilitada);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public RegistroBusquedaRepository registroBusquedaRepository(RegistroBusquedaSpringRepository registros) {
        return new JpaRegistroBusquedaRepository(registros);
    }

    @Bean
    public BuscarOfertasUseCase buscarOfertasUseCase(
            List<BuscarOfertasPort> fuentes,
            ObjectProvider<RegistroBusquedaRepository> registros
    ) {
        return new BuscarOfertasService(fuentes, registros.getIfAvailable(RegistroBusquedaRepository::nulo));
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public EvaluacionOfertaRepository jpaEvaluacionOfertaRepository(
            EvaluacionSpringRepository evaluaciones,
            EvaluacionOfertaEvidenciaSpringRepository ofertas,
            EvaluacionPerfilEvidenciaSpringRepository perfiles,
            PlatformTransactionManager transactionManager
    ) {
        return new JpaEvaluacionOfertaRepository(evaluaciones, ofertas, perfiles, transactionManager);
    }

    @Bean
    @ConditionalOnMissingBean(EvaluacionOfertaRepository.class)
    public EvaluacionOfertaRepository evaluacionOfertaRepository() {
        return new InMemoryEvaluacionOfertaRepository();
    }

    @Bean
    @ConditionalOnProperty(prefix = "jobagent.lenguaje", name = "adaptador", havingValue = "texto")
    public PropuestaLenguajePort propuestaTextoAdapter(@Value("${jobagent.lenguaje.texto:}") String texto) {
        return new PropuestaTextoAdapter(texto);
    }

    @Bean
    @ConditionalOnMissingBean(PropuestaLenguajePort.class)
    public PropuestaLenguajePort propuestaVaciaAdapter() {
        return new PropuestaVaciaAdapter();
    }

    @Bean
    public EvaluarOfertaUseCase evaluarOfertaUseCase(
            EvaluacionOfertaRepository evaluacionOfertaRepository,
            PropuestaLenguajePort propuestas
    ) {
        return new EvaluarOfertaService(evaluacionOfertaRepository, propuestas, Duration.ofSeconds(2));
    }

    @Bean
    public ConsultarEvaluacionUseCase consultarEvaluacionUseCase(EvaluacionOfertaRepository evaluacionOfertaRepository) {
        return new ConsultarEvaluacionService(evaluacionOfertaRepository);
    }

    @Bean
    public EjecutarAgenteUseCase ejecutarAgenteUseCase(
            BuscarOfertasUseCase buscarOfertas,
            ConsultarOfertaUseCase consultarOferta,
            EvaluarOfertaUseCase evaluarOferta
    ) {
        return new EjecutarAgenteService(buscarOfertas, consultarOferta, evaluarOferta);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public OfertaLaboralRepository ofertaLaboralRepository(
            OfertaSpringRepository ofertas,
            ProcedenciaSpringRepository procedencias,
            PlatformTransactionManager transactionManager
    ) {
        return new JpaOfertaLaboralRepository(ofertas, procedencias, transactionManager);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public RegistrarOfertaUseCase registrarOfertaUseCase(OfertaLaboralRepository ofertas) {
        return new RegistrarOfertaService(ofertas);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public ConsultarOfertaUseCase consultarOfertaUseCase(OfertaLaboralRepository ofertas) {
        return new ConsultarOfertaService(ofertas);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public ConsultarRegistroBusquedaUseCase consultarRegistroBusquedaUseCase(RegistroBusquedaRepository registros) {
        return new ConsultarRegistroBusquedaService(registros);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public DictamenAccesoRepository dictamenAccesoRepository(DictamenAccesoSpringRepository dictamenes) {
        return new JpaDictamenAccesoRepository(dictamenes);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url")
    public ConsultarDictamenAcceso consultarDictamenAcceso(DictamenAccesoRepository dictamenes) {
        return new ConsultarDictamenAccesoService(dictamenes);
    }
}
