package org.fontory.fontorybe.member.infrastructure;

import org.fontory.fontorybe.member.infrastructure.entity.MemberEntity;
import org.fontory.fontorybe.member.infrastructure.entity.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberJpaRepository extends JpaRepository<MemberEntity, Long> {
    boolean existsByNickname(String nickname);
    
    // 성능 최적화를 위한 추가 쿼리 메서드
    @Query("SELECT m FROM MemberEntity m WHERE m.id = :id AND m.status != :status")
    Optional<MemberEntity> findByIdAndStatusNot(@Param("id") Long id, @Param("status") MemberStatus status);
    
    @Query("SELECT m FROM MemberEntity m WHERE m.status = :status ORDER BY m.createdAt DESC")
    List<MemberEntity> findAllByStatusOrderByCreatedAtDesc(@Param("status") MemberStatus status);
    
    @Query(value = "SELECT COUNT(*) FROM member WHERE status = :status", nativeQuery = true)
    long countByStatus(@Param("status") String status);
}
