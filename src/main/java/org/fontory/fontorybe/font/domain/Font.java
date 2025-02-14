package org.fontory.fontorybe.font.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Font {

    private Long id;

    private String name;

    private FontStatus status;

    private Long downloadCount;

    private Long bookmarkCount;

    private String ttf;

    private String woff;

    private Long memberId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Font from(FontCreateDTO fontCreateDTO, Long memberId) {
        return Font.builder()
                .name(fontCreateDTO.getName())
                .status(FontStatus.PROGRESS)
                .downloadCount(0L)
                .bookmarkCount(0L)
                .memberId(memberId)
                .build();
    }
}
