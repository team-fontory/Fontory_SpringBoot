package org.fontory.fontorybe.font.controller.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;

@Getter
@Builder
public class FontUpdateResponse {
    private Long id;
    private String name;
    private FontStatus status;
    private String example;
    private Long memberId;
    private LocalDateTime createdAt;
    private String woff;

    public static FontUpdateResponse from(Font font, String url) {
        return FontUpdateResponse.builder()
                .id(font.getId())
                .name(font.getName())
                .status(font.getStatus())
                .example(font.getExample())
                .memberId(font.getMemberId())
                .createdAt(font.getCreatedAt())
                .woff(url)
                .build();
    }
}
