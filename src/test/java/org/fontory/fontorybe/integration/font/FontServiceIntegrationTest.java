package org.fontory.fontorybe.integration.font;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.fontory.fontorybe.file.application.FileService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileDetails;
import org.fontory.fontorybe.file.domain.FileType;
import org.fontory.fontorybe.font.controller.dto.FontCreateDTO;
import org.fontory.fontorybe.font.controller.dto.FontDeleteResponse;
import org.fontory.fontorybe.font.controller.dto.FontDetailResponse;
import org.fontory.fontorybe.font.controller.dto.FontPageResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressResponse;
import org.fontory.fontorybe.font.controller.dto.FontProgressUpdateDTO;
import org.fontory.fontorybe.font.controller.dto.FontResponse;
import org.fontory.fontorybe.font.controller.dto.FontUpdateDTO;
import org.fontory.fontorybe.font.controller.port.FontService;
import org.fontory.fontorybe.font.domain.Font;
import org.fontory.fontorybe.font.domain.exception.FontNotFoundException;
import org.fontory.fontorybe.font.domain.exception.FontOwnerMismatchException;
import org.fontory.fontorybe.font.infrastructure.entity.FontStatus;
import org.fontory.fontorybe.font.service.port.FontRequestProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest
@Sql(value = "/sql/createFontTestData.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteFontTestData.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class FontServiceIntegrationTest {
    @Autowired
    private FontService fontService;
    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private FontRequestProducer fontRequestProducer;

    private final Long existMemberId = 999L;
    private final String existMemberName = "existMemberNickName";

    private final Long createdMemberId = 1L;

    private final Long nonExistFontId = -1L;

    private final Long existFontId = 999L;
    private final String existFontName = "테스트폰트";
    private final String existFontStatus = "DONE";
    private final String existFontExample = "이것은 테스트용 예제입니다.";
    private final Long existFontDownloadCount = 0L;
    private final Long existFontBookmarkCount = 0L;
    private final String existFontTtf = "ttf주소";
    private final String existFontWoff = "woff주소";

    private FileDetails fileDetails;

    @BeforeEach
    void setup() {
        fileDetails = FileDetails.builder()
                .fileName("fontTemplateImage.jpg")
                .fileUrl("https://mock-s3.com/fake.jpg")
                .size(1024L)
                .build();

        given(fileService.uploadFontTemplateImage(any())).willReturn(fileDetails);

        doNothing().when(fontRequestProducer).sendFontRequest(any());
    }

    @Test
    @DisplayName("font - create success test")
    void createFontSuccess() {
        // given
        FontCreateDTO dto = FontCreateDTO.builder()
                .name("생성폰트")
                .example("생성 폰트 예제입니다.")
                .build();

        // when
        Font createdFont = fontService.create(existMemberId, dto, fileDetails);

        // then
        assertAll(
                () -> assertThat(createdFont.getId()).isNotNull(),
                () -> assertThat(createdFont.getName()).isEqualTo("생성폰트"),
                () -> assertThat(createdFont.getStatus()).isEqualTo(FontStatus.PROGRESS),
                () -> assertThat(createdFont.getExample()).isEqualTo("생성 폰트 예제입니다."),
                () -> assertThat(createdFont.getMemberId()).isEqualTo(existMemberId),
                () -> assertThat(createdFont.getDownloadCount()).isEqualTo(0L),
                () -> assertThat(createdFont.getBookmarkCount()).isEqualTo(0L),
                () -> assertThat(createdFont.getTtf()).isNull(),
                () -> assertThat(createdFont.getWoff()).isNull(),
                () -> assertThat(createdFont.getCreatedAt()).isNotNull(),
                () -> assertThat(createdFont.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("font - getFontProgress success test")
    void getFontProgressSuccess() {
        for (int i = 1; i <= 6; i++) {
            fontService.create(
                    existMemberId,
                    FontCreateDTO.builder()
                            .name("진행중폰트" + i)
                            .example("예제" + i)
                            .build(),
                    fileDetails
            );
        }

        // when
        List<FontProgressResponse> result = fontService.getFontProgress(existMemberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSizeLessThanOrEqualTo(5);

        result.forEach(font -> assertThat(font.getStatus()).isEqualTo(FontStatus.PROGRESS));
    }

    @Test
    @DisplayName("font - update success test")
    void updateFontSuccess() {
        // given
        FontUpdateDTO dto = FontUpdateDTO.builder()
                .name("수정폰트")
                .example("수정 폰트 예시입니다.")
                .build();

        // when
        Font updated = fontService.update(existMemberId, existFontId, dto);

        // then
        assertAll(
                () -> assertThat(updated.getId()).isEqualTo(existFontId),
                () -> assertThat(updated.getName()).isEqualTo("수정폰트"),
                () -> assertThat(updated.getExample()).isEqualTo("수정 폰트 예시입니다.")
        );
    }

    @Test
    @DisplayName("font - update fail test caused by access denied")
    void updateFontAccessDeniedFail() {
        // given
        FontCreateDTO createDTO = FontCreateDTO.builder()
                .name("다른사람폰트")
                .example("다른예제")
                .build();

        Font elseFont = fontService.create(createdMemberId, createDTO, fileDetails);

        FontUpdateDTO updateDTO = FontUpdateDTO.builder()
                .name("수정시도")
                .example("예제수정")
                .build();

        // when & then
        assertThatThrownBy(() -> fontService.update(existMemberId, elseFont.getId(), updateDTO))
                .isExactlyInstanceOf(FontOwnerMismatchException.class);
    }

    @Test
    @DisplayName("font - getOrThrowById success test")
    void getOrThrowByIdSuccess() {
        // when
        Font foundFont = fontService.getOrThrowById(existFontId);

        // then
        assertAll(
                () -> assertThat(foundFont.getId()).isEqualTo(existFontId),
                () -> assertThat(foundFont.getName()).isEqualTo(existFontName),
                () -> assertThat(foundFont.getStatus().name()).isEqualTo(existFontStatus),
                () -> assertThat(foundFont.getExample()).isEqualTo(existFontExample),
                () -> assertThat(foundFont.getDownloadCount()).isEqualTo(existFontDownloadCount),
                () -> assertThat(foundFont.getBookmarkCount()).isEqualTo(existFontBookmarkCount),
                () -> assertThat(foundFont.getTtf()).isEqualTo(existFontTtf),
                () -> assertThat(foundFont.getWoff()).isEqualTo(existFontWoff),
                () -> assertThat(foundFont.getMemberId()).isEqualTo(existMemberId),
                () -> assertThat(foundFont.getCreatedAt()).isNotNull(),
                () -> assertThat(foundFont.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("font - getOrThrowById fail test caused by not found")
    void getOrThrowByIdFail() {
        // when & then
        assertThatThrownBy(() -> fontService.getOrThrowById(nonExistFontId))
                .isExactlyInstanceOf(FontNotFoundException.class);
    }

    @Test
    @DisplayName("font - getFonts success test")
    void getFontsSuccess() {
        // given
        for (int i = 1; i <= 7; i++) {
            fontService.create(
                    existMemberId,
                    FontCreateDTO.builder()
                            .name("폰트" + i)
                            .example("예제" + i)
                            .build(),
                    fileDetails
            );
        }

        int page = 0;
        int size = 5;

        // when
        Page<FontResponse> result = fontService.getFonts(existMemberId, page, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSizeLessThanOrEqualTo(size);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(7);

        result.getContent().forEach(font ->
                assertThat(font.getMemberId()).isEqualTo(existMemberId)
        );

        List<Long> ids = result.getContent().stream()
                .map(FontResponse::getId)
                .toList();

        List<Long> sorted = new ArrayList<>(ids);
        sorted.sort(Comparator.reverseOrder());

        assertThat(ids).isEqualTo(sorted);
    }

    @Test
    @DisplayName("font - getFont detail success test")
    void getFontDetailSuccess() {
        // when
        FontDetailResponse detail = fontService.getFont(existFontId);

        // then
        assertAll(
                () -> assertThat(detail.getId()).isEqualTo(existFontId),
                () -> assertThat(detail.getName()).isEqualTo(existFontName),
                () -> assertThat(detail.getExample()).isEqualTo(existFontExample),
                () -> assertThat(detail.getWriterName()).isEqualTo(existMemberName),
                () -> assertThat(detail.getDownloadCount()).isEqualTo(existFontDownloadCount),
                () -> assertThat(detail.getBookmarkCount()).isEqualTo(existFontBookmarkCount)
        );
    }

    @Test
    @DisplayName("font - delete success test")
    void deleteFontSuccess() {
        // when
        FontDeleteResponse response = fontService.delete(existMemberId, existFontId);

        // then
        assertThat(response.getId()).isEqualTo(existFontId);
        assertThatThrownBy(() -> fontService.getOrThrowById(existFontId))
                .isExactlyInstanceOf(FontNotFoundException.class);
    }

    @Test
    @DisplayName("font - delete fail test caused by access denied")
    void deleteFontAccessDeniedFail() {
        // given
        FontCreateDTO createDTO = FontCreateDTO.builder()
                .name("다른사람폰트")
                .example("다른예제")
                .build();

        Font elseFont = fontService.create(createdMemberId, createDTO, fileDetails);

        // when & then
        assertThatThrownBy(() -> fontService.delete(existMemberId, elseFont.getId()))
                .isExactlyInstanceOf(FontOwnerMismatchException.class);
    }

    @Test
    @DisplayName("font - getFontPage success test")
    void getFontPageSuccess() {
        // given
        Font font1 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("페이지폰트1")
                        .example("예제1")
                        .build(),
                fileDetails
        );

        Font font2 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("페이지폰트2")
                        .example("예제2")
                        .build(),
                fileDetails
        );

        Font font3 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("페이지폰트")
                        .example("예제3")
                        .build(),
                fileDetails
        );

        for (int i = 0; i < 10; i++) {
            fontService.fontDownload(existMemberId, font1.getId());
        }
        for (int i = 0; i < 5; i++) {
            fontService.fontDownload(existMemberId, font2.getId());
        }

        int page = 0;
        int size = 5;
        String sortBy = "downloadCount";
        String keyword = "페이지";

        // when
        Page<FontPageResponse> result = fontService.getFontPage(existMemberId, page, size, sortBy, keyword);

        // then
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent()).allMatch(font -> font.getName().contains(keyword));

        List<Long> downloadCounts = result.getContent().stream()
                .map(FontPageResponse::getDownloadCount)
                .toList();

        List<Long> sorted = new ArrayList<>(downloadCounts);
        sorted.sort(Comparator.reverseOrder());

        assertThat(downloadCounts).isEqualTo(sorted);
    }

    @Test
    @DisplayName("font - getOtherFonts success test")
    void getOtherFontsSuccess() {
        // when
        List<FontResponse> result = fontService.getOtherFonts(existFontId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isLessThanOrEqualTo(3);

        result.forEach(font -> {
            assertThat(font.getId()).isNotEqualTo(existFontId);
            assertThat(font.getMemberId()).isEqualTo(existMemberId);
        });
    }

    @Test
    @DisplayName("font - getMyPopularFonts success test")
    void getMyPopularFontsSuccess() {
        // given
        Font font1 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트1")
                        .example("예1")
                        .build(),
                fileDetails
                );

        Font font2 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트2")
                        .example("예2")
                        .build(),
                fileDetails
                );

        Font font3 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트3")
                        .example("예3")
                        .build(),
                fileDetails
                );

        for (int i = 0; i < 5; i++) {
            fontService.fontDownload(existMemberId, font1.getId());
        }
        for (int i = 0; i < 3; i++) {
            fontService.fontDownload(existMemberId, font2.getId());
        }
        for (int i = 0; i < 1; i++) {
            fontService.fontDownload(existMemberId, font3.getId());
        }

        // when
        List<FontResponse> result = fontService.getPopularFonts(existMemberId);

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(3);

        List<Long> scores = result.stream()
                .map(font -> font.getDownloadCount() + font.getBookmarkCount())
                .toList();

        List<Long> sorted = new ArrayList<>(scores);
        sorted.sort(Comparator.reverseOrder());

        assertThat(scores).isEqualTo(sorted);

        result.forEach(font -> assertThat(font.getMemberId()).isEqualTo(existMemberId));
    }

    @Test
    @DisplayName("font - getPopularFonts success test")
    void getPopularFontsSuccess() {
        // given
        Font font1 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트1")
                        .example("예1")
                        .build(),
                fileDetails
        );

        Font font2 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트2")
                        .example("예2")
                        .build(),
                fileDetails
        );

        Font font3 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트3")
                        .example("예3")
                        .build(),
                fileDetails
        );

        for (int i = 0; i < 5; i++) {
            fontService.fontDownload(existMemberId, font1.getId());
        }
        for (int i = 0; i < 3; i++) {
            fontService.fontDownload(existMemberId, font2.getId());
        }
        for (int i = 0; i < 1; i++) {
            fontService.fontDownload(existMemberId, font3.getId());
        }

        // when
        List<FontResponse> result = fontService.getPopularFonts(existMemberId);

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(3);

        List<Long> scores = result.stream()
                .map(font -> font.getDownloadCount() + font.getBookmarkCount())
                .toList();

        List<Long> sorted = new ArrayList<>(scores);
        sorted.sort(Comparator.reverseOrder());

        assertThat(scores).isEqualTo(sorted);

        assertThat(result.get(0).getName()).isEqualTo("폰트1");
        assertThat(result.get(1).getName()).isEqualTo("폰트2");
        assertThat(result.get(2).getName()).isEqualTo("폰트3");
    }

    @Test
    @DisplayName("font - updateProgress success test")
    void updateFontProgressSuccess() {
        // given
        FontCreateDTO dto = FontCreateDTO.builder()
                .name("진행중폰트")
                .example("예제입니다")
                .build();

        Font createdFont = fontService.create(existMemberId, dto, fileDetails);

        FontProgressUpdateDTO progressDto = FontProgressUpdateDTO.builder()
                .status(FontStatus.DONE)
                .build();

        // when
        Font updatedFont = fontService.updateProgress(createdFont.getId(), progressDto);

        // then
        assertThat(updatedFont.getStatus()).isEqualTo(FontStatus.DONE);
    }
}
