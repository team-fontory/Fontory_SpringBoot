package org.fontory.fontorybe.font.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FontUpdateDTO {
    private String name;
    private String example;
}
