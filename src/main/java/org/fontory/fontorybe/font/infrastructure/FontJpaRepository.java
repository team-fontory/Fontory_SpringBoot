package org.fontory.fontorybe.font.infrastructure;

import java.util.List;
import org.fontory.fontorybe.font.infrastructure.entity.FontEntity;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FontJpaRepository extends JpaRepository<FontEntity, Long> {
    
    // 회원별 최근 폰트 조회
    @Query("SELECT f FROM FontEntity f WHERE f.memberId = :memberId ORDER BY f.createdAt DESC")
    List<FontEntity> findTop5ByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId, Pageable pageable);
    
    List<FontEntity> findTop10ByMemberIdOrderByCreatedAtDesc(Long memberId);
    Page<FontEntity> findAllByMemberIdAndStatus(Long memberId, PageRequest pageRequest, FontStatus status);
    Page<FontEntity> findByNameContainingAndStatus(String name, PageRequest pageRequest, FontStatus status);
    List<FontEntity> findTop3ByMemberIdAndIdNotAndStatusOrderByCreatedAtDesc(Long memberId, Long fontId, FontStatus status);
    List<FontEntity> findAllByIdIn(List<Long> ids);
    @Query("SELECT f FROM FontEntity f WHERE f.memberId = :memberId AND f.status = :status ORDER BY (f.downloadCount + f.bookmarkCount) DESC")
    List<FontEntity> findTopByMemberIdAndStatusOrderByPopularityDesc(
            @Param("memberId") Long memberId,
            @Param("status") FontStatus status,
            Pageable pageable
    );
    @Query("SELECT f FROM FontEntity f WHERE f.status = :status ORDER BY (f.downloadCount + f.bookmarkCount) DESC ")
    List<FontEntity> findTop3ByStatusOrderByDownloadAndBookmarkCountDesc(
            @Param("status") FontStatus status,
            Pageable pageable
    );
    boolean existsByName(String fontName);
    
    // 회원별 폰트 이름 중복 검사
    @Query("SELECT EXISTS(SELECT 1 FROM FontEntity f WHERE f.memberId = :memberId AND f.name = :name)")
    boolean existsByMemberIdAndName(@Param("memberId") Long memberId, @Param("name") String name);
    
    Page<FontEntity> findAllByStatus(PageRequest pageRequest, FontStatus status);
    
    // 성능 최적화 네이티브 쿼리
    @Query(value = "SELECT COUNT(*) FROM font WHERE status = :status", nativeQuery = true)
    long countByStatus(@Param("status") String status);
    
    @Query(value = "SELECT * FROM font WHERE member_id = :memberId AND status = 'DONE' ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<FontEntity> findRecentFontsByMemberId(@Param("memberId") Long memberId, @Param("limit") int limit);
}
