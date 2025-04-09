package org.fontory.fontorybe.font.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FontRequestProduceDto {
    private Long memberId;
    private Long fontId;
    private String fontName;
    private String templateURL;
    private String author;
    private String requestUUID;
}
