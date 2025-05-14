package org.fontory.fontorybe.font.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.member.domain.Member;
import org.slf4j.MDC;

@Getter
@Builder
@AllArgsConstructor
public class FontRequestProduceDto {
    private Long memberId;
    private Long fontId;
    private String fileKey;
    private String fontName;
    private String templateURL;
    private String author;
    private String requestUUID;

    public static FontRequestProduceDto from(Font font, Member member, String templateUrl) {
        return FontRequestProduceDto.builder()
                .memberId(member.getId())
                .fileKey(font.getKey())
                .fontId(font.getId())
                .fontName(font.getName())
                .templateURL(templateUrl)
                .author(member.getNickname())
                .requestUUID(MDC.get("requestId"))
                .build();
    }
}
