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

    @Enumerated(EnumType.STRING)
    private FontStatus status;

    private String example;

    private Long downloadCount;

    private Long bookmarkCount;

    private String ttf;

    private String woff;

    private Long memberId;

    private String templateURL;

    public Font toModel() {
        return Font.builder()
                .id(id)
                .name(name)
                .status(status)
                .example(example)
                .downloadCount(downloadCount)
                .bookmarkCount(bookmarkCount)
                .ttf(ttf)
                .woff(woff)
                .memberId(memberId)
                .templateURL(templateURL)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }

    public static FontEntity from(Font font) {
        return FontEntity.builder()
                .id(font.getId())
                .name(font.getName())
                .status(font.getStatus())
                .example(font.getExample())
                .downloadCount(font.getDownloadCount())
                .bookmarkCount(font.getBookmarkCount())
                .ttf(font.getTtf())
                .woff(font.getWoff())
                .memberId(font.getMemberId())
                .templateURL(font.getTemplateURL())
                .createdAt(font.getCreatedAt())
                .updatedAt(font.getUpdatedAt())
                .build();
    }
}
