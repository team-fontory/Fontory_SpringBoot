package org.fontory.fontorybe.font.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FontCreateDTO {

    @NotBlank(message = "폰트 이름은 필수 입력 값입니다.")
    @Size(min = 2, max = 30, message = "폰트 이름은 2자 이상 30자 이하로 입력해주세요.")
    @Pattern(regexp = "^[가-힣0-9]{2,30}$", message = "한글과 숫자만 입력할 수 있습니다. (예: 가나다체123)")
    private String name;

    @NotBlank(message = "폰트 영어 이름은 필수 입력 값입니다.")
    @Size(min = 2, max = 30, message = "폰트 이름은 2자 이상 30자 이하로 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9]{2,30}$", message = "영문 대소문자와 숫자만 입력할 수 있습니다. (예: ABCD123)")
    private String engName;

    @NotBlank(message = "폰트 예시는 필수 입력 값입니다.")
    @Size(min = 10, max = 255, message = "폰트 예시는 10자 이상 255자 이하로 입력해주세요.")
    private String example;

    @Pattern(regexp = "^$|^01[016-9]\\d{7,8}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    private String phoneNumber;
}
