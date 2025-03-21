package org.fontory.fontorybe.bookmark.service.port;

import org.fontory.fontorybe.bookmark.domain.Bookmark;

public interface BookmarkRepository {
    Bookmark save(Bookmark bookmark);
    boolean existsByMemberIdAndFontId(Long memberId, Long fontId);
}
