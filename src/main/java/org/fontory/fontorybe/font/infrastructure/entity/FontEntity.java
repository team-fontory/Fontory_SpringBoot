package org.fontory.fontorybe.font.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.fontory.fontorybe.common.domain.BaseEntity;
import org.fontory.fontorybe.font.domain.Font;

@Entity
@Getter
@Table(name = "font")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class FontEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "font_id")
    private Long id;

    private String name;

    private String engName;

    @Enumerated(EnumType.STRING)
    private FontStatus status;

    private String example;

    private Long downloadCount;

    private Long bookmarkCount;

    @Column(name = "file_key")
    private String key;

    private Long memberId;

    public Font toModel() {
        return Font.builder()
                .id(id)
                .name(name)
                .engName(engName)
                .status(status)
                .example(example)
                .downloadCount(downloadCount)
                .bookmarkCount(bookmarkCount)
                .key(key)
                .memberId(memberId)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }

    public static FontEntity from(Font font) {
        return FontEntity.builder()
                .id(font.getId())
                .name(font.getName())
                .engName(font.getEngName())
                .status(font.getStatus())
                .example(font.getExample())
                .downloadCount(font.getDownloadCount())
                .bookmarkCount(font.getBookmarkCount())
                .key(font.getKey())
                .memberId(font.getMemberId())
                .createdAt(font.getCreatedAt())
                .updatedAt(font.getUpdatedAt())
                .build();
    }
}
