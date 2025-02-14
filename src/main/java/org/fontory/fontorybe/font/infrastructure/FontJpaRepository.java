package org.fontory.fontorybe.font.infrastructure;

import java.util.List;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.infrastructure.entity.FontEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FontJpaRepository extends JpaRepository<FontEntity, Long> {
    List<FontEntity> findTop5ByMemberIdOrderByCreatedAtDesc(Long memberId);
}
