package org.fontory.fontorybe.bookmark.service.port;

import java.util.Optional;
import org.fontory.fontorybe.bookmark.domain.Bookmark;

public interface BookmarkRepository {
    Bookmark save(Bookmark bookmark);
    boolean existsByMemberIdAndFontId(Long memberId, Long fontId);
    Optional<Bookmark> findByMemberIdAndFontId(Long memberId, Long fontId);
    void deleteById(Long id);
}
