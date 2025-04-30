package org.fontory.fontorybe.font.infrastructure;

import java.util.List;
import org.fontory.fontorybe.font.infrastructure.entity.FontEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FontJpaRepository extends JpaRepository<FontEntity, Long> {
    List<FontEntity> findTop5ByMemberIdOrderByCreatedAtDesc(Long memberId);
    Page<FontEntity> findAllByMemberId(Long memberId, PageRequest pageRequest);
    Page<FontEntity> findByNameContaining(String name, PageRequest pageRequest);
    List<FontEntity> findTop3ByMemberIdAndIdNotOrderByCreatedAtDesc(Long memberId, Long fontId);
    List<FontEntity> findAllByIdIn(List<Long> ids);
    @Query("SELECT f FROM FontEntity f WHERE f.memberId = :memberId ORDER BY (f.downloadCount + f.bookmarkCount) DESC")
    List<FontEntity> findTop4ByMemberIdOrderByDownloadAndBookmarkCountDesc(@Param("memberId") Long memberId, Pageable pageable);
    @Query("SELECT f FROM FontEntity f ORDER BY (f.downloadCount + f.bookmarkCount) DESC")
    List<FontEntity> findTop3OrderByDownloadAndBookmarkCountDesc(Pageable pageable);
    boolean existsByName(String fontName);
}
