package org.fontory.fontorybe.font.service.dto;

import java.util.UUID;
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
    private String fontName;
    private String templateURL;
    private String author;
    private String requestUUID;

    public static FontRequestProduceDto from(Font font, Member member) {
        return FontRequestProduceDto.builder()
                .memberId(member.getId())
                .fontId(font.getId())
                .fontName(font.getName())
                .templateURL(font.getTemplateURL())
                .author(member.getNickname())
                .requestUUID(MDC.get("requestId"))
                .build();
    }
}
