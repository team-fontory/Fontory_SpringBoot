package org.fontory.fontorybe.font.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontProgressUpdateDTO;
import org.fontory.fontorybe.font.controller.dto.FontUpdateDTO;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Font {

    private Long id;

    private String name;

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
                .status(FontStatus.PROGRESS)
                .example(fontCreateDTO.getExample())
                .key(key)
                .downloadCount(0L)
                .bookmarkCount(0L)
                .memberId(memberId)
                .build();
    }

    public Font update(FontUpdateDTO fontUpdateDTO) {
        return Font.builder()
                .name(fontUpdateDTO.getName())
                .example(fontUpdateDTO.getExample())
                .id(this.id)
                .status(this.status)
                .downloadCount(this.downloadCount)
                .bookmarkCount(this.bookmarkCount)
                .key(this.key)
                .memberId(this.memberId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public Font updateProgress(FontProgressUpdateDTO fontProgressUpdateDTO, Long fontId) {

        if (fontProgressUpdateDTO.getStatus() == FontStatus.DONE) {
            return Font.builder()
                    .name(this.name)
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
        } else {
            return Font.builder()
                    .name(this.name)
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
}
