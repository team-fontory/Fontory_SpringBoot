package org.fontory.fontorybe.font.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.domain.Font;

@Getter
@Builder
public class FontDeleteResponse {
    private Long id;

    public static FontDeleteResponse from(Long fontId) {
        return FontDeleteResponse.builder()
                .id(fontId)
                .build();
    }
}
