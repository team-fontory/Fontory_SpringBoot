package org.fontory.fontorybe.font.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.domain.Font;

@Getter
@Builder
public class FontPageResponse {
    private Long id;
    private String name;
    private String example;
    private String writerName;
    private Long downloadCount;
    private Long bookmarkCount;
    private boolean isBookmarked;

    public static FontPageResponse from(Font font, String writerName, boolean isBookmarked) {
        return FontPageResponse.builder()
                .id(font.getId())
                .name(font.getName())
                .example(font.getExample())
                .writerName(writerName)
                .downloadCount(font.getDownloadCount())
                .bookmarkCount(font.getBookmarkCount())
                .isBookmarked(isBookmarked)
                .build();
    }
}
