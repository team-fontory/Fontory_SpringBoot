package org.fontory.fontorybe.member.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.member.domain.Member;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberCreateResponse {
    private Long id;
    private String nickname;
    private String profileImage;
    private Boolean terms;
    private LocalDateTime createdAt;

    public static MemberCreateResponse from(Member member) {
        return MemberCreateResponse.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .terms(member.getTerms())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
