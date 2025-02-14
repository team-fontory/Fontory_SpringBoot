package org.fontory.fontorybe.font.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;

@Getter
@Builder
public class FontProgressResponse {
    private Long id;
    private String name;
    private FontStatus status;

    public static FontProgressResponse from(Font font) {
        return FontProgressResponse.builder()
                .id(font.getId())
                .name(font.getName())
                .status(font.getStatus())
                .build();
    }
}
