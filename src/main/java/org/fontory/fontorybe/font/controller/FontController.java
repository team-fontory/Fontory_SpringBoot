package org.fontory.fontorybe.font.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontCreateResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressResponse;
import org.fontory.fontorybe.font.controller.dto.FontResponse;
import org.fontory.fontorybe.font.controller.dto.FontUpdateDTO;
import org.fontory.fontorybe.font.controller.dto.FontUpdateResponse;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.domain.Font;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        Long memberId = 1L;

        Font createdFont = fontService.create(memberId, fontCreateDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(FontCreateResponse.from(createdFont));
    }

    @Operation(summary = "폰트 제작 상황")
    @GetMapping("/progress")
    public ResponseEntity<?> getFontProgress() {
        Long memberId = 1L;

        List<FontProgressResponse> fontsProgress = fontService.getFontProgress(memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fontsProgress);
    }

    @Operation(summary = "폰트 정보 수정")
    @Parameter(name = "fontId", description = "수정할 폰트 ID")
    @PutMapping("/{fontId}")
    public ResponseEntity<?> updateFont(
            @RequestBody FontUpdateDTO fontUpdateDTO,
            @PathVariable Long fontId
    ) {
        Long memberId = 1L;

        Font updatedFont = fontService.update(memberId, fontId, fontUpdateDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(FontUpdateResponse.from(updatedFont));
    }

    @Operation(summary = "내가 제작한 폰트")
    @GetMapping("/members")
    public ResponseEntity<?> getFonts(
            @Parameter(description = "페이지 시작 오프셋 (기본값: 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 당 엘리먼트 개수 (기본값: 10)", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = 1L;

        Page<FontResponse> fonts = fontService.getFonts(memberId, page, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fonts);
    }
}
