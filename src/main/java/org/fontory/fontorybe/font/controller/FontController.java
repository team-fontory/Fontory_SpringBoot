package org.fontory.fontorybe.font.controller;

import static org.fontory.fontorybe.file.validator.MultipartFileValidator.extractSingleMultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.authentication.adapter.inbound.Login;
import org.fontory.fontorybe.authentication.domain.UserPrincipal;
import org.fontory.fontorybe.file.adapter.inbound.FileRequestMapper;
import org.fontory.fontorybe.file.adapter.inbound.dto.FileUploadResponse;
import org.fontory.fontorybe.file.application.FileService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileDetails;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontCreateResponse;
import org.fontory.fontorybe.font.controller.dto.FontDeleteResponse;
import org.fontory.fontorybe.font.controller.dto.FontDetailResponse;
import org.fontory.fontorybe.font.controller.dto.FontPageResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressUpdateDTO;
import org.fontory.fontorybe.font.controller.dto.FontResponse;
import org.fontory.fontorybe.font.controller.dto.FontUpdateDTO;
import org.fontory.fontorybe.font.controller.dto.FontUpdateResponse;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.domain.Font;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "폰트 관리", description = "폰트 API")
@RestController
@RequestMapping("/fonts")
@RequiredArgsConstructor
public class FontController {
    private final FontService fontService;
    private final FileService fileService;
    private final ObjectMapper objectMapper;
    private final FileRequestMapper fileRequestMapper;
    
