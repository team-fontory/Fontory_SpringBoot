package org.fontory.fontorybe.font.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FontUpdateDTO {
    @NotBlank(message = "폰트 예시는 필수 입력 값입니다.")
    @Size(min = 10, max = 255, message = "폰트 예시는 10자 이상 255자 이하로 입력해주세요.")
    private String example;
}
