package org.fontory.fontorybe.member.controller.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;

@Getter
@Builder
@AllArgsConstructor
public class MemberCreateRequest {
    private String nickname;
    private Gender gender;
    private LocalDate birth;
    private Boolean terms;
    private String profileImage;
}
