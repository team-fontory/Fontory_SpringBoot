package org.fontory.fontorybe.font.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontCreateResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressResponse;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.domain.Font;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "폰트 관리", description = "폰트 API")
@RestController
@RequestMapping("/fonts")
@RequiredArgsConstructor
public class FontController {
    private final FontService fontService;

    @Operation(summary = "폰트 생성")
    @PostMapping
    public ResponseEntity<?> addFont(@RequestBody FontCreateDTO fontCreateDTO) {
        Long requestMemberId = 1L;

        Font createdFont = fontService.create(requestMemberId, fontCreateDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(FontCreateResponse.from(createdFont));
    }

    @Operation(summary = "폰트 제작 상황")
    @GetMapping("/progress")
    public ResponseEntity<?> getFontProgress() {
        Long requestMemberId = 1L;

        List<FontProgressResponse> fontsProgress = fontService.getFontProgress(requestMemberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fontsProgress);
    }
}
