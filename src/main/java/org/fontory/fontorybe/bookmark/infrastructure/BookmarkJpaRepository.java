package org.fontory.fontorybe.bookmark.infrastructure;

import org.fontory.fontorybe.bookmark.infrastructure.entity.BookmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkJpaRepository extends JpaRepository<BookmarkEntity, Long> {
    boolean existsByMemberIdAndFontId(Long memberId, Long fontId);

}
