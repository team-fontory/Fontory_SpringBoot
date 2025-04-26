package org.fontory.fontorybe.unit.mock;

import org.fontory.fontorybe.file.adapter.outbound.s3.dto.AmazonS3ObjectMetadata;
import org.fontory.fontorybe.file.adapter.outbound.s3.dto.AmazonS3PutRequest;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;

import java.time.LocalDateTime;
import java.util.UUID;

public class FakeCloudStorageService implements CloudStorageService {
    private static final String profileImageBucketName = "test-profile-image-bucket";
    private static final String fontPaperBucketName = "test-font-paper-bucket";

    @Override
    public FileMetadata uploadProfileImage(FileCreate fileCreate, String key) {
        AmazonS3PutRequest amazonS3PutRequest = AmazonS3PutRequest.from(fileCreate, key, profileImageBucketName, LocalDateTime.now());
        return uploadFile(amazonS3PutRequest).toModel();
    }

    @Override
    public String getFileUrl(FileMetadata fileMetadata, String key) {
        return fileMetadata.getFileType().toString() + key;
    }

    @Override
    public FileMetadata uploadFontTemplateImage(FileCreate request) {
        String key = UUID.randomUUID().toString();
        AmazonS3PutRequest amazonS3PutRequest = AmazonS3PutRequest.from(request, key, fontPaperBucketName, LocalDateTime.now());
        return uploadFile(amazonS3PutRequest).toModel();
    }

    private AmazonS3ObjectMetadata uploadFile(AmazonS3PutRequest amazonS3PutRequest) {

        return AmazonS3ObjectMetadata.from(amazonS3PutRequest);
    }
}
