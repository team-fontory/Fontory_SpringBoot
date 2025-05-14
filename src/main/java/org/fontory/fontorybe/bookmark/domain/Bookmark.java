package org.fontory.fontorybe.bookmark.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Bookmark {

    private Long id;

    private Long memberId;

    private Long fontId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Bookmark from(Long memberId, Long fontId) {
        return Bookmark.builder()
                .memberId(memberId)
                .fontId(fontId)
                .build();
    }
}
