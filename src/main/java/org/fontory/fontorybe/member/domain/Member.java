package org.fontory.fontorybe.member.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
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

    private String profileImageKey;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private Long provideId;

    public static Member from(MemberCreateRequest memberCreateRequestDto, Provide provide) {
        return Member.builder()
                .nickname(memberCreateRequestDto.getNickname())
                .gender(memberCreateRequestDto.getGender())
                .birth(memberCreateRequestDto.getBirth())
                .terms(memberCreateRequestDto.getTerms())
                .profileImageKey(memberCreateRequestDto.getProfileImageKey())
                .provideId(provide.getId())
                .build();
    }

    public Member update(MemberUpdateRequest memberUpdateRequest) {
        return Member.builder()
                //tobe update
                .nickname(memberUpdateRequest.getNickname())
                .terms(memberUpdateRequest.getTerms())
                .profileImageKey(memberUpdateRequest.getProfileImageKey())

                //not to be update
                .id(this.id)
                .gender(this.gender)
                .birth(this.birth)
                .createdAt(this.createdAt)
                .provideId(this.provideId)
                .deletedAt(this.deletedAt)
                .provideId(this.provideId)
                .build();
    }
    public void disable() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean getTerms() {
        return this.terms;
    }

    public Member setProfileImageKey(String profileImageKey) {
        return Member.builder()
                .profileImageKey(profileImageKey)

                .id(this.id)
                .nickname(this.nickname)
                .gender(this.gender)
                .birth(this.birth)
                .createdAt(this.createdAt)
                .provideId(this.provideId)
                .deletedAt(this.deletedAt)
                .terms(this.terms)
                .provideId(this.provideId)
                .build();
    }
}
