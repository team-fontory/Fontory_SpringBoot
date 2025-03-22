package org.fontory.fontorybe.font.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.fontory.fontorybe.authentication.adapter.inbound.Login;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontCreateResponse;
import org.fontory.fontorybe.font.controller.dto.FontDeleteResponse;
import org.fontory.fontorybe.font.controller.dto.FontDetailResponse;
import org.fontory.fontorybe.font.controller.dto.FontPageResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public ResponseEntity<?> addFont(@RequestBody FontCreateDTO fontCreateDTO, @Login UserPrincipal userPrincipal) {
        Long memberId = userPrincipal.getId();

        Font createdFont = fontService.create(memberId, fontCreateDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(FontCreateResponse.from(createdFont));
    }

    @Operation(summary = "폰트 제작 상황")
    @GetMapping("/progress")
    public ResponseEntity<?> getFontProgress(@Login UserPrincipal userPrincipal) {
        Long memberId = userPrincipal.getId();

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
            @PathVariable Long fontId,
            @Login UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getId();

        Font updatedFont = fontService.update(memberId, fontId, fontUpdateDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(FontUpdateResponse.from(updatedFont));
    }

    @Operation(summary = "내가 제작한 폰트")
    @GetMapping("/members")
    public ResponseEntity<?> getFonts(
            @Parameter(description = "페이지 시작 오프셋 (기본값: 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 당 엘리먼트 개수 (기본값: 10)", example = "10") @RequestParam(defaultValue = "10") int size,
            @Login UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getId();

        Page<FontResponse> fonts = fontService.getFonts(memberId, page, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fonts);
    }

    @Operation(summary = "폰트 상세보기")
    @Parameter(name = "fontId", description = "상세 조회 할 폰트 ID")
    @GetMapping("/{fontId}")
    public ResponseEntity<?> getFont(@PathVariable Long fontId) {
        FontDetailResponse font = fontService.getFont(fontId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(font);
    }

    @Operation(summary = "내가 제작한 폰트 삭제")
    @Parameter(name = "fontId", description = "삭제 할 폰트 ID")
    @DeleteMapping("/members/{fontId}")
    public ResponseEntity<?> deleteFont(@PathVariable Long fontId, @Login UserPrincipal userPrincipal) {
        Long memberId = userPrincipal.getId();

        FontDeleteResponse deletedFont = fontService.delete(memberId, fontId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(deletedFont);
    }

    @Operation(summary = "폰트 둘러보기")
    @GetMapping
    public ResponseEntity<?> getFontPage(
            @Parameter(description = "페이지 시작 오프셋 (기본값: 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 당 엘리먼트 개수 (기본값: 10)", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준 (예: createdAt, downloadCount, bookmarkCount)", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "검색 키워드", example = "") @RequestParam(required = false) String keyword
    ) {
        Page<FontPageResponse> fontPage = fontService.getFontPage(page, size, sortBy, keyword);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fontPage);
    }

    @Operation(summary = "제작자의 다른 폰트 3개 조회")
    @Parameter(name = "fontId", description = "현재 상세보기 한 폰트 ID")
    @GetMapping("/{fontId}/others")
    public ResponseEntity<?> getOtherFontsByWriter(@PathVariable Long fontId) {
        List<FontResponse> font = fontService.getOtherFonts(fontId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(font);
    }

    @Operation(summary = "나만의 폰트 랭킹 조회")
    @GetMapping("/members/popular")
    public ResponseEntity<?> getMyPopularFonts(@Login UserPrincipal userPrincipal) {
        Long memberId = userPrincipal.getId();

        List<FontResponse> fonts = fontService.getMyPopularFonts(memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fonts);
    }

    @Operation(summary = "인기 폰트 조회")
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularFonts() {

        List<FontResponse> fonts = fontService.getPopularFonts();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fonts);
    }
}
