package org.fontory.fontorybe.bookmark.controller.port;

import org.fontory.fontorybe.bookmark.controller.dto.BookmarkDeleteResponse;
import org.fontory.fontorybe.bookmark.domain.Bookmark;

public interface BookmarkService {
    Bookmark create(Long memberId, Long fontId);
    BookmarkDeleteResponse delete(Long memberId, Long fontId);
}
