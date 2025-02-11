package org.fontory.fontorybe.member.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;
import org.fontory.fontorybe.member.controller.dto.MemberCreate;
import org.fontory.fontorybe.member.controller.dto.MemberUpdate;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.provide.domain.Provide;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {
    private Long id;

    private String nickname;

    private Gender gender;

    private LocalDate birth;

    private boolean terms;

    private String profileImage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private Long provideId;

    public static Member from(MemberCreate memberCreateDto, Provide provide) {
        return Member.builder()
                .nickname(memberCreateDto.getNickname())
                .gender(memberCreateDto.getGender())
                .birth(memberCreateDto.getBirth())
                .terms(memberCreateDto.getTerms())
                .profileImage(memberCreateDto.getProfileImage())
                .provideId(provide.getId())
                .build();
    }

    public Member update(MemberUpdate memberUpdate) {
        return Member.builder()
                //tobe update
                .nickname(memberUpdate.getNickname())
                .terms(memberUpdate.getTerms())
                .profileImage(memberUpdate.getProfileImage())
                //not to be update
                .id(this.id)
                .gender(this.gender)
                .birth(this.birth)
                .createdAt(this.createdAt)
                .provideId(this.provideId)
                .deletedAt(this.deletedAt)
                .build();
    }
    public void disable() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean getTerms() {
        return this.terms;
    }
}
