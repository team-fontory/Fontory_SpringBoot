package org.fontory.fontorybe.member.infrastructure;

import org.fontory.fontorybe.member.infrastructure.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {
}
