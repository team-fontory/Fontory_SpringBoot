package org.fontory.fontorybe.bookmark.infrastructure;

import java.util.List;
import java.util.Optional;
import org.fontory.fontorybe.bookmark.infrastructure.entity.BookmarkEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkJpaRepository extends JpaRepository<BookmarkEntity, Long> {
    boolean existsByMemberIdAndFontId(Long memberId, Long fontId);
    Optional<BookmarkEntity> findByMemberIdAndFontId(Long memberId, Long fontId);
    Page<BookmarkEntity> findAllByMemberId(Long memberId, Pageable pageable);
    
    // 성능 최적화를 위한 추가 쿼리
    @Query(value = "SELECT COUNT(*) FROM bookmark WHERE member_id = :memberId", nativeQuery = true)
    long countByMemberId(@Param("memberId") Long memberId);
    
    @Query(value = "SELECT font_id FROM bookmark WHERE member_id = :memberId ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<Long> findRecentBookmarkedFontIds(@Param("memberId") Long memberId, @Param("limit") int limit);
    
    @Query("SELECT b FROM BookmarkEntity b WHERE b.memberId = :memberId ORDER BY b.createdAt DESC")
    List<BookmarkEntity> findAllByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId, Pageable pageable);
}
