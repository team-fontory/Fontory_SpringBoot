package org.fontory.fontorybe.bookmark.controller.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.bookmark.domain.Bookmark;

@Getter
@Builder
public class BookmarkCreateResponse {
    private Long id;
    private Long memberId;
    private Long fontId;
    private LocalDateTime createdAt;

    public static BookmarkCreateResponse from(Bookmark bookmark) {
        return BookmarkCreateResponse.builder()
                .id(bookmark.getId())
                .memberId(bookmark.getMemberId())
                .fontId(bookmark.getFontId())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
