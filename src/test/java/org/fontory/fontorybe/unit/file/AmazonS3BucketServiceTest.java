package org.fontory.fontorybe.unit.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.fontory.fontorybe.common.application.ClockHolder;
import org.fontory.fontorybe.config.S3Config;
import org.fontory.fontorybe.file.adapter.outbound.dto.AwsUploadFailException;
import org.fontory.fontorybe.file.adapter.outbound.s3.AmazonS3BucketService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

class AmazonS3BucketServiceTest {
    
    private AmazonS3BucketService amazonS3BucketService;
    private S3Client s3Client;
    private S3Config s3Config;
    private ClockHolder clockHolder;
    
    // Test data
    private String cdnUrl = "https://cdn.example.com";
    private String fontPaperBucket = "fontory-font-paper";
    private String fontBucket = "fontory-fonts";
    private String fontPaperPrefix = "font-papers";
    private String fontPrefix = "fonts";
    private LocalDateTime currentTime = LocalDateTime.of(2025, 1, 26, 10, 0, 0);
    
    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        s3Config = createTestS3Config();
        clockHolder = mock(ClockHolder.class);
        when(clockHolder.getCurrentTimeStamp()).thenReturn(currentTime);
        
        amazonS3BucketService = new AmazonS3BucketService(s3Client, s3Config, clockHolder);
        
