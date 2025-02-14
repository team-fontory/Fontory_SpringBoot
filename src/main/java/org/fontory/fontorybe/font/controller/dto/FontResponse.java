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

    public static FontResponse from(Font font) {
        return FontResponse.builder()
                .id(font.getId())
                .name(font.getName())
                .example(font.getExample())
                .build();
    }
}
