package org.fontory.fontorybe.bookmark.controller;

import org.fontory.fontorybe.authentication.adapter.inbound.annotation.Login;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.bookmark.controller.dto.BookmarkCreateResponse;
import org.fontory.fontorybe.bookmark.controller.dto.BookmarkDeleteResponse;
import org.fontory.fontorybe.bookmark.controller.port.BookmarkService;
import org.fontory.fontorybe.bookmark.domain.Bookmark;
import org.fontory.fontorybe.font.controller.dto.FontResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "북마크 관리", description = "북마크 API")
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @Operation(summary = "북마크 추가")
    @PostMapping("/{fontId}")
    public ResponseEntity<?> addBookmark(@Login UserPrincipal userPrincipal, @PathVariable Long fontId) {
        Long memberId = userPrincipal.getId();
        log.info("Request received: Add bookmark for font ID: {} by member ID: {}", fontId, memberId);

        Bookmark createdBookmark = bookmarkService.create(memberId, fontId);
        log.info("Response sent: Bookmark created with ID: {}, for font ID: {}, member ID: {}", 
                createdBookmark.getId(), createdBookmark.getFontId(), createdBookmark.getMemberId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BookmarkCreateResponse.from(createdBookmark));
    }

    @Operation(summary = "북마크 삭제")
    @DeleteMapping("/{fontId}")
    public ResponseEntity<?> deleteBookmark(@Login UserPrincipal userPrincipal, @PathVariable Long fontId) {
        Long memberId = userPrincipal.getId();
        log.info("Request received: Delete bookmark for font ID: {} by member ID: {}", fontId, memberId);

        BookmarkDeleteResponse deletedBookmark = bookmarkService.delete(memberId, fontId);
        log.info("Response sent: Bookmark deleted successfully for font ID: {}, member ID: {}, deleted bookmark ID: {}", 
                fontId, memberId, deletedBookmark.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(deletedBookmark);
    }

    @Operation(summary = "북마크한 폰트 보기")
    @GetMapping
    public ResponseEntity<?> getBookmarks(
            @Parameter(description = "페이지 시작 오프셋 (기본값: 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 당 엘리먼트 개수 (기본값: 10)", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "검색 키워드", example = "") @RequestParam(required = false) String keyword,
            @Login UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal.getId();
        log.info("Request received: Get bookmarked fonts for member ID: {}, page: {}, size: {}, keyword: {}", 
                memberId, page, size, keyword);

        Page<FontResponse> fonts = bookmarkService.getBookmarkedFonts(memberId, page, size, keyword);
        log.info("Response sent: Returned {} bookmarked fonts out of {} total, {} pages", 
                fonts.getNumberOfElements(), fonts.getTotalElements(), fonts.getTotalPages());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fonts);
    }
}
