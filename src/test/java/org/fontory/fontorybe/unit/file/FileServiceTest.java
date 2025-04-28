package org.fontory.fontorybe.unit.file;

import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileType;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.member.controller.dto.MemberCreateRequest;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.fontory.fontorybe.member.infrastructure.entity.Gender;
import org.fontory.fontorybe.provide.infrastructure.entity.Provider;
import org.fontory.fontorybe.provide.service.dto.ProvideCreateDto;
import org.fontory.fontorybe.unit.mock.FakeFileRepository;
import org.fontory.fontorybe.unit.mock.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class FileServiceTest {
    private FileService fileService;
    private TestContainer testContainer;

    // 테스트 값
    private Long existMemberId;
    private Member existMember;
    private Long nonExistentId = -1L;
    private byte[] fileContent = "Test file content".getBytes(StandardCharsets.UTF_8);
    private String profileImageFilename = "profile.jpg";
    private String fontTemplateFilename = "template.jpg";

    @BeforeEach
    void init() {
        testContainer = new TestContainer();
        fileService = testContainer.fileService;

        // Create a member for testing
        ProvideCreateDto provideCreateDto = new ProvideCreateDto(
                Provider.GOOGLE,
                UUID.randomUUID().toString(),
                "test@example.com"
        );

        existMember = testContainer.create(createMemberRequest("testUser") ,testContainer.provideService.create(provideCreateDto));
        existMemberId = existMember.getId();
    }

    private static MockMultipartFile createValidImageFile(String filename, String contentType) {
        return new MockMultipartFile(
                "file",
                filename,
                contentType,
                "Test file content".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static MemberCreateRequest createMemberRequest(String nickname) {
        return new MemberCreateRequest(
                nickname,
                Gender.MALE,
                LocalDate.of(2025, 1, 26),
                true,
                "testUrl"
        );
    }


    @Test
    @DisplayName("getOrThrowById - should return file metadata when file exists")
    void getOrThrowByIdTest() {
        // given
        MultipartFile mockFile = createValidImageFile(profileImageFilename, "image/jpeg");

        // Upload a file to get its ID
        FileUploadResult uploadResult = fileService.uploadProfileImage(mockFile, existMemberId);
        FileMetadata savedMetadata = testContainer.fileRepository.findById(uploadResult.getId()).get();
        Long fileId = savedMetadata.getId();

        // when
        FileMetadata retrievedMetadata = fileService.getOrThrowById(fileId);

        // then
        assertAll(
                () -> assertThat(retrievedMetadata).isNotNull(),
                () -> assertThat(retrievedMetadata.getId()).isEqualTo(fileId),
                () -> assertThat(retrievedMetadata.getFileName()).isEqualTo(existMemberId + ".jpg"),
                () -> assertThat(retrievedMetadata.getFileType()).isEqualTo(FileType.PROFILE_IMAGE),
                () -> assertThat(retrievedMetadata.getUploaderId()).isEqualTo(existMemberId),
                () -> assertThat(retrievedMetadata.getSize()).isEqualTo(fileContent.length),
                () -> assertThat(retrievedMetadata.getUploadedAt()).isNotNull(),
                () -> assertThat(retrievedMetadata.getCreatedAt()).isNotNull(),
                () -> assertThat(retrievedMetadata.getUpdatedAt()).isNotNull()
        );
    }

    @Test
    @DisplayName("getOrThrowById - should throw exception when file doesn't exist")
    void getOrThrowByIdNonExistentTest() {
        // given
        Long nonExistentFileId = 999L;

        // when & then
        assertThatThrownBy(
                () -> fileService.getOrThrowById(nonExistentFileId)
        ).isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("uploadProfileImage - should upload profile image successfully")
    void uploadProfileImageTest() {
        // given
        MultipartFile mockFile = createValidImageFile(profileImageFilename, "image/jpeg");

        // when
        FileUploadResult result = fileService.uploadProfileImage(mockFile, existMemberId);

        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getFileName()).isEqualTo(existMemberId + ".jpg"),
                () -> assertThat(result.getFileUrl()).isNotNull(),
                () -> assertThat(result.getFileUploadTime()).isNotNull(),
                () -> assertThat(result.getSize()).isEqualTo(fileContent.length)
        );

        // 멤버의 프로필 이미지가 업데이트되었는지 확인
        Member updatedMember = testContainer.memberService.getOrThrowById(existMemberId);
        assertThat(updatedMember.getProfileImageKey()).isNotNull();
    }

    @Test
    @DisplayName("uploadProfileImage - should throw exception when member doesn't exist")
    void uploadProfileImageNonExistentMemberTest() {
        // given
        MultipartFile mockFile = createValidImageFile(profileImageFilename, "image/jpeg");

        // when & then
        assertThatThrownBy(
                () -> fileService.uploadProfileImage(mockFile, nonExistentId)
        ).isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("uploadFontTemplateImage - should upload font template image successfully")
    void uploadFontTemplateImageTest() {
        // given
        MultipartFile mockFile = createValidImageFile(fontTemplateFilename, "image/jpeg");

        // when
        FileUploadResult result = fileService.uploadFontTemplateImage(mockFile, existMemberId);

        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getFileName()).isEqualTo(existMemberId + ".jpg"),
                () -> assertThat(result.getFileUrl()).isNotNull(),
                () -> assertThat(result.getFileUploadTime()).isNotNull(),
                () -> assertThat(result.getSize()).isEqualTo(fileContent.length)
        );
    }

    @Test
    @DisplayName("uploadFontTemplateImage - should throw exception when member doesn't exist")
    void uploadFontTemplateImageNonExistentMemberTest() {
        // given
        MultipartFile mockFile = createValidImageFile(fontTemplateFilename, "image/jpeg");

        // when & then
        assertThatThrownBy(
                () -> fileService.uploadFontTemplateImage(mockFile, nonExistentId)
        ).isExactlyInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("upload and retrieve file metadata should work together")
    void uploadAndRetrieveTest() {
        // given
        MockMultipartFile mockFile = createValidImageFile("profile.jpg", "image/jpeg");

        // when - upload file
        FileUploadResult uploadResult = fileService.uploadProfileImage(mockFile, existMemberId);

        // Find the file ID from repository
        FileMetadata savedMetadata = testContainer.fileRepository.findById(uploadResult.getId()).get();
        Long fileId = savedMetadata.getId();

        // when - retrieve metadata
        FileMetadata retrievedMetadata = fileService.getOrThrowById(fileId);

        // then
        assertAll(
                () -> assertThat(retrievedMetadata).isNotNull(),
                () -> assertThat(retrievedMetadata.getId()).isEqualTo(fileId),
                () -> assertThat(retrievedMetadata.getFileName()).isEqualTo(existMemberId + ".jpg"),
                () -> assertThat(retrievedMetadata.getFileType()).isEqualTo(FileType.PROFILE_IMAGE),
                () -> assertThat(uploadResult.getFileUrl()).contains(retrievedMetadata.getKey())
        );
    }

    @Test
    @DisplayName("uploading different types of images should update FileMetadata correctly")
    void uploadDifferentImagesTest() {
        // given
        MockMultipartFile profileFile = createValidImageFile("profile.jpg", "image/jpeg");
        MockMultipartFile templateFile = createValidImageFile("template.png", "image/png");

        // when - upload profile image
        FileUploadResult profileResult = fileService.uploadProfileImage(profileFile, existMemberId);

        // when - upload template image
        FileUploadResult templateResult = fileService.uploadFontTemplateImage(templateFile, existMemberId);

        // then
        assertAll(
                () -> assertThat(profileResult.getFileName()).contains("jpg"),
                () -> assertThat(templateResult.getFileName()).contains("png"),
                () -> assertThat(((FakeFileRepository)testContainer.fileRepository).findAll()).hasSize(2)
        );

        // Member should have profile image updated
        Member updatedMember = testContainer.memberService.getOrThrowById(existMemberId);
        assertThat(updatedMember.getProfileImageKey()).isNotNull();
    }
}