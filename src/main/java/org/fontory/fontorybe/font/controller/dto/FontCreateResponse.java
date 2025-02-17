package org.fontory.fontorybe.font.controller.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;

@Getter
@Builder
public class FontCreateResponse {
    private Long id;
    private String name;
    private FontStatus status;
    private Long memberId;
    private LocalDateTime createdAt;

    public static FontCreateResponse from(Font font) {
        return FontCreateResponse.builder()
                .id(font.getId())
                .name(font.getName())
                .status(font.getStatus())
                .memberId(font.getMemberId())
                .createdAt(font.getCreatedAt())
                .build();
    }
}
