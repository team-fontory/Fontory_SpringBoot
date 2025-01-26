package org.fontory.fontorybe.provide.infrastructure;

import org.fontory.fontorybe.provide.infrastructure.entity.ProvideEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvideJpaRepository extends JpaRepository<ProvideEntity, Long> {
}
