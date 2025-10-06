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
    private String nickname;
    private Gender gender;
    private LocalDate birth;

    public static MyProfileResponse from(Member member) {
        return MyProfileResponse.builder()
                .nickname(member.getNickname())
                .birth(member.getBirth())
                .gender(member.getGender())
                .build();
    }
}
