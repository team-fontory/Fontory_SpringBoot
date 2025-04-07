package org.fontory.fontorybe.font.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FontRequestProduceDto {
    private Long memberId;
    private String fontName;
    private String templateUrl;
}
