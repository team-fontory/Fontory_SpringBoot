package org.fontory.fontorybe.bookmark.controller.port;

import org.fontory.fontorybe.bookmark.controller.dto.BookmarkDeleteResponse;
import org.fontory.fontorybe.bookmark.domain.Bookmark;
import org.fontory.fontorybe.font.controller.dto.FontResponse;
import org.springframework.data.domain.Page;

public interface BookmarkService {
    Bookmark create(Long memberId, Long fontId);
    BookmarkDeleteResponse delete(Long memberId, Long fontId);
    Page<FontResponse> getBookmarkedFonts(Long memberId, int page, int size, String keyword);
}