        // Call init() manually since @PostConstruct won't run in unit tests
        ReflectionTestUtils.invokeMethod(amazonS3BucketService, "init");
    }
    
    private S3Config createTestS3Config() {
        S3Config config = new S3Config(
                "us-east-1", // region
                cdnUrl,
                "fontory-profile-images", // profileImageBucket  
                fontPaperBucket,
                fontBucket,
                "profile-images", // profileImageBucketPrefix
                fontPaperPrefix,
                fontPrefix
        );
        
        return config;
    }
    
    @Test
    @DisplayName("getFontPaperUrl - should generate correct CDN URL for font paper")
    void getFontPaperUrlTest() {
        // given
        String key = "test-key-123";
        
        // when
        String url = amazonS3BucketService.getFontPaperUrl(key);
        
        // then
        String expected = cdnUrl + "/" + fontPaperPrefix + "/" + key;
        assertThat(url).isEqualTo(expected);
    }
    
    @Test
    @DisplayName("getWoff2Url - should generate correct CDN URL for woff2 font")
    void getWoff2UrlTest() {
        // given
        String key = "font-key-456";
        
        // when
        String url = amazonS3BucketService.getWoff2Url(key);
        
        // then
        String expected = cdnUrl + "/" + fontPrefix + "/" + key + ".woff2";
        assertThat(url).isEqualTo(expected);
    }
    
    @Test
    @DisplayName("getTtfUrl - should generate correct CDN URL for ttf font")
    void getTtfUrlTest() {
        // given
        String key = "font-key-789";
        
        // when
        String url = amazonS3BucketService.getTtfUrl(key);
        
        // then
        String expected = cdnUrl + "/" + fontPrefix + "/" + key + ".ttf";
        assertThat(url).isEqualTo(expected);
    }
    
    @Test
    @DisplayName("uploadFontTemplateImage - should upload image successfully")
    void uploadFontTemplateImageSuccessTest() {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "template.jpg",
                "image/jpeg",
                "Test image content".getBytes(StandardCharsets.UTF_8)
        );
        
        FileCreate fileCreate = FileCreate.builder()
                .file(mockFile)
                .fileName("template.jpg")
                .fileType(FileType.FONT_PAPER)
                .build();
        
        // when
        FileMetadata result = amazonS3BucketService.uploadFontTemplateImage(fileCreate);
        
        // then
        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.getKey()).isNotNull(),
                () -> assertThat(result.getKey()).matches("^[a-f0-9-]+$"), // UUID format
                () -> assertThat(result.getFileName()).isEqualTo("template.jpg"),
                () -> assertThat(result.getFileType()).isEqualTo(FileType.FONT_PAPER),
                () -> assertThat(result.getSize()).isEqualTo(mockFile.getSize()),
                () -> assertThat(result.getUploadedAt()).isEqualTo(currentTime)
        );
        
        // Verify S3 client was called
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
    
    @Test
    @DisplayName("uploadFontTemplateImage - should throw AwsUploadFailException when S3 upload fails")
    void uploadFontTemplateImageFailureTest() {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "template.jpg",
                "image/jpeg",
                "Test image content".getBytes(StandardCharsets.UTF_8)
        );
        
        FileCreate fileCreate = FileCreate.builder()
                .file(mockFile)
                .fileName("template.jpg")
                .fileType(FileType.FONT_PAPER)
                .build();
        
        // Mock S3 client to throw exception
        S3Exception s3Exception = (S3Exception) S3Exception.builder()
                .message("S3 service error")
                .build();
        doThrow(s3Exception).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        
        // when & then
        assertThatThrownBy(
                () -> amazonS3BucketService.uploadFontTemplateImage(fileCreate)
        ).isExactlyInstanceOf(AwsUploadFailException.class)
                .hasMessage("Error occurred during upload to s3");
        
        // Verify S3 client was called
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
    
    @Test
    @DisplayName("uploadFontTemplateImage - should generate unique keys for each upload")
    void uploadFontTemplateImageUniqueKeysTest() {
        // given
        MockMultipartFile mockFile1 = new MockMultipartFile(
                "file",
                "template1.jpg",
                "image/jpeg",
                "Test image 1".getBytes(StandardCharsets.UTF_8)
        );
        
        MockMultipartFile mockFile2 = new MockMultipartFile(
                "file",
                "template2.jpg",
                "image/jpeg",
                "Test image 2".getBytes(StandardCharsets.UTF_8)
        );
        
        FileCreate fileCreate1 = FileCreate.builder()
                .file(mockFile1)
                .fileName("template1.jpg")
                .fileType(FileType.FONT_PAPER)
                .build();
        
        FileCreate fileCreate2 = FileCreate.builder()
                .file(mockFile2)
                .fileName("template2.jpg")
                .fileType(FileType.FONT_PAPER)
                .build();
        
        // when
        FileMetadata result1 = amazonS3BucketService.uploadFontTemplateImage(fileCreate1);
        FileMetadata result2 = amazonS3BucketService.uploadFontTemplateImage(fileCreate2);
        
        // then
        assertAll(
                () -> assertThat(result1.getKey()).isNotNull(),
                () -> assertThat(result2.getKey()).isNotNull(),
                () -> assertThat(result1.getKey()).isNotEqualTo(result2.getKey())
        );
    }
    
    @Test
    @DisplayName("uploadFontTemplateImage - should handle different image types correctly")
    void uploadFontTemplateImageDifferentTypesTest() {
        // given - JPEG file
        MockMultipartFile jpegFile = new MockMultipartFile(
                "file",
                "template.jpg",
                "image/jpeg",
                "JPEG content".getBytes(StandardCharsets.UTF_8)
        );
        
        // given - PNG file
        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "template.png",
                "image/png",
                "PNG content".getBytes(StandardCharsets.UTF_8)
        );
        
        FileCreate jpegCreate = FileCreate.builder()
                .file(jpegFile)
                .fileName("template.jpg")
                .fileType(FileType.FONT_PAPER)
                .build();
        
        FileCreate pngCreate = FileCreate.builder()
                .file(pngFile)
                .fileName("template.png")
                .fileType(FileType.FONT_PAPER)
                .build();
        
        // when
        FileMetadata jpegResult = amazonS3BucketService.uploadFontTemplateImage(jpegCreate);
        FileMetadata pngResult = amazonS3BucketService.uploadFontTemplateImage(pngCreate);
        
        // then
        assertAll(
                () -> assertThat(jpegResult.getFileName()).isEqualTo("template.jpg"),
                () -> assertThat(pngResult.getFileName()).isEqualTo("template.png"),
                () -> assertThat(jpegResult.getSize()).isEqualTo(jpegFile.getSize()),
                () -> assertThat(pngResult.getSize()).isEqualTo(pngFile.getSize())
        );
    }
}