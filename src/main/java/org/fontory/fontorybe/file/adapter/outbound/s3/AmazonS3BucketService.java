package org.fontory.fontorybe.file.adapter.outbound.s3;

import java.util.UUID;

import org.fontory.fontorybe.common.application.ClockHolder;
import org.fontory.fontorybe.config.S3Config;
import org.fontory.fontorybe.file.adapter.outbound.dto.AwsUploadFailException;
import org.fontory.fontorybe.file.adapter.outbound.s3.dto.AmazonS3ObjectMetadata;
import org.fontory.fontorybe.file.adapter.outbound.s3.dto.AmazonS3PutRequest;
import org.fontory.fontorybe.file.application.port.CloudStorageService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileType;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3BucketService implements CloudStorageService {

    private final S3Client s3;
    private final S3Config s3Config;
    private final ClockHolder clockHolder;
    private String profileImageBucketName;
    private String fontPaperBucketName;
    private String profileImagePrefix;
    private String fontPaperPrefix;

    @PostConstruct
    void init() {
        profileImageBucketName = s3Config.getBucketName(FileType.PROFILE_IMAGE);
        fontPaperBucketName = s3Config.getBucketName(FileType.FONT_PAPER);
        profileImagePrefix = s3Config.getPrefix(FileType.PROFILE_IMAGE);
        fontPaperPrefix = s3Config.getPrefix(FileType.FONT_PAPER);
    }

    @Override
    public FileMetadata uploadProfileImage(FileCreate request, String key) {
        log.info("Uploading profile image: fileName={}, contentType={}, size={} bytes, fileKey={}",
            request.getFileName(), request.getFile().getContentType(), request.getFile().getSize(), key);

        AmazonS3PutRequest amazonS3PutRequest = AmazonS3PutRequest.from(
                request,
                key,
                profileImageBucketName,
                profileImagePrefix,
                clockHolder.getCurrentTimeStamp());

        log.info("Profile image uploaded successfully: fileKey={}, bucket={}", key, profileImageBucketName);
        return uploadFile(amazonS3PutRequest).toModel();
    }

    @Override
    public String getFileUrl(FileMetadata fileMetadata, String key) {
        String bucketName = s3Config.getBucketName(fileMetadata.getFileType());
        String prefix = s3Config.getPrefix(fileMetadata.getFileType());
        log.debug("Generating file URL: fileType={}, fileKey={}, bucket={}",
                fileMetadata.getFileType(), key, bucketName);

        return s3Config.getCdnUrl() + "/" + prefix + "/" + key;
    }

    /**
     * use uuid as fileKey
     */
    @Override
    public FileMetadata uploadFontTemplateImage(FileCreate request) {
        String key = UUID.randomUUID().toString();
        log.info("Uploading font template image: fileName={}, contentType={}, fileType={} bytes, fileKey={}",
                request.getFileName(), request.getFile().getContentType(), request.getFileType(), key);
        AmazonS3PutRequest amazonS3PutRequest = AmazonS3PutRequest.from(
                request,
                key,
                fontPaperBucketName,
                fontPaperPrefix,
                clockHolder.getCurrentTimeStamp());
        FileMetadata result = uploadFile(amazonS3PutRequest).toModel();
        log.info("Font template image uploaded successfully: fileKey={}, bucket={}", key, fontPaperBucketName);
        return result;
    }

    private AmazonS3ObjectMetadata uploadFile(AmazonS3PutRequest amazonS3PutRequest) {
        AmazonS3ObjectMetadata metadata = AmazonS3ObjectMetadata.from(amazonS3PutRequest);

        try {
            log.debug("Sending request to S3: bucket={}, fileKey={}, contentType={}",
                    amazonS3PutRequest.getBucketName(), amazonS3PutRequest.getKey(), amazonS3PutRequest.getFile().getContentType());
            s3.putObject(amazonS3PutRequest.toPutObjectRequest(),
                    RequestBody.fromBytes(amazonS3PutRequest.getFile().getBytes()));
            log.debug("S3 upload completed for fileKey: {}", amazonS3PutRequest.getKey());
        } catch (Exception e) {
            log.error("S3 upload failed: bucket={}, fileKey={}, error={}",
                    amazonS3PutRequest.getBucketName(), amazonS3PutRequest.getKey(), e.getMessage(), e);
            throw new AwsUploadFailException("Error occurred during upload to s3");
        }
        return metadata;
    }
}
