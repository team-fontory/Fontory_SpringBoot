package org.fontory.fontorybe.provide.infrastructure;

import org.fontory.fontorybe.provide.infrastructure.entity.ProvideEntity;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProvideJpaRepository extends JpaRepository<ProvideEntity, Long> {
    Optional<ProvideEntity> findByProvidedIdAndProvider(String providedId, Provider provider);
}
