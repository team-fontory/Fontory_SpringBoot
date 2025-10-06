package org.fontory.fontorybe.unit.file;

import org.fontory.fontorybe.file.adapter.inbound.FileRequestMapper;
import org.fontory.fontorybe.file.adapter.inbound.exception.FileEmptyException;
import org.fontory.fontorybe.file.adapter.inbound.exception.MissingFileExtensionException;
import org.fontory.fontorybe.file.adapter.inbound.exception.UnsupportedFileTypeException;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileType;
import org.fontory.fontorybe.member.controller.dto.InitMemberInfoRequest;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.unit.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class FileRequestMapperTest {
    private FileRequestMapper fileRequestMapper;
    private TestContainer testContainer;

    // 테스트 값
    private Long existMemberId;
    private Member existMember;

    @BeforeEach
    void init() {
        testContainer = new TestContainer();
        fileRequestMapper = testContainer.fileRequestMapper;

        // Create a member for testing
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(
                Provider.GOOGLE,
                UUID.randomUUID().toString(),
                "test@example.com"
        );

        existMember = testContainer.create(createMemberRequest("testUser") ,testContainer.provideService.create(provideCreateDto));
        existMemberId = existMember.getId();
        System.out.println("existMemberId = " + existMemberId);
    }

    private static MockMultipartFile createValidImageFile(String filename, String contentType) {
        return new MockMultipartFile(
                "file",
                filename,
                contentType,
                "Test file content".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static MockMultipartFile createEmptyFile(String filename) {
        return new MockMultipartFile(
                "file",
                filename,
                "image/jpeg",
                new byte[0]
        );
    }

    private static MockMultipartFile createFileWithoutExtension() {
        return new MockMultipartFile(
                "file",
                "fileWithoutExtension",
                "image/jpeg",
                "Test file content".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static MockMultipartFile createUnsupportedFile() {
        return new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "Test file content".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static MockMultipartFile createNullOriginalFilenameFile() {
        return new MockMultipartFile(
                "file",
                null,
                "image/jpeg",
                "Test file content".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static MockMultipartFile createFileWithDifferentExtensions(String extension) {
        return new MockMultipartFile(
                "file",
                "test." + extension,
                extension.equals("png") ? "image/png" : "image/jpeg",
                "Test file content".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static InitMemberInfoRequest createMemberRequest(String nickname) {
        return new InitMemberInfoRequest(
                nickname,
                Gender.MALE,
                LocalDate.of(2025, 1, 26)
        );
    }

    @Test
    @DisplayName("toProfileImageFileCreate - should create FileCreate object correctly")
    void toProfileImageFileCreateTest() {
        // given
        MockMultipartFile mockFile = createValidImageFile("profile.jpg", "image/jpeg");

        // when
        System.out.println("existMemberId = " + existMemberId);
        FileCreate fileCreate = fileRequestMapper.toProfileImageFileCreate(mockFile, existMember);

        // then
        assertAll(
                () -> assertThat(fileCreate.getFileType()).isEqualTo(FileType.PROFILE_IMAGE),
                () -> assertThat(fileCreate.getFileName()).isEqualTo(existMemberId + ".jpg"),
                () -> assertThat(fileCreate.getExtension()).isEqualTo("jpg"),
                () -> assertThat(fileCreate.getUploaderId()).isEqualTo(existMemberId),
                () -> assertThat(fileCreate.getFile()).isEqualTo(mockFile)
        );
    }

    @Test
    @DisplayName("toFontTemplateImageFileCreate - should create FileCreate object correctly")
    void toFontTemplateImageFileCreateTest() {
        // given
        MockMultipartFile mockFile = createValidImageFile("template.jpg", "image/jpeg");

        // when
        FileCreate fileCreate = fileRequestMapper.toFontTemplateImageFileCreate(mockFile, existMemberId);

        // then
        assertAll(
                () -> assertThat(fileCreate.getFileType()).isEqualTo(FileType.FONT_PAPER),
                () -> assertThat(fileCreate.getFileName()).isEqualTo(existMemberId + ".jpg"),
                () -> assertThat(fileCreate.getExtension()).isEqualTo("jpg"),
                () -> assertThat(fileCreate.getUploaderId()).isEqualTo(existMemberId),
                () -> assertThat(fileCreate.getFile()).isEqualTo(mockFile)
        );
    }

    @Test
    @DisplayName("should throw FileEmptyException when file is empty")
    void emptyFileTest() {
        // given
        MockMultipartFile mockFile = createEmptyFile("empty.jpg");

        // when & then
        assertThatThrownBy(
                () -> fileRequestMapper.toProfileImageFileCreate(mockFile, existMember)
        ).isExactlyInstanceOf(FileEmptyException.class)
                .hasMessageContaining("File is empty");
    }

    @Test
    @DisplayName("should throw MissingFileExtensionException when file has no extension")
    void noExtensionFileTest() {
        // given
        MockMultipartFile mockFile = createFileWithoutExtension();

        // when & then
        assertThatThrownBy(
                () -> fileRequestMapper.toProfileImageFileCreate(mockFile, existMember)
        ).isExactlyInstanceOf(MissingFileExtensionException.class)
                .hasMessageContaining("Valid file extension is missing");
    }

    @Test
    @DisplayName("should throw MissingFileExtensionException when file has null original filename")
    void nullFilenameTest() {
        // given
        MockMultipartFile mockFile = createNullOriginalFilenameFile();

        // when & then
        assertThatThrownBy(
                () -> fileRequestMapper.toProfileImageFileCreate(mockFile, existMember)
        ).isExactlyInstanceOf(MissingFileExtensionException.class)
                .hasMessageContaining("Valid file extension is missing");
    }

    @Test
    @DisplayName("should throw UnsupportedFileTypeException when file type is not supported")
    void unsupportedFileTypeTest() {
        // given
        MockMultipartFile mockFile = createUnsupportedFile();

        // when & then
        assertThatThrownBy(
                () -> fileRequestMapper.toProfileImageFileCreate(mockFile, existMember)
        ).isExactlyInstanceOf(UnsupportedFileTypeException.class)
                .hasMessageContaining("Only jpg, jpeg, png files are allowed");
    }

    @Test
    @DisplayName("should support different valid image extensions")
    void validExtensionsTest() {
        // given & when & then
        // JPG
        FileCreate jpgCreate = fileRequestMapper.toProfileImageFileCreate(
                createFileWithDifferentExtensions("jpg"),
                existMember
        );
        assertThat(jpgCreate.getExtension()).isEqualTo("jpg");

        // JPEG
        FileCreate jpegCreate = fileRequestMapper.toProfileImageFileCreate(
                createFileWithDifferentExtensions("jpeg"),
                existMember
        );
        assertThat(jpegCreate.getExtension()).isEqualTo("jpeg");

        // PNG
        FileCreate pngCreate = fileRequestMapper.toProfileImageFileCreate(
                createFileWithDifferentExtensions("png"),
                existMember
        );
        assertThat(pngCreate.getExtension()).isEqualTo("png");
    }
}