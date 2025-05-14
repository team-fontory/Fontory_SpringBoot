package org.fontory.fontorybe.member.controller.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class InitMemberInfoRequest {
    private String nickname;
    private Gender gender;
    private LocalDate birth;
    private Boolean terms;
}
