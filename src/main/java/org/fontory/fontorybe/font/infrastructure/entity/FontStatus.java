package org.fontory.fontorybe.font.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FontStatus {
    PROGRESS("제작 중"),
    DONE("제작 완료");

    private final String key;
}
