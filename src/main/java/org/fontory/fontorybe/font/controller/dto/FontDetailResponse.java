package org.fontory.fontorybe.font.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.domain.Font;

@Getter
@Builder
public class FontDetailResponse {
    private Long id;
    private String name;
    private String example;
    private Long downloadCount;
    private Long bookmarkCount;
    private Long memberId;

    public static FontDetailResponse from(Font font) {
        return FontDetailResponse.builder()
                .id(font.getId())
                .name(font.getName())
                .example(font.getExample())
                .downloadCount(font.getDownloadCount())
                .bookmarkCount(font.getBookmarkCount())
                .memberId(font.getMemberId())
                .build();
    }
}
