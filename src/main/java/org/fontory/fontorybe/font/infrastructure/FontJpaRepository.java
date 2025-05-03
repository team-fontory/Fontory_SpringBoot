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
    List<FontEntity> findTop5ByMemberIdOrderByCreatedAtDesc(Long memberId);
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
    Page<FontEntity> findAllByStatus(PageRequest pageRequest, FontStatus status);
}
