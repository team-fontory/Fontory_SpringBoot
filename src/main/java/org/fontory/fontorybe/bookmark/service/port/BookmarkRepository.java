package org.fontory.fontorybe.bookmark.service.port;

import java.util.Optional;
import org.fontory.fontorybe.bookmark.domain.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface BookmarkRepository {
    Bookmark save(Bookmark bookmark);
    boolean existsByMemberIdAndFontId(Long memberId, Long fontId);
    Optional<Bookmark> findByMemberIdAndFontId(Long memberId, Long fontId);
    void deleteById(Long id);
    Page<Bookmark> findAllByMemberId(Long memberId, PageRequest pageRequest);
}
