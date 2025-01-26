package org.fontory.fontorybe.member.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.fontory.fontorybe.member.domain.dto.MemberCreateDto;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.provide.domain.Provide;
import org.fontory.fontorybe.provide.infrastructure.entity.ProvideEntity;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;

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

    private Long provideId;

    public static Member from(MemberCreateDto memberCreateDto, Provide provide) {
        return Member.builder()
                .nickname(memberCreateDto.getNickname())
                .gender(memberCreateDto.getGender())
                .birth(memberCreateDto.getBirth())
                .terms(memberCreateDto.getTerms())
                .profileImage(memberCreateDto.getProfileImage())
                .provideId(provide.getId())
                .build();
    }
}
