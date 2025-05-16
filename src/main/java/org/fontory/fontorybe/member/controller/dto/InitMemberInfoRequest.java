package org.fontory.fontorybe.member.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
    private String nickname;

    @NotNull(message = "성별을 선택해주세요.")
    private Gender gender;

    @NotNull(message = "생년월일을 입력해주세요.")
    @Past(message = "생년월일은 과거 날짜만 가능합니다.")
    private LocalDate birth;

    @NotNull(message = "이용약관 동의 여부를 선택해주세요.")
    private Boolean terms;
}
