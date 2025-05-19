package org.fontory.fontorybe.member.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.member.domain.Member;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberUpdateResponse {
    private String nickname;
    private String profileImage;
    private LocalDateTime updatedAt;

    public static MemberUpdateResponse from(Member member) {
        return MemberUpdateResponse.builder()
                .nickname(member.getNickname())
                .profileImage(member.getProfileImageKey())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
