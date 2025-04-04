package org.fontory.fontorybe.font.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;

@Getter
@Builder
public class FontProgressUpdateDTO {
    @Schema(description = "폰트의 상태 (PROGRESS, DONE)")
    private FontStatus status;
}
