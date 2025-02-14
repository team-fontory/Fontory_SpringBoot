package org.fontory.fontorybe.font.infrastructure;

import org.fontory.fontorybe.font.infrastructure.entity.FontEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FontJpaRepository extends JpaRepository<FontEntity, Long> {
}
