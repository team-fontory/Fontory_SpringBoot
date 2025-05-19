package org.fontory.fontorybe.member.controller.dto;

import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MemberCreateResponse {
    private final String nickname;
    private final Gender gender;
    private final String profileImageUrl;
    private final LocalDate birth;
    private final LocalDateTime createdAt;

    public static MemberCreateResponse from(Member member, String url) {
        return MemberCreateResponse.builder()
                .nickname(member.getNickname())
                .gender(member.getGender())
                .profileImageUrl(url)
                .birth(member.getBirth())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
