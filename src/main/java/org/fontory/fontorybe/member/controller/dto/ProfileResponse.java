package org.fontory.fontorybe.member.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.fontory.fontorybe.member.domain.Member;

@Getter
@Builder
@ToString
public class ProfileResponse {
    private Long memberId;
    private String nickname;
    private String profileImageUrl;

    public static ProfileResponse from(Member member, String url) {
        return ProfileResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImageUrl(url)
                .build();
    }
}
