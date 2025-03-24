package org.fontory.fontorybe.font.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.domain.Font;

@Getter
@Builder
public class FontResponse {
    private Long id;
    private String name;
    private String example;
    private Long downloadCount;
    private Long bookmarkCount;
    private boolean isBookmarked;

    public static FontResponse from(Font font, boolean isBookmarked) {
        return FontResponse.builder()
                .id(font.getId())
                .name(font.getName())
                .example(font.getExample())
                .downloadCount(font.getDownloadCount())
                .bookmarkCount(font.getBookmarkCount())
                .isBookmarked(isBookmarked)
                .build();
    }
}
