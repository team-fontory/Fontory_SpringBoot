package org.fontory.fontorybe.member.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.*;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.controller.dto.MemberUpdateRequest;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.member.infrastructure.entity.MemberStatus;
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

    private MemberStatus status;

    public static Member fromDefaults(MemberDefaults memberDefaults, String nickname, Provide provide) {
        return Member.builder()
                .nickname(nickname)
                .gender(memberDefaults.getGender())
                .birth(memberDefaults.getBirth())
                .terms(memberDefaults.getTerms())
                .profileImageKey(memberDefaults.getProfileImageKey())
                .provideId(provide.getId())
                .status(MemberStatus.ONBOARDING)
                .build();
    }

    public Member initNewMemberInfo(InitMemberInfoRequest initNewMemberInfo, String profileImageKey) {
        return Member.builder()
                .id(this.id)
                .nickname(initNewMemberInfo.getNickname())
                .gender(initNewMemberInfo.getGender())
                .birth(initNewMemberInfo.getBirth())
                .terms(initNewMemberInfo.getTerms())
                .profileImageKey(profileImageKey)
                .createdAt(this.createdAt)
                .provideId(this.provideId)
                .deletedAt(this.deletedAt)
                .provideId(this.provideId)
                .status(MemberStatus.ACTIVATE)
                .build();
    }

    public Member initNewMemberInfo(InitMemberInfoRequest initNewMemberInfo) {
        return Member.builder()
                .id(this.id)
                .nickname(initNewMemberInfo.getNickname())
                .gender(initNewMemberInfo.getGender())
                .birth(initNewMemberInfo.getBirth())
                .terms(initNewMemberInfo.getTerms())
                .profileImageKey(this.profileImageKey)
                .createdAt(this.createdAt)
                .provideId(this.provideId)
                .deletedAt(this.deletedAt)
                .provideId(this.provideId)
                .status(MemberStatus.ACTIVATE)
                .build();
    }

    public Member update(MemberUpdateRequest memberUpdateRequest) {
        return Member.builder()
                //tobe update
                .nickname(memberUpdateRequest.getNickname())
                .terms(memberUpdateRequest.getTerms())

                //not to be update
                .id(this.id)
                .gender(this.gender)
                .birth(this.birth)
                .createdAt(this.createdAt)
                .provideId(this.provideId)
                .deletedAt(this.deletedAt)
                .provideId(this.provideId)
                .status(this.status)
                .profileImageKey(this.profileImageKey)
                .build();
    }
    public void disable() {
        this.status = MemberStatus.DEACTIVATE;
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
                .status(this.status)
                .build();
    }
}
