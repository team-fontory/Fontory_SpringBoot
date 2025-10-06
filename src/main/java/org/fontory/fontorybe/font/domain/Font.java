package org.fontory.fontorybe.font.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontProgressUpdateDTO;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Font {

    private Long id;

    private String name;

    private String engName;

    private FontStatus status;

    private String example;

    private Long downloadCount;

    private Long bookmarkCount;

    private String key;

    private Long memberId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void increaseBookmarkCount() {
        this.bookmarkCount++;
    }

    public void decreaseBookmarkCount() {
        this.bookmarkCount--;
    }

    public void increaseDownloadCount() {
        this.downloadCount++;
    }

    public static Font from(FontCreateDTO fontCreateDTO, Long memberId, String key) {
        return Font.builder()
                .name(fontCreateDTO.getName())
                .engName(fontCreateDTO.getEngName())
                .status(FontStatus.PROGRESS)
                .example(fontCreateDTO.getExample())
                .key(key)
                .downloadCount(0L)
                .bookmarkCount(0L)
                .memberId(memberId)
                .build();
    }

    public Font updateProgress(FontProgressUpdateDTO fontProgressUpdateDTO) {
        return Font.builder()
                .name(this.name)
                .engName(this.engName)
                .example(this.example)
                .id(this.id)
                .status(fontProgressUpdateDTO.getStatus())
                .downloadCount(this.downloadCount)
                .bookmarkCount(this.bookmarkCount)
                .key(this.key)
                .memberId(this.memberId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
