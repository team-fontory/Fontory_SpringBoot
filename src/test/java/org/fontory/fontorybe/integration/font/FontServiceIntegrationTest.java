package org.fontory.fontorybe.integration.font;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.fontory.fontorybe.file.application.port.FileRepository;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.font.controller.dto.*;
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
    private final String existFontTemplateName = "fontTemplateImage.jpg";
    private final Long existFontSize = 12345L;
    private final String existFontKey = "key";
    private final String existFontTemplateExtension = "jpg";

    private FileUploadResult fileDetails;
    private final FontProgressUpdateDTO fontProgressUpdateDTO = FontProgressUpdateDTO.builder().status(FontStatus.DONE).build();

    @BeforeEach
    void setup() {
        fileDetails = FileUploadResult.builder()
                .id(existFontId)
                .fileName(existFontTemplateName)
                .fileUrl("https://mock-s3.com/fake.jpg")
                .size(existFontSize)
                .build();
        FileMetadata fileMetadata = FileMetadata.builder()
                        .id(existFontId)
                        .fileName(existFontTemplateName)
                        .key(existFontKey)
                        .extension(existFontTemplateExtension)
                        .size(existFontSize)
                        .build();

        given(fileService.getOrThrowById(any())).willReturn(fileMetadata);
        given(fileService.uploadFontTemplateImage(any(), any())).willReturn(fileDetails);

        doNothing().when(fontRequestProducer).sendFontRequest(any());
    }

    @Test
    @DisplayName("font - create success test")
    void createFontSuccess() {
        // given
        FontCreateDTO dto = FontCreateDTO.builder()
                .name("생성폰트")
                .engName("ENG1")
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
                () -> assertThat(createdFont.getDownloadCount()).isZero(),
                () -> assertThat(createdFont.getBookmarkCount()).isZero(),
                () -> assertThat(createdFont.getKey()).isEqualTo(existFontKey),
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
                            .engName("ENG" + i)
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
                () -> assertThat(foundFont.getKey()).isNotNull(),
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
            Font font = fontService.create(
                    existMemberId,
                    FontCreateDTO.builder()
                            .name("폰트" + i)
                            .engName("ENG" + i)
                            .example("예제" + i)
                            .build(),
                    fileDetails
            );
            fontService.updateProgress(font.getId(), fontProgressUpdateDTO);
        }

        int page = 0;
        int size = 5;

        // when
        Page<FontResponse> result = fontService.getFonts(existMemberId, page, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSizeLessThanOrEqualTo(size);
        assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(7);

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
        FontResponse detail = fontService.getFont(existFontId, null);

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
                .engName("ENG1")
                .example("다른예제")
                .build();

        Font elseFont = fontService.create(createdMemberId, createDTO, fileDetails);
        fontService.updateProgress(elseFont.getId(), fontProgressUpdateDTO);

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
                        .engName("ENG1")
                        .example("예제1")
                        .build(),
                fileDetails
        );

        Font font2 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("페이지폰트2")
                        .engName("ENG2")
                        .example("예제2")
                        .build(),
                fileDetails
        );

        Font font3 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("페이지폰트")
                        .engName("ENG3")
                        .example("예제3")
                        .build(),
                fileDetails
        );

        fontService.updateProgress(font1.getId(), fontProgressUpdateDTO);
        fontService.updateProgress(font2.getId(), fontProgressUpdateDTO);
        fontService.updateProgress(font3.getId(), fontProgressUpdateDTO);

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
        assertThat(result).hasSizeLessThanOrEqualTo(3);

        result.forEach(font -> {
            assertThat(font.getId()).isNotEqualTo(existFontId);
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
                        .engName("ENG1")
                        .example("예1")
                        .build(),
                fileDetails
                );

        Font font2 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트2")
                        .engName("ENG2")
                        .example("예2")
                        .build(),
                fileDetails
                );

        Font font3 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트3")
                        .engName("ENG3")
                        .example("예3")
                        .build(),
                fileDetails
                );

        fontService.updateProgress(font1.getId(), fontProgressUpdateDTO);
        fontService.updateProgress(font2.getId(), fontProgressUpdateDTO);
        fontService.updateProgress(font3.getId(), fontProgressUpdateDTO);

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

    }

    @Test
    @DisplayName("font - getPopularFonts success test")
    void getPopularFontsSuccess() {
        // given
        Font font1 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트1")
                        .engName("ENG1")
                        .example("예1")
                        .build(),
                fileDetails
        );

        Font font2 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트2")
                        .engName("ENG2")
                        .example("예2")
                        .build(),
                fileDetails
        );

        Font font3 = fontService.create(
                existMemberId,
                FontCreateDTO.builder()
                        .name("폰트3")
                        .engName("ENG3")
                        .example("예3")
                        .build(),
                fileDetails
        );

        fontService.updateProgress(font1.getId(), fontProgressUpdateDTO);
        fontService.updateProgress(font2.getId(), fontProgressUpdateDTO);
        fontService.updateProgress(font3.getId(), fontProgressUpdateDTO);

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
                .engName("ENG1")
                .example("예제입니다")
                .build();

        Font createdFont = fontService.create(existMemberId, dto, fileDetails);

        FontProgressUpdateDTO progressDto = FontProgressUpdateDTO.builder()
                .status(FontStatus.DONE)
                .build();

        // when
        FontUpdateResponse fontUpdateResponse = fontService.updateProgress(createdFont.getId(), progressDto);

        // then
        assertThat(fontUpdateResponse.getStatus()).isEqualTo(FontStatus.DONE);
    }
}
