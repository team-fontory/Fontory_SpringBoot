package org.fontory.fontorybe.font.controller.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private LocalDateTime createdAt;

    public static FontProgressResponse from(Font font) {
        return FontProgressResponse.builder()
                .id(font.getId())
                .name(font.getName())
                .status(font.getStatus())
                .createdAt(
                        font.getCreatedAt()
                                .atZone(ZoneId.of("UTC"))
                                .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                                .toLocalDateTime())
                .build();
    }
}
