package org.fontory.fontorybe.provide.infrastructure;

import org.fontory.fontorybe.provide.infrastructure.entity.ProvideEntity;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProvideJpaRepository extends JpaRepository<ProvideEntity, Long> {
    Optional<ProvideEntity> findByProvidedIdAndProvider(String providedId, Provider provider);
    
    // 성능 최적화를 위한 추가 쿼리
    @Query("SELECT p FROM ProvideEntity p WHERE p.memberId = :memberId ORDER BY p.createdAt DESC")
    List<ProvideEntity> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);
    
    @Query(value = "SELECT * FROM provide WHERE member_id = :memberId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<ProvideEntity> findLatestByMemberId(@Param("memberId") Long memberId);
}
