package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcedenciaSpringRepository extends JpaRepository<ProcedenciaJpaEntity, String> {
    List<ProcedenciaJpaEntity> findByOfertaIdOrderByCapturadoEnAsc(String ofertaId);
}
