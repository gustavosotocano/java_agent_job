package com.gustavo.jobagent.opportunities.adapter.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfertaSpringRepository extends JpaRepository<OfertaJpaEntity, String> {
    Optional<OfertaJpaEntity> findByFuenteIdAndUrlNormalizada(String fuenteId, String urlNormalizada);
}
