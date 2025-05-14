package org.fontory.fontorybe.font.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FontCreateDTO {

    @NotBlank(message = "폰트 이름은 필수 입력 값입니다.")
    private String name;

    @NotBlank(message = "폰트 예시는 필수 입력 값입니다.")
    private String example;

    @Pattern(regexp = "^$|^01[016-9]\\d{7,8}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    private String phoneNumber;
}
