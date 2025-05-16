package org.fontory.fontorybe.font.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;

@Getter
@Builder
public class FontProgressUpdateDTO {
    @NotNull(message = "폰트 상태는 필수입니다.")
    @Schema(description = "폰트의 상태 (PROGRESS, DONE, FAILED)")
    private FontStatus status;
}
