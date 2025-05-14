package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.config.S3Config;
import org.fontory.fontorybe.file.adapter.outbound.s3.dto.AmazonS3ObjectMetadata;
import org.fontory.fontorybe.file.adapter.outbound.s3.dto.AmazonS3PutRequest;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileType;

import java.time.LocalDateTime;
import java.util.UUID;

public class FakeCloudStorageService implements CloudStorageService {
    private final S3Config s3Config;

    public FakeCloudStorageService(S3Config s3Config) {
        this.s3Config = s3Config;
    }

    @Override
    public FileMetadata uploadProfileImage(FileCreate fileCreate, String key) {
        AmazonS3PutRequest amazonS3PutRequest = AmazonS3PutRequest.from(
                fileCreate,
                key,
                s3Config.getBucketName(FileType.PROFILE_IMAGE),
                s3Config.getPrefix(FileType.PROFILE_IMAGE),
                LocalDateTime.now());
        return uploadFile(amazonS3PutRequest).toModel();
    }

    @Override
    public FileMetadata uploadFontTemplateImage(FileCreate request) {
        String key = UUID.randomUUID().toString();
        AmazonS3PutRequest amazonS3PutRequest = AmazonS3PutRequest.from(
                request,
                key,
                s3Config.getBucketName(FileType.FONT_PAPER),
                s3Config.getPrefix(FileType.FONT_PAPER),
                LocalDateTime.now());
        return uploadFile(amazonS3PutRequest).toModel();
    }

    private String getFileUrl(FileType fileType, String key) {
        return String.format("%s/%s/%s", s3Config.getCdnUrl(), s3Config.getPrefix(fileType), key);
    }

    @Override
    public String getProfileImageUrl(String key) {
        return getFileUrl(FileType.PROFILE_IMAGE, key);
    }

    @Override
    public String getFontPaperUrl(String key) {
        return getFileUrl(FileType.FONT_PAPER, key);
    }

    @Override
    public String getWoff2Url(String key) {
        return getFileUrl(FileType.FONT, key + ".woff2");
    }

    @Override
    public String getTtfUrl(String key) {
        return getFileUrl(FileType.FONT, key + ".ttf");
    }

    private AmazonS3ObjectMetadata uploadFile(AmazonS3PutRequest amazonS3PutRequest) {
        return AmazonS3ObjectMetadata.from(amazonS3PutRequest);
    }
}
