package org.fontory.fontorybe.bookmark.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkDeleteResponse {
    private Long id;

    public static BookmarkDeleteResponse from(Long bookmarkId) {
        return BookmarkDeleteResponse.builder()
                .id(bookmarkId)
                .build();
    }
}
