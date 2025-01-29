package org.fontory.fontorybe.member.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class MemberUpdateDto {
    private String nickname;
    private Gender gender;
    private LocalDate birth;
    private Boolean terms;
    private String profileImage;
}
