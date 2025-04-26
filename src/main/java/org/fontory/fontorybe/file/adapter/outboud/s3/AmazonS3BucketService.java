package org.fontory.fontorybe.file.adapter.outboud.s3;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fontory.fontorybe.common.application.ClockHolder;
import org.fontory.fontorybe.config.S3Config;
import org.fontory.fontorybe.file.adapter.outboud.dto.AwsUploadFailException;
import org.fontory.fontorybe.file.adapter.outboud.port.CloudStorageService;
import org.fontory.fontorybe.file.domain.FileCreate;
import org.fontory.fontorybe.file.domain.FileMetadata;
import org.fontory.fontorybe.file.domain.FileType;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmazonS3BucketService implements CloudStorageService {

    private final S3Client s3;
    private final S3Config s3Config;
    private final ClockHolder clockHolder;
    private String profileImageBucketName;
    private String fontPaperBucketName;

    @PostConstruct
    void init() {
        profileImageBucketName = s3Config.getBucketName(FileType.PROFILE_IMAGE);
        fontPaperBucketName = s3Config.getBucketName(FileType.FONT_PAPER);
    }

    @Override
    public FileMetadata uploadProfileImage(FileCreate request, String key) {
        AmazonS3PutRequest amazonS3PutRequest = AmazonS3PutRequest.from(request, key, profileImageBucketName, clockHolder.getCurrentTimeStamp());
        return getFileUploadResult(amazonS3PutRequest).toModel();
    }

    public String getFileUrl(FileMetadata fileMetadata, String key) {
        String bucketName = s3Config.getBucketName(fileMetadata.getFileType());
        return s3.utilities()
                .getUrl(builder -> builder
                        .bucket(bucketName)
                        .key(key))
                .toExternalForm();
    }

    /**
     * use uuid as key
     */
    @Override
    public FileMetadata uploadFontTemplateImage(FileCreate request) {
        AmazonS3PutRequest amazonS3PutRequest = AmazonS3PutRequest.from(request, UUID.randomUUID().toString(), fontPaperBucketName, clockHolder.getCurrentTimeStamp());
        return getFileUploadResult(amazonS3PutRequest).toModel();
    }

    private AmazonS3ObjectMetadata getFileUploadResult(AmazonS3PutRequest amazonS3PutRequest) {
        AmazonS3ObjectMetadata metadata = AmazonS3ObjectMetadata.from(amazonS3PutRequest);

        try {
            s3.putObject(amazonS3PutRequest.toPutObjectRequest(),
                    RequestBody.fromBytes(amazonS3PutRequest.getFile().getBytes()));
        } catch (Exception e) {
            log.error("S3 업로드 오류 발생", e);
            throw new AwsUploadFailException("Error occurred during upload to s3");
        }
        return metadata;
    }
}
