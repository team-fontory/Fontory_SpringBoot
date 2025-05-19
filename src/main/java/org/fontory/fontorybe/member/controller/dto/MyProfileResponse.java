package org.fontory.fontorybe.member.controller.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;

import java.time.LocalDate;

@Getter
@Builder
@ToString
public class MyProfileResponse {
    private Long memberId;
    private String nickname;
    private Gender gender;
    private LocalDate birth;
    private String profileImageUrl;

    public static MyProfileResponse from(Member member, String url) {
        return MyProfileResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .birth(member.getBirth())
                .gender(member.getGender())
                .profileImageUrl(url)
                .build();
    }
}
