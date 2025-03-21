package org.fontory.fontorybe.bookmark.infrastructure;

import java.util.Optional;
import org.fontory.fontorybe.bookmark.domain.Bookmark;
import org.fontory.fontorybe.bookmark.infrastructure.entity.BookmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkJpaRepository extends JpaRepository<BookmarkEntity, Long> {
    boolean existsByMemberIdAndFontId(Long memberId, Long fontId);
    Optional<BookmarkEntity> findByMemberIdAndFontId(Long memberId, Long fontId);
}
