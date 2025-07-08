package org.fontory.fontorybe.integration.file;

import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.application.port.FileService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileUploadResult;
import org.fontory.fontorybe.file.domain.exception.FileNotFoundException;
import org.fontory.fontorybe.member.controller.port.MemberLookupService;
import org.fontory.fontorybe.member.domain.Member;
import org.fontory.fontorybe.member.domain.exception.MemberNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Sql(value = "/sql/createFileTestData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/sql/deleteFileTestData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class FileServiceIntegrationTest {

    @Autowired private MemberLookupService memberLookupService;
    @Autowired private FileService fileService;
    @MockitoBean private S3Client s3Client;
    @MockitoBean private CloudStorageService cloudStorageService;
    @MockitoBean private ApplicationEventPublisher eventPublisher;

    private final Long existMemberId = 999L;
    private final Long nonExistentId = -1L;
    private final Long existFileId = 999L;
    private final String mockedFileUrl = "https://mock-s3.amazonaws.com/test-file.jpg";

    @BeforeEach
    void setup() {
        given(s3Client.copyObject(any(CopyObjectRequest.class)))
                .willReturn(CopyObjectResponse.builder().build());
        given(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .willReturn(DeleteObjectResponse.builder().build());

        // Stub cloud storage upload for font template
        given(cloudStorageService.uploadFontTemplateImage(any(FileCreate.class)))
                .willAnswer(invocation -> {
                    FileCreate fc = invocation.getArgument(0);
                    String key = UUID.randomUUID().toString();
                    return FileMetadata.builder()
                            .fileName(fc.getFileName())
                            .fileType(fc.getFileType())
                            .extension(fc.getExtension())
                            .key(key)
                            .uploaderId(fc.getUploaderId())
                            .size(fc.getFile().getSize())
                            .uploadedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                });

        // Stub getting file URL
        given(cloudStorageService.getFontPaperUrl(anyString()))
                .willReturn(mockedFileUrl);
    }

    @Test
    @DisplayName("getOrThrowById - existing file returns metadata")
    void getFileMetadataByIdTest() {
        FileMetadata meta = fileService.getOrThrowById(existFileId);
        assertThat(meta).isNotNull();
        assertThat(meta.getId()).isEqualTo(existFileId);
        assertThat(meta.getKey()).isNotNull();
        assertThat(meta.getUploaderId()).isEqualTo(existMemberId);
    }

    @Test
    @DisplayName("getOrThrowById - non-existent file throws")
    void getNonExistentFileMetadataTest() {
        assertThatThrownBy(() -> fileService.getOrThrowById(nonExistentId))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    @DisplayName("uploadFontTemplateImage - uploads successfully")
    void uploadFontTemplateImageTest() {
        MockMultipartFile mockFile = createMockImageFile("fontTemplate.png", "image/png");

        FileUploadResult result = fileService.uploadFontTemplateImage(mockFile, existMemberId);

        assertThat(result).isNotNull();
        assertThat(result.getFileUrl()).isEqualTo(mockedFileUrl);
        assertThat(result.getFileName()).isEqualTo(existMemberId + ".png");
        assertThat(result.getSize()).isEqualTo(mockFile.getSize());
    }

    @Test
    @DisplayName("uploadFontTemplateImage - non-existent member throws")
    void uploadFontTemplateImageNonExistentMemberTest() {
        MockMultipartFile mockFile = createMockImageFile("fontTemplate.png", "image/png");
        assertThatThrownBy(() -> fileService.uploadFontTemplateImage(mockFile, nonExistentId))
                .isInstanceOf(MemberNotFoundException.class);
    }

    private MockMultipartFile createMockImageFile(String filename, String contentType) {
        return new MockMultipartFile(
                "file",
                filename,
                contentType,
                "Test image content".getBytes(StandardCharsets.UTF_8)
        );
    }
}