    /**
     * Convert an object to JSON string for logging
     * If conversion fails, falls back to toString()
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert object to JSON for logging: {}", e.getMessage());
            return obj.toString();
        }
    }

    @Operation(summary = "폰트 생성")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addFont(
            @Login UserPrincipal userPrincipal,
            @RequestPart("fontCreateDTO") FontCreateDTO fontCreateDTO,
                @Parameter(
                        description = "업로드할 파일. 정확히 1개의 파일만 제공되어야 합니다.",
                        required = true,
                        content = @Content(
                                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                array = @ArraySchema(
                                        schema = @Schema(type = "string", format = "binary"),
                                        maxItems = 1
                                )
                        )
                )
            @RequestPart("file") List<MultipartFile> files
    ) {
        Long memberId = userPrincipal.getId();

        MultipartFile file = extractSingleMultipartFile(files);

        log.info("Request received: Create font and Upload font template image for member ID: {}, request: {}",
                memberId, toJson(fontCreateDTO));

        logFileDetails(file, "Font template image upload");

        FileCreate fileCreate = fileRequestMapper.toFontTemplateImageFileCreate(file, memberId);
        FileDetails fileDetails = fileService.uploadFontTemplateImage(fileCreate);
        FileUploadResponse fileUploadResponse = FileUploadResponse.from(fileDetails);

        Font createdFont = fontService.create(memberId, fontCreateDTO, fileDetails);

        log.info("Response sent: Font created with ID: {}, name: {} and Font template image uploaded successfully, url: {}, fileName: {}, size: {} bytes",
                createdFont.getId(), createdFont.getName(), fileDetails.getFileUrl(), fileDetails.getFileName(), fileDetails.getSize());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(FontCreateResponse.from(createdFont, fileUploadResponse));
    }

    private void logFileDetails(MultipartFile file, String context) {
        log.debug("{} - File details: name='{}', original name='{}', size={} bytes, contentType='{}'",
                context,
                file.getName(),
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType());
    }

    @Operation(summary = "폰트 제작 상황")
    @GetMapping("/progress")
    public ResponseEntity<?> getFontProgress(@Login UserPrincipal userPrincipal) {
        Long memberId = userPrincipal.getId();
        log.info("Request received: Get font progress for member ID: {}", memberId);

        List<FontProgressResponse> fontsProgress = fontService.getFontProgress(memberId);
        log.info("Response sent: Returned {} fonts for progress display", fontsProgress.size());

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
        log.info("Request received: Update font ID: {} by member ID: {}, request: {}", 
                fontId, memberId, toJson(fontUpdateDTO));

        Font updatedFont = fontService.update(memberId, fontId, fontUpdateDTO);
        log.info("Response sent: Font ID: {} updated successfully, name: {}", 
                updatedFont.getId(), updatedFont.getName());

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
        log.info("Request received: Get fonts for member ID: {}, page: {}, size: {}", memberId, page, size);

        Page<FontResponse> fonts = fontService.getFonts(memberId, page, size);
        log.info("Response sent: Returned {} fonts, total pages: {}", fonts.getNumberOfElements(), fonts.getTotalPages());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fonts);
    }

    @Operation(summary = "폰트 상세보기")
    @Parameter(name = "fontId", description = "상세 조회 할 폰트 ID")
    @GetMapping("/{fontId}")
    public ResponseEntity<?> getFont(@PathVariable Long fontId) {
        log.info("Request received: Get font details for font ID: {}", fontId);

        FontDetailResponse font = fontService.getFont(fontId);
        log.info("Response sent: Font details returned for font ID: {}, name: {}", 
                fontId, font.getName());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(font);
    }

    @Operation(summary = "내가 제작한 폰트 삭제")
    @Parameter(name = "fontId", description = "삭제 할 폰트 ID")
    @DeleteMapping("/members/{fontId}")
    public ResponseEntity<?> deleteFont(@PathVariable Long fontId, @Login UserPrincipal userPrincipal) {
        Long memberId = userPrincipal.getId();
        log.info("Request received: Delete font ID: {} by member ID: {}", fontId, memberId);

        FontDeleteResponse deletedFont = fontService.delete(memberId, fontId);
        log.info("Response sent: Font ID: {} deleted successfully", fontId);

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
            @Parameter(description = "검색 키워드", example = "") @RequestParam(required = false) String keyword,
            @Login(required = false) UserPrincipal userPrincipal
    ) {
        Long memberId = userPrincipal != null ? userPrincipal.getId() : null;
        log.info("Request received: Get font page with params - page: {}, size: {}, sortBy: {}, keyword: {}, memberId: {}", 
                page, size, sortBy, keyword, memberId);

        Page<FontPageResponse> fontPage = fontService.getFontPage(memberId, page, size, sortBy, keyword);
        log.info("Response sent: Returned {} fonts, total pages: {}", fontPage.getNumberOfElements(), fontPage.getTotalPages());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fontPage);
    }

    @Operation(summary = "제작자의 다른 폰트 3개 조회")
    @Parameter(name = "fontId", description = "현재 상세보기 한 폰트 ID")
    @GetMapping("/{fontId}/others")
    public ResponseEntity<?> getOtherFontsByWriter(@PathVariable Long fontId) {
        log.info("Request received: Get other fonts by the creator of font ID: {}", fontId);

        List<FontResponse> font = fontService.getOtherFonts(fontId);
        log.info("Response sent: Returned {} other fonts from the same creator", font.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(font);
    }

    @Operation(summary = "나만의 폰트 랭킹 조회")
    @GetMapping("/members/popular")
    public ResponseEntity<?> getMyPopularFonts(@Login UserPrincipal userPrincipal) {
        Long memberId = userPrincipal.getId();
        log.info("Request received: Get popular fonts for member ID: {}", memberId);

        List<FontResponse> fonts = fontService.getMyPopularFonts(memberId);
        log.info("Response sent: Returned {} popular fonts for member", fonts.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fonts);
    }

    @Operation(summary = "인기 폰트 조회")
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularFonts(@Login(required = false) UserPrincipal userPrincipal) {
        Long memberId = userPrincipal != null ? userPrincipal.getId() : null;
        log.info("Request received: Get globally popular fonts, requesting member ID: {}", memberId);

        List<FontResponse> fonts = fontService.getPopularFonts(memberId);
        log.info("Response sent: Returned {} globally popular fonts", fonts.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(fonts);
    }

    @Operation(summary = "폰트 상태 수정")
    @Parameter(name = "fontId", description = "수정할 폰트 ID")
    @PatchMapping("/progress/{fontId}")
    public ResponseEntity<?> updateFontProgress(
            @RequestBody FontProgressUpdateDTO fontProgressUpdateDTO,
            @PathVariable Long fontId
    ) {
        log.info("Request received: Update font progress ID: {}, request: {}",
                fontId, toJson(fontProgressUpdateDTO));

        Font updatedFont = fontService.updateProgress(fontId, fontProgressUpdateDTO);
        log.info("Response sent: Font ID: {} updated successfully, name: {}",
                updatedFont.getId(), updatedFont.getName());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(FontUpdateResponse.from(updatedFont));
    }

    @Operation(summary = "폰트 다운로드")
    @GetMapping("/{fontId}/download")
    public ResponseEntity<?> downloadFont(
            @Login UserPrincipal userPrincipal,
            @PathVariable Long fontId
    ) {
        Long memberId = userPrincipal.getId();
        log.info("Request received: Get font download for font ID : {}, requesting memberId : {}", fontId, memberId);

        FontResponse res = fontService.fontDownload(memberId, fontId);
        log.info("Response sent: Font downloaded with ID: {}", fontId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(res);
    }
}
