package org.fontory.fontorybe.font.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.domain.Font;

@Getter
@Builder
public class FontDownloadResponse {
    private Long id;
    private String name;
    private String ttf;

    public static FontDownloadResponse from(Font font, String url) {
        return FontDownloadResponse.builder()
                .id(font.getId())
                .name(font.getName())
                .ttf(url)
                .build();
    }
}
